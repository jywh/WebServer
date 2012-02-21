package webServer.ulti;

/**
 * 
 * This class is used for debugging.
 * 
 * @author Wenhui
 *
 */
public class Log {

	public static boolean LOG_ON = true;
	
	public static void log(String tag, String message){
		if( LOG_ON )
			System.out.printf("%-35s %-15s %s\n",getCallingClass(), tag, message);
	}
	
	public static void log(String field, Integer number){
		if( LOG_ON )
			System.out.printf("%-35s %-15s %d\n",getCallingClass(), field, number);
	}

	public static void log(String field, Float f){
		if( LOG_ON )
			System.out.printf("%-35s %-15s %f\n",getCallingClass(), field, f);
	}
	
	public static void log(String field, Double d){
		if( LOG_ON ) 
			System.out.printf("%-35s %-15s %f\n",getCallingClass(), field, d);
	}
	
	private static String getCallingClass(){
		return new Throwable().getStackTrace()[2].getClassName();
	}
	
}
