package webServer.ulti;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class Ulti {

	public static final DateFormat DATE_FORMATE = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"dd/MMM/yyyy :HH:mm:ss Z");
	private final static Pattern pattern = Pattern.compile("([^\\s]+(\\.(?i)(py|pl)))");

	public static String getTimeFull(long time) {
		return DATE_FORMATE.format(time);
	}

	public static boolean isScript(String fileName){
		return pattern.matcher(fileName).matches();
	}
	
	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			return fileName.substring(index + 1);
		}
		return "";
	}

//	public static boolean isScriptFile(String fileName) {
//		String extension = getFileExtension(fileName);
//		if (extension.equalsIgnoreCase("py")
//				|| extension.equalsIgnoreCase("pl"))
//			return true;
//		return false;
//	}
	
	/**
	 * Retrieve current time in milliseconds.
	 * 
	 * @return Current time in milliseconds from epoch
	 */
	public static long currentTimeMillis() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTimeInMillis();
	}

	public static String timeInLogFormat() {
		return simpleDateFormat.format(currentTimeMillis());
	}

}
