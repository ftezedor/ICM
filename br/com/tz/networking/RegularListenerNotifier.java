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

final class RegularListenerNotifier implements ListenerNotifier
{
	private final InternetConectivityChangeListener listener;
	
	private boolean fired = false;
	
	RegularListenerNotifier(final InternetConectivityChangeListener listener)
	{
		this.listener = listener;
	}

	// equals() must compare the object with the listener 
	// object instead of the instance of this very class
	@Override
	public boolean equals( final Object arg0 )
	{
		return this.listener.equals(arg0);
	}

	@Override
	public void run()
	{
		// this method isn't necessary for this class
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void notify( final ICMEvent event, final ICMStatus status )
	{
		if ( fired )
		{
			System.out.println("The previous notification did not complete yet");
		}
		else
		{
			this.fired = true;
			this.listener.onIcmStatusChange(event, status);
			this.fired = false;
		}
	}
}