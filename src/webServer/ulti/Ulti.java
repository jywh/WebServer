package webServer.ulti;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Ulti {

	public static final DateFormat DATE_FORMATE = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy :HH:mm:ss Z");
	
	public static synchronized String getTimeFull(long time) {
		return DATE_FORMATE.format(time);
	}

	public static String getFileExtension(File document) {
		String name = document.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			return name.substring(index + 1);
		}
		return "";
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
	
	public static String timeInLogFormat() {
		return simpleDateFormat.format(currentTimeMillis());
	}
}
