/*
 * Copyright (c) 2018, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Mon Aug 20 2018
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
package br.com.tz.testing;

import br.com.tz.networking.ICCL;
import br.com.tz.networking.ICM;
import br.com.tz.networking.InternetConnectivityMonitor.ICMEvent;
import br.com.tz.networking.InternetConnectivityMonitor.ICMStatus;

public class ICMTester
{
	public static void main(String[] args) throws Exception
	{
		/*
		ICMTester myself = new ICMTester();
		br.com.tz.networking.InternetConnectivityMonitor.addConnectivityChangeListener( myself );

		Thread.sleep(15000);

		br.com.tz.networking.InternetConnectivityMonitor.removeConnectivityChangeListener( myself );

		Thread.sleep(5000);

		br.com.tz.networking.InternetConnectivityMonitor.addConnectivityChangeListener( myself );

		Thread.sleep(15000);
		*/

		Tester1 t1 = new Tester1();
		Tester1 t2 = new Tester1();

		//br.com.tz.networking.InternetConnectivityMonitor.addConnectivityChangeListener( t1 );
		ICM.addConnectivityChangeListener(t1);
		//br.com.tz.networking.InternetConnectivityMonitor.addConnectivityChangeListener( t2 );
		ICM.addConnectivityChangeListener(t2);

		Thread.sleep(12000);

		//br.com.tz.networking.InternetConnectivityMonitor.removeConnectivityChangeListener( t1 );
		ICM.removeConnectivityChangeListener(t1);

		Thread.sleep(1000);

		System.out.println("exiting main method");
	}
}

class Tester1 implements ICCL
{
	@Override
	public void onIcmStatusChange(ICMEvent evt, ICMStatus stt)
	{
		if ( evt == ICMEvent.CON_FAILURE ) return;
		System.out.println("Tester1, Thread: " + Thread.currentThread().getName() + ", Event occurred: " + evt.name() + ", Connectivity status: " + stt.name());
	}
}

class Tester2 implements ICCL
{
	@Override
	public void onIcmStatusChange(ICMEvent evt, ICMStatus stt)
	{
		if ( evt == ICMEvent.CON_FAILURE ) return;
		try
		{
			System.out.println("sleeping");
			Thread.sleep(10000);
		} 
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Tester2, Thread: " + Thread.currentThread().getName() + ", Event occurred: " + evt.name() + ", Connectivity status: " + stt.name());
	}
}