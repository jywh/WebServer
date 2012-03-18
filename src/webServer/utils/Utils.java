package webServer.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {

	public static final DateFormat DATE_FORMATE = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss z",
			Locale.US );

	public static String getFileExtension( File file ) {
		String fileName = file.getName();
		int index = fileName.lastIndexOf( '.' );
		if ( index > 0 ) {
			return fileName.substring( index + 1 );
		}
		return "";
	}

	public static String getTimeFull( long time ) {
		return DATE_FORMATE.format( time );
	}

	/**
	 * Retrieve current time in milliseconds.
	 * 
	 * @return Current time in milliseconds from epoch
	 */
	public static long currentTimeMillis() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTimeInMillis();
	}

	/**
	 * Trim whitespace at the beginning and end, and remove quote at the beginning and end
	 * 
	 * @param orig
	 * @return
	 */
	public static String removeQuote( String orig ) {
		orig = orig.trim();
		int begin = 0, end = orig.length();

		if ( orig.startsWith( "\"" ) )
			begin++;
		if ( orig.endsWith( "\"" ) )
			end--;

		return orig.trim().substring( begin, end );
	}

}
