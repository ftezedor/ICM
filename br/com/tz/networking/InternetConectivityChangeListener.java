/*
 * Copyright (c) 2018, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Tue May 19 2018
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

/**
 * 
 * @author  Fabio Tezedor
 * @since   Dec 7, 2018
 * @version 1.0.0
 * 
 */
public interface InternetConectivityChangeListener 
{
	/**
	 * method called automatically when the connectivity status changes
	 * @param evt = event occurred
	 * @param stt = Internet connectivity status
	 */
	public void onIcmStatusChange( ICMEvent evt, ICMStatus stt );
}
