package webServer.log;

/**
 * <p>
 * This class is used for debugging.
 * </p>
 * 
 */
public class Log {

	/**
	 * Use to control debug mode. This should be set to false when in real production.
	 */
	public static boolean DEBUG = false;

	public static void debug( String tag, String message ) {
		if ( DEBUG )
			System.out.printf( "%-15s %s\n", tag, message );
	}

	public static void debug( String tag, Integer number ) {
		if ( DEBUG )
			System.out.printf( "%-15s %d\n", tag, number );
	}

	public static void debug( String tag, Float f ) {
		if ( DEBUG )
			System.out.printf( "%-15s %f\n", tag, f );
	}

	public static void debug( String tag, Double d ) {
		if ( DEBUG )
			System.out.printf( "%-15s %f\n", tag, d );
	}

	public static void debug( String tag, char c ) {
		if ( DEBUG )
			System.out.printf( "%-15s %s\n", tag, c );
	}

	public static void debug( String tag, Long d ) {
		if ( DEBUG )
			System.out.printf( "%-15s %d\n", tag, d );
	}

}
