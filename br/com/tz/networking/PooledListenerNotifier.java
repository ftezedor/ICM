/*
 * Copyright (c) 2020, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Mon Jun 08 2020
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 * 
 * https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * "Free software is a matter of liberty, not price. To understand the concept, 
 * you should think of free as in free speech, not as in free beer." — Richard Stallman
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package br.com.tz.networking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import br.com.tz.networking.InternetConnectivityMonitor.ICMEvent;
import br.com.tz.networking.InternetConnectivityMonitor.ICMStatus;


final class PooledListenerNotifier implements ListenerNotifier
{
	// create a thread pool with daemon threads 
	private static final ExecutorService es = Executors.newFixedThreadPool(10, 
			r -> {
					//System.out.println("ThreadedListenerNotifier:ExecutorService");
        			//Thread t = Executors.defaultThreadFactory().newThread(r);
					Thread t = new Thread(r);
					t.setName("ln-tpool-" + t.getName().toLowerCase());
        			t.setDaemon(true);
        			return t;
        		}
    		);
	
	static 
	{
		setShutdownHook();
	}
	
	private final InternetConectivityChangeListener listener;

	private ICMEvent event = null;
	private ICMStatus status = null;
	
	private long lastNotification = Long.MAX_VALUE;
	
	PooledListenerNotifier(final InternetConectivityChangeListener listener)
	{
		this.listener = listener;
	}
	
	/**
	 * sets up the 'shutdown' method to be called when the jvm is going down
	 */
	private static void setShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread() 
		{
			@Override
			public void run()
			{
				//System.out.println("Shutting ListenerNotifierQueue down");
				PooledListenerNotifier.shutdown();
				//System.out.println("ListenerNotifierQueue has been shutdown");
			}
		});
	}

	/**
	 * in order to get the thread pool (and its worker threads) stopped, this method
	 * must be called even though if threads were set to daemon 
	 */
	public static void shutdown()
	{
		//if ( Executor.isShutdown() || Executor.isTerminated() ) return;
		if ( es.isShutdown() || es.isTerminated() ) return;
		try
		{
//			Executor.shutdown();
//			Executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
			es.shutdown();
			es.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} 
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// equals() must compare the object with the listener 
	// object instead of the instance of this very class
	@Override
	public boolean equals( Object arg0 )
	{
		return this.listener.equals(arg0);
	}
	
	@Override
	public void run()
	{
		listener.onIcmStatusChange(event, status);
		event = null;
		status = null;
		lastNotification = Long.MAX_VALUE;
		//System.out.println( "exiting " + Thread.currentThread().getName() ); 
	}
	
	@Override
	public void notify( final ICMEvent event, final ICMStatus status )
	{
		//if ( Executor.isShutdown() || Executor.isTerminated() )
		if ( es.isShutdown() || es.isTerminated() )
		{
			new IllegalStateException("Internal thread pool is shutdown").printStackTrace();
			return;
		}
		
		// a tenth of a second seems to be the perfect amount of time to get a good implemented onIcmStatusChange executed
		if ( System.currentTimeMillis() - lastNotification > 100 )
		{
			StringBuilder sb = new StringBuilder("WARN: The previous notification did not complete yet.\n");
			if ( System.currentTimeMillis() - lastNotification > 5000 )
			{
				sb.append("WARN: " + ((System.currentTimeMillis() - lastNotification)/1000) + " secs has elapsed since then.\n");
				sb.append("WARN: Looks like it got stuck or blocked.\n");
			}
			else if ( System.currentTimeMillis() - lastNotification > 1000 )
			{
				sb.append("WARN: " + ((System.currentTimeMillis() - lastNotification)/1000) + " secs has elapsed since then.\n");
				sb.append("WARN: Looks like it got stuck or blocked or is taking too much time to complete.\n");
			}
			else
			{
				sb.append("WARN: Looks like it is taking too much time to complete.\n"); 
			}
			sb.append("WARN: check onIcmStatusChange@" + listener.getClass().getName() + " for performance issues.");
			System.out.println(sb.toString());
		}
		else
		{
			this.event = event;
			this.status = status;

			this.lastNotification = System.currentTimeMillis();
			
			//Executor.submit(this);
			es.submit(this);
		}
	}
	
}
