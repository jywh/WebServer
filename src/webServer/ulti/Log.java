package webServer.ulti;


/**
 * 
 * This class is used for log server access informatipon and for debugging.
 * 
 *
 */
public class Log {

	private static boolean DEBUG = false;
	
	/*************************************************************
	 * 
	 * Debug log
	 * 
	 *************************************************************/
	
	public static void debug(String tag, String message){
		if( DEBUG )
			System.out.printf("%-15s %s\n",tag, message);
	}
	
	public static void debug(String field, Integer number){
		if( DEBUG )
			System.out.printf("%-15s %d\n", field, number);
	}

	public static void debug(String field, Float f){
		if( DEBUG )
			System.out.printf("%-15s %f\n", field, f);
	}
	
	public static void debug(String field, Double d){
		if( DEBUG ) 
			System.out.printf("%-15s %f\n", field, d);
	}
	
	public static void debug(String field, char c){
		if( DEBUG ) 
			System.out.printf("%-15s %s\n", field, c);
	}
	
	public static void debug(String field, Long d){
		if( DEBUG ) 
			System.out.printf("%-15s %d\n", field, d);
	}
}
