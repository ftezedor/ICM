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

import br.com.tz.networking.InternetConnectivityMonitor.ICMEvent;
import br.com.tz.networking.InternetConnectivityMonitor.ICMStatus;


final class ThreadedListenerNotifier implements ListenerNotifier
{
	private final InternetConectivityChangeListener listener;
	protected Thread thread = null;
	private ICMEvent event = null;
	private ICMStatus status = null;
	
	ThreadedListenerNotifier(final InternetConectivityChangeListener listener)
	{
		this.listener = listener;
		this.thread = new Thread( this );
		this.thread.setDaemon(true);
		this.thread.setName(this.thread.getName().replace("Thread", "ICM-Notifier"));
		this.thread.start();
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
		while ( !Thread.interrupted() )
		{
			if ( event == null || status == null )
			{
				synchronized (this)
				{
					try
					{
						this.wait();
					} 
					catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
						continue;
					}
				}
			}
			listener.onIcmStatusChange(event, status);
			event = null;
			status = null;
		}
		System.out.println( "exiting " + Thread.currentThread().getName() ); 
	}
	
//	@Override
//	public void die()
//	{
//		if ( thread != null && thread.getState().equals( Thread.State.WAITING ) )
//		{
//			thread.interrupt();
//		}
//	}
	
	@Override
	public void notify( final ICMEvent event, final ICMStatus status )
	{
		if ( thread != null && thread.getState().equals( Thread.State.TERMINATED ) )
		{
			return;
		}

		//System.out.println( "threaded notifying" );
		this.event = event;
		this.status = status;
		synchronized (this)
		{
			if ( thread.getState().equals(Thread.State.WAITING) )
			{
				this.notify();
			}
		}
	}
}