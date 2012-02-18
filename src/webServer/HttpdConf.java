package webServer;

import java.util.HashMap;


public class HttpdConf {

	public static String SERVER_ROOT;

	public static String DOCUMENT_ROOT;
	
	public static int LISTEN = 80;
	
	public static String LOG_FILE;
	
	public static HashMap<String, String> SCRIPT_ALIAS= new HashMap<String, String>();
	
	public static HashMap<String, String> ALIAS = new HashMap<String, String>(); 
	
	public static String SERVER_ADMIN;
	
	public static String UPLOAD;
	
	public static String TEMP_DIRECTORY;
	
	public static int MAX_THREAD = 2;
	
	public static boolean CACHE_ENABLE=false;
	
	public static boolean PERSISTENT_CONNECTION = false;
	
	public static String[] DIRECTORY_INDEX={"index.html", "index.htm", "default.html", "default.htm"};
	
	public static HashMap<String, String> CGI_HANDLER = new HashMap<String,String>();
	
}
