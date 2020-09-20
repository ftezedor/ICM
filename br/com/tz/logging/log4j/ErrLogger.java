/*
 * Copyright (c) 2018, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Mon Feb 11 2019
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
package br.com.tz.logging.log4j;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrLogger extends java.io.PrintStream
{
	private static Logger LOGGER;

	public ErrLogger( java.io.PrintStream s )
	{
		super(s);
		LOGGER = LogManager.getLogger( s.getClass() );
	}
	
    @Override
    public void write(byte[] b) throws IOException 
    {
    	super.write(b);
    	doWrite(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) 
    {
    	doWrite(new String(b, off, len));
    }

    @Override
    public void write(int b) 
    {
    	doWrite(String.valueOf((char)b));
    }

    private void doWrite(String str) 
	{
    	for ( String s : str.split("[\\r?\\n]+") )
		{
    		LOGGER.log( Level.ERROR, s );
		}
    }
}

