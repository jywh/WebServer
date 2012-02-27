package webServer.ulti;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Ulti {

	private static final DateFormat dateFormat = new SimpleDateFormat(
			" EEE, d MMM yyy HH:mm:ss z", Locale.US);
	
	public static synchronized String getTimeFull(long time) {
		return dateFormat.format(time);
	}

	public static String getFileExtension(File document) {
		String name = document.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			return name.substring(index + 1);
		}
		return "";
	}

	public static boolean isModified(long lastModified,
			String dateFromClient) {
		try {
			Date aDate = new Date(lastModified);
			Date anotherDate = dateFormat.parse(dateFromClient);
			return aDate.compareTo(anotherDate) > 0;
		} catch (ParseException pe) {
			// If there is exception, assume file is modified
			pe.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Retrieve current time in milliseconds.
	 * 
	 * @return Current time in milliseconds from epoch
	 */
	public static long currentTimeMillis(){
		Calendar calendar = Calendar.getInstance();
		return calendar.getTimeInMillis();
	}
	
}
