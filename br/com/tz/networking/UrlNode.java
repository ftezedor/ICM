/*
 * Copyright (c) 2020, Fabio Tezedor <fabio@tezedor.com.br>
 *
 * Sat Mar 28 2020
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class UrlNode
{
	public int failCounter = 0;
	public final String url;
	public final HttpURLConnection conn;

	protected UrlNode(int counter, String url) throws MalformedURLException, IOException
	{
		this.failCounter = counter;
		this.url = url;
		this.conn = (java.net.HttpURLConnection) (new java.net.URL(url)).openConnection();
		this.conn.setConnectTimeout(500);
		this.conn.addRequestProperty("User-Agent", "Mozilla/4.0");
	}

	protected UrlNode(String url) throws MalformedURLException, IOException 
	{
		this(0,url);
	}

	/**
	 * creates a new instance of the UrlNode class
	 * 
	 * @param url - URL object containing the website url
	 * @return {@code UrlNode} object
	 */
	public final static UrlNode create(URL url)
	{
		return create(0, url.toString());
	}
	

	/**
	 * creates a new instance of the UrlNode class
	 * 
	 * @param url - string represent a url
	 * @return {@code UrlNode} object
	 */
	public final static UrlNode create(String url)
	{
		return create(0, url);
	}
	
	/**
	 * creates a new instance of the UrlNode class
	 * 
	 * @param counter - the failure counter
	 * @param url - string represent a url
	 * @return {@code UrlNode} object
	 */
	public final static UrlNode create(int counter, String url)
	{
		try
		{
			return new UrlNode(counter, url);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * gets a {@code String[]} and gives back a {@code List<UrlNode>}
	 * 
	 * @param urls - the array of urls in string format
	 * @return {@code List<UrlNode>}
	 */
	public static List<UrlNode> toList(String[] urls)
	{
		return toList(Arrays.asList(urls));
	}

	/**
	 * gets a {@code List<String>} and gives back a {@code List<UrlNode>}
	 * 
	 * @param urls - the list of urls in string format
	 * @return {@code List<UrlNode>}
	 */
	public static List<UrlNode> toList(List<String> urls)
	{
		return urls.stream().map(u -> UrlNode.create(0,u)).filter(un -> un != null).collect(Collectors.toList());
	}

	public String toString()
	{
		return "(url=" + url + ", counter=" + failCounter + ")";
	}
}