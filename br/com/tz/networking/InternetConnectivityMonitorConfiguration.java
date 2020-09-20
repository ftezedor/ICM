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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;

/*
 * April 7, 2020 - notification mode was introduced
 */

/**
 * simple framework (http://simple.sourceforge.net/) is used here to serialize/deserialize
 */
@Root(name="config")
class InternetConnectivityMonitorConfiguration
{
	java.util.List<String> _urls = null;
	
	byte _failure_max_number_level1 = 3,   // number of failures in a row to consider connectivity to internet broken and than slow down the time between checkings
		 _failure_max_number_level2 = 13,  // number of failures in a row to slow down the time between checkings even more
		 _listeners_max_number = 15;       // 2020-6-6 - max number of listeners ICM must accept
	
	short _success_sleep_interval = 3000,		 // time in milliseconds to perform the next checking
		  _failure_seepp_interval_level1 = 1000,  // time in milliseconds to perform the next checking when the previous one failed
		  _failure_seepp_interval_level2 = 5000,  // time in milliseconds to perform the next checking when the previous one failed and the connectivity is broken
		  _failure_seepp_interval_level3 = 10000; // time in milliseconds to perform the next checking when the previous one failed and the connectivity is broken for awhile
	
	String _notify_mode = "serial";
	
	boolean _wait_on_failure = true; // 2020-6-5

	public InternetConnectivityMonitorConfiguration() 
	{
	}
	
	@ElementList(name="urls",entry="url")
	public void setUrls( java.util.List<String> urls )
	{
		this._urls = urls;
		//System.out.println(urls.toString());
	}

	@ElementList(name="urls",entry="url")
	public java.util.List<String> getUrls()
	{
		//return (this._urls==null?this._internal_urls:this._urls);
		return _urls;
	}

	@Path("failure/limit")
	@Element(name="level1")
	public void setMaxNumberFailuresL1( byte n )
	{
		this._failure_max_number_level1 = n;
	}

	@Path("failure/limit")
	@Element(name="level1")
	public byte getMaxNumberFailuresL1()
	{
		return this._failure_max_number_level1;
	}

	@Path("failure/limit")
	@Element(name="level2")
	public void setMaxNumberFailuresL2( byte n )
	{
		this._failure_max_number_level2 = n;
	}

	@Path("failure/limit")
	@Element(name="level2")
	public byte getMaxNumberFailuresL2()
	{
		return this._failure_max_number_level2;
	}

	@Path("success")
	@Element(name="sleepTime")
	public void setSuccessSleepTime( short n )
	{
		this._success_sleep_interval = n;
	}
	
	@Path("success")
	@Element(name="sleepTime")
	public short getSuccessSleepTime()
	{
		return this._success_sleep_interval;
	}

	@Path("failure/sleep-time")
	@Element(name="level1")
	public void setFailureSleepTimeL1( short n )
	{
		this._failure_seepp_interval_level1 = n;
	}

	@Path("failure/sleep-time")
	@Element(name="level1")
	public short getFailureSleepTimeL1()
	{
		return this._failure_seepp_interval_level1;
	}

	@Path("failure/sleep-time")
	@Element(name="level2")
	public void setFailureSleepTimeL2( short n )
	{
		this._failure_seepp_interval_level2 = n;
	}

	@Path("failure/sleep-time")
	@Element(name="level2")
	public short getFailureSleepTimeL2()
	{
		return this._failure_seepp_interval_level2;
	}

	@Path("failure/sleep-time")
	@Element(name="level3")
	public void setFailureSleepTimeL3( short n )
	{
		this._failure_seepp_interval_level3 = n;
	}

	@Path("failure/sleep-time")
	@Element(name="level3")
	public short getFailureSleepTimeL3()
	{
		return this._failure_seepp_interval_level3;
	}

	// added Jun 5, 2020
	@Path("failure")
	@Element(name="wait-on-failure")
	public void setWaitOnFailure( boolean b )
	{
		this._wait_on_failure = b;
	}

	// added Jun 5, 2020
	@Path("failure")
	@Element(name="wait-on-failure")
	public boolean getWaitOnFailure()
	{
		return this._wait_on_failure;
	}

	// added Jun 6, 2020
	@Path("listeners")
	@Element(name="max")
	public void setMaxListenersNumber( byte b )
	{
		this._listeners_max_number = b;
	}

	// added Jun 6, 2020
	@Path("listeners")
	@Element(name="max")
	public byte getMaxListenersNumber()
	{
		return this._listeners_max_number;
	}

	@Path("notification")
	@Element(name="mode")
	public void setNotificantionMode( String mode )
	{
		if ( mode == null || mode.isEmpty() ) return; //mode = "parallel";
		if ( !(mode.equalsIgnoreCase("serial") ||  mode.equalsIgnoreCase("parallel")) )
		{
			throw new IllegalArgumentException("Invalid notification mode '" + mode + "'");
		}
		_notify_mode = mode;
	}
	
	@Path("notification")
	@Element(name="mode")
	public String getNotificantionMode()
	{
		return this._notify_mode;
	}
	
//	public static java.util.List<String> getInnerUrls()
//	{
//		return new ArrayList<String>(
//				Arrays.asList(
//						"http://www.google.com.br",  
//						"https://registro.br",  
//						"http://www.facebook.com.br",  
//						"http://www.ibm.com.br",  
//						"https://www.itau.com.br",  
//						"http://www.receita.fazenda.gov.br",  
//						"https://www.sbt.com.br",  
//						"https://www.bradesco.com.br",  
//						"http://www.faperj.br",  
//						"http://www.fapesp.br",  
//						"http://mec.gov.br",  
//						"https://www.pucsp.br",  
//						"https://www.rnp.br",  
//						//"https://ufrj.br",  
//						"http://www.unesp.br",  
//						"http://www.unicamp.br",  
//						"http://www.usp.br",  
//						"https://www.chevrolet.com.br",  
//						"https://www.embratel.com.br",  
//						"https://www.vw.com.br"
//					));
//	}
	
	public String exportConfiguration() throws Exception 
	{
		java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

		((Serializer) new Persister()).write(this, os);

		return os.toString();

	}
	
	public static InternetConnectivityMonitorConfiguration importConfiguration( String xml ) throws Exception
	{
		return ((Serializer) new Persister()).read(InternetConnectivityMonitorConfiguration.class, xml);
	}
	
	public static InternetConnectivityMonitorConfiguration importConfiguration( java.net.URL url ) throws FileNotFoundException, IOException, Exception
	{
		return importConfiguration( new java.io.FileInputStream(new File(url.toURI())) );
	}

	public static InternetConnectivityMonitorConfiguration importConfiguration( java.net.URI uri ) throws FileNotFoundException, IOException, Exception
	{
		return importConfiguration( new java.io.FileInputStream(new File(uri)) );
	}
	
	public static InternetConnectivityMonitorConfiguration importConfiguration( java.io.File file ) throws FileNotFoundException, IOException, Exception
	{
		return importConfiguration( new java.io.FileInputStream(file) );
	}

	//public static InternetConnectivityMonitorConfiguration importConfiguration( java.io.FileInputStream fis ) throws IOException, Exception
	public static InternetConnectivityMonitorConfiguration importConfiguration( java.io.InputStream is ) throws IOException, Exception
	{
		return importConfiguration( getFileContent( is ) );
	}
	
	//private static String getFileContent( java.io.FileInputStream fis ) throws IOException
	private static String getFileContent( java.io.InputStream is ) throws IOException
	{
		java.io.BufferedReader br = new java.io.BufferedReader( new java.io.InputStreamReader(is) );
		StringBuilder sb = new StringBuilder();
		String line;
		while(( line = br.readLine()) != null ) 
		{
	         sb.append( line + '\n' );
		}
		br.close();
		return sb.toString();
	}
	
	public static void main( String[] args )
	{
		try 
		{
			System.out.println(
					"Usage: InternetConnectivityMonitorConfiguration.doDeserialize( xml_string )" +
					"\n\nThe xml template below must be used\n\n" + 
					"<config>\n" + 
					"   <!-- add many urls as possible -->\n" + 
					"   <urls class=\"java.util.ArrayList\">\n" + 
					"      <url>http://www.google.com.br</url>\n" + 
					"      <url>https://registro.br</url>\n" + 
					"   </urls>\n" + 
					"   <!-- \n" + 
					"        it is highly recommended to keep untouched the values of the following nodes unless you know what you are doing \n" + 
					"        they are dependent on each other and must be set very carefully in order to keep the good functioning of the app\n" + 
					"   -->\n" + 
					"   <failure>\n" + 
					"      <!-- tell the app whether it should or not wait between checkings when a failure occurs\n" +  
				    "           it affects only the level1 checking attempts -->\n" +
					"      <wait-on-failure>yes<wait-on-failure>\n" +
					"      <limit>\n" + 
					"         <!-- number of failed attempts in a row before considering connectivity to the internet broken -->\n" + 
					"         <level1>3</level1>\n" + 
					"         <!-- number of failed attempts in a row to raise  the time between checkings -->\n" + 
					"         <level2>13</level2>\n" + 
					"      </limit>\n" + 
					"      <sleep-time>\n" + 
					"         <!-- time in milliseconds to perform the next checking when the previous one failed -->\n" + 
					"         <level1>1000</level1>\n" + 
					"         <!-- time in milliseconds to perform the next checking when the previous one failed and the connectivity is broken -->\n" + 
					"         <level2>5000</level2>\n" + 
					"         <!-- time in milliseconds to perform the next checking when the previous one failed and the connectivity is broken for awhile -->\n" + 
					"         <level3>10000</level3>\n" + 
					"      </sleep-time>\n" + 
					"   </failure>\n" + 
					"   <success>\n" + 
					"      <!-- time in milliseconds to perform the next checking -->\n" + 
					"      <sleepTime>3000</sleepTime>\n" + 
					"   </success>\n" + 
					"   <notification>\n" +
					"      <!-- valid values parallel or serial -->\n" +
					"      <!-- parallel tells icm to notify listeners using threads -->\n" +
					"      <!-- serial tells to not use threads -->\n" +
					"      <mode>parallel</mode>\n" + 
					"   </notification>\n" +
					"</config>"
			);
		}
		catch( Exception ex)
		{
			ex.printStackTrace();
		}
	}

}