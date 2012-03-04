package webServer.ulti;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Ulti {

	public static final DateFormat DATE_FORMATE = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"dd/MMM/yyyy :HH:mm:ss Z");

	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			return fileName.substring(index + 1);
		}
		return "";
	}

	public static String getTimeFull(long time) {
		return DATE_FORMATE.format(time);
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

	public static String timeInLogFormat() {
		return simpleDateFormat.format(currentTimeMillis());
	}

	@SuppressWarnings("rawtypes")
	public static String getCurrentPackageName(Class c){
		String className = c.getName();
		System.out.println("class name: "+className);
		int indexLastDot = className.lastIndexOf('.');
		return className.substring(0, indexLastDot);
	}
	
	public synchronized static void writeFile(String content, String path) {

		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
			out.write(content);
			out.newLine();
			out.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		} 
	}
}
