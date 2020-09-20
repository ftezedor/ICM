/*
 * Copyright (c) 2020, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Tue Jun 09 2020
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

public interface ListenerNotifier extends Runnable
{

//	@Override
//	abstract boolean equals(Object arg0);

	//void die();

	void notify(ICMEvent event, ICMStatus status);

}