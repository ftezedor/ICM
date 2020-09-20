/*
 * Copyright (c) 2018, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Tue Dec 31, 2018
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
 *
 * this is a very simple and minimalist logger intended to be used to redirect System.out 
 * and System.err to a file.
 * the use of a professional and robust log service is more than encouraged
 */
package br.com.tz.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * This class is intended to be used to redirect System.out and System.err to a file (log)
 * 
 * @author Fabio Tezedor
 */
public class Logger extends java.io.OutputStream
{
	private Type eLoggerType = Type.INFO;
	
	private static final java.util.regex.Pattern newLineRegEx = java.util.regex.Pattern.compile("[\\r?\\n]+");
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static java.io.PrintWriter oLogger = null;
	
	public static enum Type
	{
		INFO, ERROR
	}

	private static void initialize() throws IOException
	{
		if ( oLogger != null ) return;
		
		java.io.File file = new java.io.File("output.log");
		java.io.FileWriter fw = new java.io.FileWriter(file, true);
		oLogger = new java.io.PrintWriter(fw);
	}
	
	private Logger( Type t ) throws IOException
	{
		eLoggerType = t;
		initialize();
	}
	
	public static java.io.PrintStream getLogger( Type t ) throws IOException
	{
		return new java.io.PrintStream( new Logger( t ));
	}
	
	public static java.io.PrintStream getLogger() throws IOException
	{
		return getLogger( Type.INFO );
	}
	
    public void write(byte[] b) throws IOException 
    {
    	super.write(b);
    	doWrite(new String(b));
    }

    public void write(byte[] b, int off, int len) 
    {
    	doWrite(new String(b, off, len));
    }

    public void write(int b) 
    {
    	doWrite(String.valueOf((char)b));
    }

    private void doWrite(String s)
    {
    	br.com.tz.logging.Logger.logIt(s, eLoggerType);
    }
    
    private static final void logIt( final String str, final Type t )
	{
    	String[] a = newLineRegEx.split(str);

    	for ( String s : a )
		{
    		oLogger.write(formatter.format(new java.util.Date()) + "  " + t.name().charAt(0) + "  " + s + "\r\n");
    		oLogger.flush();
		}
    }
}