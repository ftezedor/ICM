/*
 * Copyright (c) 2018, Fabio Tezedor. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Tue May 19 2018
 *
 */
package br.com.tz.networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.stream.Collectors;

/*
 * history
 * 
 *    Sep, 2018
 *       the start, pause, resume, and stop of the monitor was manually commanded.
 *       it's up to the clients to get ICM started, paused, resumed, or stopped.
 *    May, 2019
 *       the start, pause, resume, and stop are now called automatically by the monitor itself.
 *       when registering the first foreign listener, the monitor is automatically initiated.
 *       when the last foreign listener is removed, the monitor is automatically put on hold.
 *    Aug, 2019
 *       the HttpURLConnection object that was being recreated repeatedly for each and every 
 *       url in the run() method had its creation moved to the UrlNode class constructor so, 
 *       the connection now is created only once when the UrlNode object  is  created  thus, 
 *       delegating to the run() method just the task of connecting to the website.
 *    Apr, 2020
 *       the notification mode has been introduced.
 *       it can be serial (default) or parallel.
 *       parallel mode means that listeners will get notified by separate threads.
 *       serial mode means that listeners will get notified by the ICM's main thread.
 *       the use of parallel notifications can prevent the ICM's main thread to get stuck or 
 *       blocked by listeners with a bad onIcmStatusChange's implementing code but it comes
 *       with a certain overhead added by the creation of the extra threads.
 *       although serial notifications don't have such an extra overhead, listeners' bad imple-
 *       mentations of the method onIcmStatusChange can lead the ICM's main thread to get stuck 
 *       or blocked thus making ICM inoperable.      
 *       this can be changed in the configuration file 
 *    May, 2020
 *       instead of creating new threads to notify listeners when notification mode is set to 
 *       parallel, every listener is now being encapsulated into a class that has its own no-
 *       tification thread. This way, bad implementations of the onIcmStatusChange will affect 
 *       only its corresponding thread and the overhead of threads creation will be diminished 
 *       considerably. Due to this change, the default notification mode is now parallel.
 *    Jun, 2020
 *    	 * introduced config option "max listeners number". its purpose is to define how many 
 *       listeners ICM should accept and thus prevent programmers to make all their threads an 
 *       ICM listener.
 *       * introduced config option "wait on failure". when it's set to false, in case of con-
 *       nection failures, there will be no sleeping between checkings. it affects only level1 
 *       checkings. this way, for apps whose time is in the essence, ICM can shorten the time 
 *       it takes to state the connection to the internet is down.
 *       * the dedicated thread for each individual listener was replaced by a pool of threads 
 *       so no matter how many listeners we have the number of threads will remain stead. that 
 *       is OK since well-implemented onIcmStatusChange methods should be executed in  just  a 
 *       few milliseconds
 */

/**
 * This class checks the Internet connectivity by establishing an http
 * connection to a predefined list of well known websites. Classes can be
 * notified about changes by registering themselves using the method
 * addConnectivityChangeListener. To do so they must implement the interface
 * InternetConectivityChangeListener
 * <p/>
 * e.g.:
 * <code>InternetConnectivityMonitor.addConnectivityChangeListener(this)</code>
 * 
 * @author Fabio Tezedor
 * @since May 19, 2018
 * @version 1.4.0
 */
//public final class InternetConnectivityMonitor implements Runnable, InternetConectivityChangeListener
public class InternetConnectivityMonitor implements Runnable, InternetConectivityChangeListener
{
	public static enum ICMEvent
	{
		NOTHING, MON_STARTED, MON_PAUSED, MON_RESUMED, MON_STOPPED, MON_ABORTED, CON_CHANGED, CON_FAILURE
	}

	public static enum ICMStatus
	{
		ONLINE, OFFLINE, UNKNOWN
	}

	public static enum State
	{
		RUNNING, PAUSED, STOPPED
	}
	
	private static boolean simulate = false;

	private static boolean verbose = false;
	private static State state = State.STOPPED;
	private static final Object pauseLock = new Object();
	
	private volatile boolean running = false;
	private volatile boolean paused = false;
	private volatile boolean online = false;
	private boolean innerUrlsInUse = true;

	private static InternetConnectivityMonitor icmInst = SingletonHelper.INSTANCE;
	private static Thread icmThread = new Thread(icmInst, "ICM-Thread");
	// list to hold registered listeners
	//private static java.util.List<InternetConectivityChangeListener> listeners = new ArrayList<InternetConectivityChangeListener>();
	private static java.util.List<ListenerNotifier> listeners = new ArrayList<ListenerNotifier>();

	// this is a singleton class and that's why the constructor is private
	//private InternetConnectivityMonitor()
	protected InternetConnectivityMonitor()
	{
		if (System.getProperty("icm.verbose") != null)
		{
			setVerboseOn();
		}

		InternetConnectivityMonitorConfiguration icmCfg = null;

		// if specified at command line, get values from config file
		// e.g.: java -Dicm.configurationFile=/path/to/the/config.xml ...
		if (System.getProperty("icm.configurationFile") != null)
		{
			try
			{
				icmCfg = InternetConnectivityMonitorConfiguration.importConfiguration(new java.io.File(System.getProperty("icm.configurationFile")));
				Configuration.source = System.getProperty("icm.configurationFile");
			} 
			catch (FileNotFoundException e)
			{
				System.err.println("Config file '" + System.getProperty("icm.configurationFile") + "' was not found");
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// if config file was not informed or could not be loaded let's try the inner one
		if (Configuration.source.indexOf("default") > 0)
		{
			try
			{
				java.io.InputStream is = getClass().getResourceAsStream("/br/com/tz/config/icm.cfg");
				icmCfg = InternetConnectivityMonitorConfiguration.importConfiguration(is);
				is.close();
				is = null;
				Configuration.source = "jar:/br/com/tz/config/icm.cfg";
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// if Configuration.source is different of 'default'
		// replace default config values by the ones from the config file
		if (Configuration.source.indexOf("default") <= 0)
		{
			Configuration.maxListenersNumber         = icmCfg.getMaxListenersNumber();
			Configuration.maxNumberOfFailuresLevel1  = icmCfg.getMaxNumberFailuresL1();
			Configuration.maxNumberOfFailuresLevel2  = icmCfg.getMaxNumberFailuresL2();
			Configuration.successSleepInterval       = icmCfg.getSuccessSleepTime();
			Configuration.failureSleepIntervalLevel1 = icmCfg.getFailureSleepTimeL1();
			Configuration.failureSleepIntervalLevel2 = icmCfg.getFailureSleepTimeL2();
			Configuration.failureSleepIntervalLevel3 = icmCfg.getFailureSleepTimeL3();
			Configuration.waitOnFailure              = icmCfg.getWaitOnFailure();
			Configuration.notificationMode           = icmCfg.getNotificantionMode();
			Configuration.urls = icmCfg.getUrls();
			// since urls were load from config file set innerUrlsInUse = false
			innerUrlsInUse = false;
		}

		if (verbose)
		{
			System.out.println("Using configuration file "     + Configuration.source + "\n\n"
					+ "Maximum number of failures (level 1): " + Configuration.maxNumberOfFailuresLevel1 + "\n"
					+ "Failure sleep interval (level 1): "     + Configuration.failureSleepIntervalLevel1 + " ms\n"
					+ "Maximum number of failures (level 2): " + Configuration.maxNumberOfFailuresLevel2 + "\n"
					+ "Failure sleep interval (level 2): "     + Configuration.failureSleepIntervalLevel2 + " ms\n"
					+ "Failure sleep interval (level 3): "     + Configuration.failureSleepIntervalLevel3 + " ms\n"
					+ "Wait on failure: "                      + (Configuration.waitOnFailure ? "ON" : "OFF") + "\n"
					+ "Success sleep interval: "               + Configuration.successSleepInterval + " ms\n"
					+ "Notification mode: "                    + Configuration.notificationMode + "\n");
		}
	}

	public final static void setVerboseOn()
	{
		verbose = true;
	}

	public final static void setVerboseOff()
	{
		verbose = false;
	}

	public final static State getState()
	{
		return state;
	}

	public final static void main(String[] args)
	{
		final boolean debugMode = true;

		System.out.println(	"Copyright (c) Fabio Tezedor, 2018\n"
				+ "\n*** to see the connection turning offline/online, disable/re-enable your networking ***" );

		if (!debugMode) return;

		// System.out.println("\n" + InternetConnectivityMonitor.getConfiguration() +
		// "\n");

		// the lines below set 4 timers to change the static field 'simulation'
		// in order to simulate the break of the internet connectivity
//		Environment.timer.schedule( createSimulationTimerTask(true),  11000);
//		Environment.timer.schedule( createSimulationTimerTask(false), 21000);
//		Environment.timer.schedule( createSimulationTimerTask(true),  43000);
//		Environment.timer.schedule( createSimulationTimerTask(false), 51000);
		
		try
		{
			// System.out.println("\n" + InternetConnectivityMonitor.getState().toString());
			System.out.println("\nStarting the one-minute long self-testing\n");
			// InternetConnectivityMonitor.setVerboseOn();
			start();
			System.out.println("The Internet Connectivity Monitor has been started");

			Thread.sleep(60000);
			
//			Thread.sleep(30000);
//			pause();
//			Thread.sleep(10000);
//			resume();
//			Thread.sleep(30000);

			if (stop())
			{
				System.out.println("The Internet Connectivity Monitor has been stopped");
			} 
			else
			{
				System.err.println("The Internet Connectivity Monitor couldn't be stopped");
			}

			System.out.println("\nThe self-testing has finished");
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * register listeners to be notified about connectivity status changes
	 * 
	 * @return void
	 * @param listener a class that implements the interface
	 *                 InternetConectivityChangeListener
	 */
	@SuppressWarnings("unlikely-arg-type")
	public final static void addConnectivityChangeListener(final InternetConectivityChangeListener listener)
	{
		if ( listeners.size() >= Configuration.maxListenersNumber ) 
			throw new RuntimeException("There is no room for more listeners");

		if (listeners.stream().filter( e -> e.equals(listener) ).findFirst().isPresent())
		{
			if (verbose)
				System.out.println(
						"You (" + listener.getClass().getName() + "@" + listener.getClass().hashCode() + ") again?");
			return;		
		}
		// check the listener against the ones already registered to prevent duplicates
//		for (int i = 0; i < listeners.size(); i++)
//		{
//			if (listeners.get(i).equals(listener))
//			{
//				if (verbose)
//				{
//					System.out.println("You (" + listener.getClass().getName() + "@" + listener.getClass().hashCode() + ") again?");
//				}
//				return;
//			}
//		}

		// start monitoring if the listener being added is not the monitor itself
		if ( !listener.equals(icmInst) ) start();

		// ICM's onIcmStatusChange is well implemented so, it can be notified by the ICM's 
		// thread with no worries regardless of what the notification mode is set to
		if ( ! Configuration.notificationMode.equalsIgnoreCase("parallel") || listener.equals(icmInst) )
		{
			listeners.add( new RegularListenerNotifier(listener) );
		}
		else
		{
			//listeners.add( new ThreadedListenerNotifier(listener) );
			listeners.add( new PooledListenerNotifier(listener) );
		}

		if (verbose)
			System.out.format( "Listeners: %s out of %d. %s still available.\n", 
					listeners.size() + " slot" + (listeners.size() > 1 ? "s" : ""), 
					Configuration.maxListenersNumber, 
					Configuration.maxListenersNumber-listeners.size() + " slot" +
					(Configuration.maxListenersNumber - listeners.size() > 1 ? "s" : "") );

		listeners.get(listeners.size()-1).notify(ICMEvent.CON_CHANGED, (icmInst.online ? ICMStatus.ONLINE : ICMStatus.OFFLINE));
	}

	/**
	 * unregister listeners 
	 * 
	 * @param listener - a class that implements the interface
	 *                 InternetConectivityChangeListener
	 */
	@SuppressWarnings("unlikely-arg-type")
	public final static void removeConnectivityChangeListener(InternetConectivityChangeListener listener)
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			if (listeners.get(i).equals(listener))
			{
				listeners.remove(i); //.die();
				break;
			}
		}

		if (verbose)
			System.out.format( "Listeners: %s out of %d. %s still available.\n", 
					listeners.size() + " slot" + (listeners.size() > 1 ? "s" : ""), 
					Configuration.maxListenersNumber, 
					Configuration.maxListenersNumber-listeners.size() + " slot" +
					(Configuration.maxListenersNumber - listeners.size() > 1 ? "s" : "") );
		
		// if listeners list has one or none listeners the monitor can be paused
		if ( listeners.size() <= 1 ) pause();
	}

	/**
	 * start the monitor
	 */
	protected final static void start()
	{
		if (verbose)
		{
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
			System.out.println(caller.getClassName() + "." + caller.getMethodName() + " called ICM's start method ");
		}

		if (icmInst.paused) resume();
		if (icmInst.running) return;

		// Lazy Initialization
		// instantiate InternetConnectivityMonitor right before it's gonna be used
		// if ( icmInst == null ) icmInst = SingletonHelper.INSTANCE;
		// if ( icmThread == null ) icmThread = new Thread( icmInst );

		if (icmThread.getState() == Thread.State.TERMINATED)
		{
			icmThread = new Thread(icmInst, "ICM-Thread");
		}

		// running a thread as daemon allows JVM to kill it when going down
		// thus there's no need for any explicit 'stop' call to be made
		if ( ! icmThread.isDaemon() ) icmThread.setDaemon(true);
		
		icmInst.running = true;
		icmThread.start();
	}

	/**
	 * stops monitor
	 */
	protected final static boolean stop()
	{
		if (verbose)
		{
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
			System.out.println(caller.getClassName() + "." + caller.getMethodName() + " called ICM's stop method ");
		}

		// since thread might be paused let's invoke the resume() method, 
		// just in case it is, before trying to stop it
		resume();
		// signaling it to stop
		if (icmInst.running) icmInst.running = false;
		// wait for the thread to leave method run(). it wouldn't take long.
		while (icmThread.isAlive())
		{
			// let's wait for a little while before checking icmThread once more
			try
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e)
			{
			}
		}
		return icmThread.getState() == Thread.State.TERMINATED;
	}

	/**
	 * pause monitor
	 */
	protected final static void pause()
	{
		if (verbose)
		{
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
			System.out.println(caller.getClassName() + "." + caller.getMethodName() + " called ICM's pause method ");
		}

		if (!icmInst.running) return;
		icmInst.paused = true;
	}

	/**
	 * resume monitoring previously paused
	 */
	protected final static void resume()
	{
		if (verbose)
		{
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
			System.out.println(caller.getClassName() + "." + caller.getMethodName() + " called ICM's resume method ");
		}

		// if it's not running there's no point in trying to resume it
		if (!icmInst.running) return;
		// if it's not paused just get the hell out of here
		if (!icmInst.paused) return;
		// if reached here do whatever needs to be done to get it resumed
		synchronized (pauseLock)
		{
			icmInst.paused = false;
			pauseLock.notifyAll(); // Unblocks threads
		}
	}

	// holds the last event occurred
	// it's intended to be used by the notifyListeners method
	private int lastEvent; // = State.STOPPED.hashCode() * -1;

	/**
	 * notify all registered listeners about the connectivity status change
	 */
	private final void notifyListeners(final ICMEvent evt, final ICMStatus stt)
	{
		int event = evt.hashCode();
//		if ( simulate ) stt = ICMStatus.OFFLINE;
		/* a negative value means connection is offline */
		if ( stt == ICMStatus.OFFLINE || simulate ) event *= -1;
		// if event and status are the same as the previous one, just get the hell out right away
		if (lastEvent == event && evt != ICMEvent.CON_FAILURE) return;
		// otherwise save them
		lastEvent = event;
		online = (event > 0);
		// traverse the listeners list and notify each and every one
		listeners.stream().filter(l -> l != null).forEach( l -> l.notify(evt, (simulate ? ICMStatus.OFFLINE : stt)) );
//				{
//					//if ( verbose ) System.out.println("Notifying " + l.getClass() + " about the occurred event");
//					notifyListener( l, evt, (simulate ? ICMStatus.OFFLINE : stt) );
//					//if ( verbose ) System.out.println("Notifying " + l.getClass() + " about connectivity status change");		
//				} );
	}
	
	/**
	 * Delegates the notification to a separate thread
	 * 
	 * @param l - the listener
	 * @param e - the triggered event
	 * @param s - the connectivity status
	 */

	/*
	public void notifyListener( final InternetConectivityChangeListener l, final ICMEvent e, final ICMStatus s )
	{
		if ( Configuration.notificationMode.equalsIgnoreCase("parallel"))
		{
			Thread t = new Thread() {
				@Override
				public void run() {
					l.onIcmStatusChange(e, s);
				}
			};
			t.setDaemon(true);
			t.start();
		}
		else
		{
			l.onIcmStatusChange( e, (simulate ? ICMStatus.OFFLINE : s) );
		}
	}
	*/

	/**
	 * The main method that does the actual job
	 */
	public final void run()
	{
		// set innerUrlsInUse = false no matter if it's the first run or not
		innerUrlsInUse = false;

		addConnectivityChangeListener(icmInst);

		byte fmn_l1 = Configuration.maxNumberOfFailuresLevel1, fmn_l2 = Configuration.maxNumberOfFailuresLevel2;

		short ssi = Configuration.successSleepInterval, 
				fsi_l1 = Configuration.failureSleepIntervalLevel1,
				fsi_l2 = Configuration.failureSleepIntervalLevel2, 
				fsi_l3 = Configuration.failureSleepIntervalLevel3;

		// urls' circular list
		UrlCircularList clUrls = new UrlCircularList(UrlNode.toList(Configuration.urls));

		UrlNode urlNode = null;

		int failCounter = 0; // http connection failure counter

		// notify listeners the monitor has been started
		notifyListeners(ICMEvent.MON_STARTED, (icmInst.online ? ICMStatus.ONLINE : ICMStatus.OFFLINE));

		while (running)
		{
			state = State.RUNNING;

			if (clUrls.size() <= 0)
			{
				(new java.lang.IndexOutOfBoundsException("URLs list is empty")).printStackTrace();
				// if urls list is empty let's use the inner defined ones
				if (!innerUrlsInUse)
				{
					innerUrlsInUse = true;
					clUrls = new UrlCircularList(UrlNode.toList(Configuration.builtinUrls));
					System.err.print("The urls list got empty then the inner defined urls will be loaded");
				}
				// if urls list is empty there's nothing to do but stop the 
				// monitor and notify everyone the connectivity is broken
				else
				{
					running = false;
					// if ( online ) notifyListeners( false );
					notifyListeners(ICMEvent.MON_ABORTED, ICMStatus.OFFLINE);
					System.err.print("The urls list got empty thus the monitor cannot do its job and will be ended");
				}
			}

			if (paused)
			{
				synchronized (pauseLock)
				{
					if (!running) break;

					try
					{
						state = State.PAUSED;

						notifyListeners(ICMEvent.MON_PAUSED, (icmInst.online ? ICMStatus.ONLINE : ICMStatus.OFFLINE));
						pauseLock.wait();
						notifyListeners(ICMEvent.MON_RESUMED, (icmInst.online ? ICMStatus.ONLINE : ICMStatus.OFFLINE));
					} 
					catch (InterruptedException ex)
					{
						break;
					}
				}
				// 'running' status might have changed since we paused
				if (!running) break;
			}

			try
			{
				urlNode = clUrls.getNext();
				if (verbose) System.out.println("Probing " + urlNode.url);

				// every url node holds an http connection object specially built for it
				urlNode.conn.connect();
				urlNode.conn.getInputStream().close();
				urlNode.conn.disconnect();

				// reset the global and the url failure counter since the connection was successfully established
				failCounter = 0;
				urlNode.failCounter = 0;

				// notify the Internet connection is up
				notifyListeners(ICMEvent.CON_CHANGED, ICMStatus.ONLINE);

				// check once more if stop was called
				if (!running) break;

				// let's wait awhile before performing another check
				if (ssi > 0) Thread.sleep(ssi);
			} 
			catch (java.net.MalformedURLException e)
			{
				System.err.println(urlNode.url + ": " + (new RuntimeException(e)).getMessage());
				// if url is malformed it must be popped out from the list
				clUrls.remove(urlNode);
			} 
			catch (java.io.IOException e)
			{
				// if connectivity goes down it can take awhile to get back so there's no point
				// in get hundreds or thousands exceptions logged
				if (!(e.getMessage().contains("Network is unreachable") || e instanceof java.net.UnknownHostException))
				{
					System.err.println(">>> " + urlNode.url + " <<<");
					e.printStackTrace();
				}
				// if exception thrown is related to time out, increase the url failure counter
				// but if the failure counter reached the max number of 10, remove the url from
				// the list and perform another check right away using the next url on the list
				if (e.getMessage().contains("timed out"))
				{
					System.err.println(urlNode.url + " did not respond in a timely manner");
					urlNode.failCounter++;
					if (urlNode.failCounter >= 10)
					{
						if (verbose) 
							System.out.println("'" + urlNode.url + "' has timed out too many times and will be removed");
						clUrls.remove(urlNode);
					}
				} 
				else
				{
					//e.printStackTrace();
					// if status is online, report connection failure
					// once the connection failed the status must be reported as unknown
					if (icmInst.online) notifyListeners(ICMEvent.CON_FAILURE, ICMStatus.UNKNOWN);

					failCounter++; // increase the failure counter

					try
					{
						// even if waitOnFailure is false, it's better sleep a  
						// bit so, let's make fsi_l1 = 100 (tenth of a second)
						if ( ! Configuration.waitOnFailure ) fsi_l1 = 100;
						Thread.sleep(((online || failCounter <= fmn_l1 ? fsi_l1 : (failCounter > fmn_l2 ? fsi_l3 : fsi_l2))));
					} 
					catch (InterruptedException e1)
					{
					}
				}
			} 
			catch (InterruptedException ie)
			{
				ie.printStackTrace();
			}

			// if global failure counter is equal or greater than fmn_l1, notify
			if (failCounter >= fmn_l1)
			{
				notifyListeners(ICMEvent.CON_CHANGED, ICMStatus.OFFLINE);
				if (failCounter == Integer.MAX_VALUE)
				{
					failCounter = fmn_l2 + 1; // prevent overflow. although unlikely, it could happen.
				}
			}
		}

		state = State.STOPPED;
		notifyListeners(ICMEvent.MON_STOPPED, (icmInst.online ? ICMStatus.ONLINE : ICMStatus.OFFLINE));
	}

//	public static void terminate()
//	{
//		System.out.println("terminate called");
//		stop();
//	}

	@Override
	public void onIcmStatusChange(ICMEvent evt, ICMStatus stt)
	{
		System.out.println("Event occurred: " + evt.name() + ", Connectivity status: " + stt.name());
	}

	private final static TimerTask createSimulationTimerTask( final boolean status )
	{
		return new TimerTask() 
		{
			@Override
			public void run()
			{
				InternetConnectivityMonitor.simulate = status;
			}
		};
	}
	
	/*
	 * ICM's supporting classes
	 */
	
//	private static final class ListenerEntry
//	{
//		public final InternetConectivityChangeListener listener;
//		public long last = 0;
//		
//		public ListenerEntry(InternetConectivityChangeListener listener)
//		{
//			this.listener = listener;
//		}
//	}
	
	/**
	 * Bill Pugh Singleton approach. 
	 * Lazy initialization.
	 * Doesn’t require synchronization.
	 * It is thread safe
	 */
	private static final class SingletonHelper
	{
		private static final InternetConnectivityMonitor INSTANCE = new InternetConnectivityMonitor();
	}

	protected static class Configuration
	{
		public static String source = "built-in default values";
		public static byte maxNumberOfFailuresLevel1 = 3, 
				maxNumberOfFailuresLevel2 = 13,
				maxListenersNumber = 15;
		// ssi = success sleep interval, fsi_l1 = failure sleep interval level 1, ...
		public static short successSleepInterval = 3000, 
				failureSleepIntervalLevel1 = 1000,
				failureSleepIntervalLevel2 = 5000, 
				failureSleepIntervalLevel3 = 10000;
		public static String notificationMode = "parallel";
		public static java.util.List<String> urls = Arrays.asList(
				"http://www.google.com.br", "https://registro.br",
				"http://www.facebook.com.br", "http://www.ibm.com.br", "https://www.itau.com.br",
				"http://www.receita.fazenda.gov.br", "https://www.bradesco.com.br"
				);
		public static boolean waitOnFailure = true;
		public final static java.util.List<String> builtinUrls = urls.stream().collect(Collectors.toList());
	}
	
}
