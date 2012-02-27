package webServer.ulti;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import webServer.constant.HttpdConf;

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
	 * Access log
	 * 
	 *************************************************************/
	
	public static void initialize() throws IOException {
		if(!verifyFile(HttpdConf.LOG_FILE)){
			throw new FileNotFoundException("Access log file not found");
		}
	}

	private static boolean verifyFile(String path) throws IOException {
		File logFile = new File(path);

		if (!logFile.exists()) {
			return logFile.createNewFile();
		}
		return true;
	}
	
	
	public synchronized static void access(String content) {

		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(HttpdConf.LOG_FILE, true));
			out.write(content);
			out.newLine();
			out.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		} 
	}
	
	/*************************************************************
	 * 
	 * Debug log
	 * 
	 *************************************************************/
	
	public static void debug(String tag, String message){
		if( DEBUG )
			System.out.printf("%-35s %-15s %s\n",getCallingClass(), tag, message);
	}
	
	public static void debug(String field, Integer number){
		if( DEBUG )
			System.out.printf("%-35s %-15s %d\n",getCallingClass(), field, number);
	}

	public static void debug(String field, Float f){
		if( DEBUG )
			System.out.printf("%-35s %-15s %f\n",getCallingClass(), field, f);
	}
	
	public static void debug(String field, Double d){
		if( DEBUG ) 
			System.out.printf("%-35s %-15s %f\n",getCallingClass(), field, d);
	}
	
	public static void debug(String field, char c){
		if( DEBUG ) 
			System.out.printf("%-35s %-15s %s\n",getCallingClass(), field, c);
	}
	
	private static String getCallingClass(){
		return new Throwable().getStackTrace()[2].getClassName();
	}
	
	public static void debug(String field, Long d){
		if( DEBUG ) 
			System.out.printf("%-35s %-15s %d\n",getCallingClass(), field, d);
	}
}
