package br.com.tz.collections;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * usage: new Version("1.0.23@2019-10-3")
 * 
 * @author Fabio Tezedor
 */
public class Version
{
	public final int max;
	public final int mid;
	public final int min;
	public final Date release;
	public final String owner;
	public final String className;
	
	public Version( String version, String owner )  
	{
		this.owner = owner;
		
		className = Thread.currentThread().getStackTrace()[2].getClassName();
		
		String pattern = "^(?:([0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{1,2}@[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}))$";
		if ( ! Pattern.matches( pattern, version ) )
		{
			throw new RuntimeException( "Argument passed does not conform to the expected format '##.##.##@####-##-##'" );
		}

		String[] x = version.split("@");
		String[] s = x[0].split("\\.");
		//System.out.println( version );
		max = Integer.parseInt(s[0]);
		if ( s.length == 3 )
		{
			mid = Integer.parseInt(s[1]);
			min = Integer.parseInt(s[2]);
		}
		else if ( s.length == 2 )
		{
			mid = 0;
			min = Integer.parseInt(s[1]);
		} 
		else
		{
			mid = 0;
			min = 0;
		}
		Date d = new Date();
		try
		{
			d = (new SimpleDateFormat("yyyy-MM-dd")).parse( x[1] );
		}
		catch ( ParseException e )
		{
			//release = new Date();
		}
		release = d;
	}
	
	@Override
	public String toString()
	{
		return String.format( "%s version \"%d.%d.%d\" %s (c) %s %s", 
				className, max, mid, min, 
				(new SimpleDateFormat("yyyy-MM-dd")).format( release ),
				(new SimpleDateFormat("yyyy")).format( release ),
				owner );
		
//		return className + " version \"" + max + "." + mid + "." + min + "\" " + 
//				new SimpleDateFormat("yyyy-MM-dd").format( release );
	}
}
