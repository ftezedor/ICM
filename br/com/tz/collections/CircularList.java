package br.com.tz.collections;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * To create a CircularList a list must be provided in order to initialize it.
 * Once created, the list cannot be modified meaning it is immutable although the list members can.
 * 
 * @author (c) 2019, Fabio Tezedor
 *
 * @param <T>
 */
public class CircularList<T>
{
	public static final Version version = new Version("1.0.9@2020-03-07", "Fabio Tezedor");
	
	protected final List<T> innerList;
	protected int current = 0;
	
	public CircularList(List<T> list)
	{
		//this.innerList = list;
		this.innerList = list.stream().collect( Collectors.toList() );
	}
	
//	public static <X extends Object> CircularList<X> create( List<X> list )
//	{
//		return new CircularList<X>( list );
//	}
	
	// this constructor is intended to be used by the clone method solely 
	// in order to allow the list's current item to be set
	protected CircularList(List<T> list, int current)
	{
		if ( current >= list.size() )
		{
			throw new IndexOutOfBoundsException("Index number of the current item is bigger than the list size");
		}
		//this.innerList = list;
		this.innerList = list.stream().collect( Collectors.toList() );
		this.current = current;
	}
	
//	public static <T> CircularList<T> create( List<T> list )
//	{
//		return new CircularList<T>( list );
//	}

	public final T getNext()
	{
		if ( current >= innerList.size() ) current = 0;
		return innerList.get(current++);
	}
	
	public final int size()
	{
		return innerList.size(); 
	}
	
	public final void forEach( Consumer<? super T> cons )
	{
		innerList.forEach(cons);
	}
	
	public CircularList<T> clone()
	{
		return new CircularList<T>( innerList, current );
	}
	
	public static void main( String[] args )
	{
		List<String> lst = Arrays.asList( 
				"http://www.google.com.br", 
				"https://registro.br", 
				"yyy",
				"http://www.facebook.com.br", 
				"http://www.ibm.com.br", 
				"https://www.itau.com.br",
				"http://www.receita.fazenda.gov.br", 
				"https://www.bradesco.com.br" );
		
		//CircularList<UrlNode> cl = new CircularList<UrlNode>( UrlNode.toList(lst) );
//		UrlCircularList cl = new UrlCircularList( UrlNode.toList(lst) );
		UrlCircularList cl = UrlCircularList.create( UrlNode.toList(lst) );
		
		
		for ( int i=0; i<10; i++ )
		{
			UrlNode w = cl.getNext();
			if ( w.url == "yyy" ) w.counter++;
			System.out.println( w.url + ", " + w.counter );
			if ( w.counter >= 10 )
			{
				System.out.println("--------------------------------------------------------------");
				System.out.println(cl.toString());
				System.out.println("--------------------------------------------------------------");
				System.out.println("deleting url " + w.url);
				System.out.println("--------------------------------------------------------------");
				cl.remove(w);
				System.out.println(cl.toString());
				System.out.println("--------------------------------------------------------------");
			}
		}
		
		//UrlCircularList ucl = (UrlCircularList) cl.clone();
		UrlCircularList ucl = cl.clone();
		System.out.println( "cl.hashCode="+cl + ", ucl.hashCode="+ucl );
	}
}

final class UrlNode
{
	public int counter = 0;
	public final String url;
	
	protected UrlNode(int counter, String url ) 
	{
		this.counter = counter;
		this.url = url;
	}

	protected UrlNode( String url ) 
	{
		this.url = url;
	}
	
	public static List<UrlNode> toList( String[] urls )
	{
		return toList( Arrays.asList( urls ));
	}
	
	/**
	 * Takes a List of Strings and gives back a List of UrlNodes 
	 * @param List{@literal <}String{@literal >} urls
	 * @return List{@literal <}UrlNode{@literal >}
	 */
	public static List<UrlNode> toList( List<String> urls )
	{
		List<UrlNode> lst = new Vector<UrlNode>();
		for ( int i=0;i<urls.size();i++ ) 
		{
			lst.add(new UrlNode(urls.get(i)));
		}
		return lst;
	}
}

//since CircularList is immutable it is needed to extend it and add the remove method
final class UrlCircularList extends CircularList<UrlNode>
{
	public UrlCircularList(List<UrlNode> list) 
	{
		super(list);
	}
	
	private UrlCircularList(List<UrlNode> list, int current)
	{
		super(list, current);
	}

	public static UrlCircularList create( List<UrlNode> list )
	{
		return new UrlCircularList( list );
	}

	public static UrlCircularList create( UrlNode[] urls )
	{
		return create( Arrays.asList( urls ) );
	}
	
	public void remove(UrlNode nd)
	{
		if ( innerList.contains(nd) ) innerList.remove(nd);
	}
	
	@Override
	public UrlCircularList clone()
	{
		return new UrlCircularList( innerList.stream().collect( Collectors.toList() ), current );
	}
}

