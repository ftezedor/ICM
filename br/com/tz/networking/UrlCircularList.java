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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import br.com.tz.collections.CircularList;

//extends CircularList adding the 'remove' method 
//so the inner list entries can be drew out
final class UrlCircularList extends CircularList<UrlNode>
{
	public UrlCircularList(List<UrlNode> list)
	{
		super(list);
	}

	// this constructor is intended to be used by the clone method solely 
	// in order to allow the list's current item to be set
	private UrlCircularList(List<UrlNode> list, int current)
	{
		super(list, current);
	}

	public static UrlCircularList create(List<UrlNode> list)
	{
		return new UrlCircularList(list);
	}

	public static UrlCircularList create(UrlNode[] urls)
	{
		return create(Arrays.asList(urls));
	}

	public void remove(UrlNode nd)
	{
		if (innerList.contains(nd)) innerList.remove(nd);
	}

	@Override
	public UrlCircularList clone()
	{
		return new UrlCircularList(innerList.stream().collect(Collectors.toList()), current);
	}
}