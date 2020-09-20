package br.com.tz.utils;

//import java.io.File;
//import java.nio.file.Path;
import java.util.Timer;

public class Environment
{
	public final static Timer timer = new Timer("System timer", true);
	
//	public static Path getPath( String path )
//	{
//		File dir = new File( path );
//		if ( dir.exists() && dir.isDirectory() )
//		{
//			return dir.toPath();	
//		}
//		return null;
//	}
	
	public static <T> T orElse( T t1, T t2 )
	{
		if ( t1 == null ) return t2;
		//if ( t1.getClass() == String.class && t1.equals("") ) return t2;
		if ( t1.getClass() == String.class && t1.toString().isEmpty() ) return t2;
		return t1;
	}
}
