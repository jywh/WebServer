package webServer.utils;

/**
 * <p>
 * This class is used for debugging.
 * </p>
 * 
 */
public class Log {

	private static boolean DEBUG = false;

	public static void debug(String tag, String message) {
		if (DEBUG)
			System.out.printf("%-15s %s\n", tag, message);
	}

	public static void debug(String field, Integer number) {
		if (DEBUG)
			System.out.printf("%-15s %d\n", field, number);
	}

	public static void debug(String field, Float f) {
		if (DEBUG)
			System.out.printf("%-15s %f\n", field, f);
	}

	public static void debug(String field, Double d) {
		if (DEBUG)
			System.out.printf("%-15s %f\n", field, d);
	}

	public static void debug(String field, char c) {
		if (DEBUG)
			System.out.printf("%-15s %s\n", field, c);
	}

	public static void debug(String field, Long d) {
		if (DEBUG)
			System.out.printf("%-15s %d\n", field, d);
	}
	
}
