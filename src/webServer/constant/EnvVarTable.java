package webServer.constant;

import java.util.HashMap;

/**
 * 
 * This class maps http request header field to corresponding Environment
 * variables.
 * 
 */
public final class EnvVarTable {

	public static final String REMOTE_ADDR = "REMOTE_ADDR";
	public static final String SERVER_NAME = "SERVER_NAME";
	public static final String SERVER_SOFTWARE = "SERVER_SOFTWARE";
	public static final String SERVER_PROTOCOL = "SERVER_PROTOCOL";
	public static final String SERVER_PORT = "SERVER_PORT";
	public static final String REQUEST_METHOD = "REQUEST_METHOD";
	public static final String QUERY_STRING = "QUERY_STRING";
	public static final String GATEWAY_INTERFACE = "GATEWAY_INTERFACE";
	public static final String PATH_INFO = "PATH_INFO";
	public static final String SCRIPT_NAME = "SCRIPT_NAME";
	public static final String PATH_TRANSLATED = "PATH_TRANSLATED";
	public static final String HTTP_ACCEPT = "HTTP_ACCEPT";
	public static final String CONTENT_LENGTH = "CONTENT_LENGTH";
	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String HTTP_REFERER = "HTTP_REFERER";
	public static final String REMOTE_HOST = "REMOTE_HOST";
	public static final String HTTP_USER_AGENT = "HTTP_USER_AGENT";
	public static final String HTTP_COOKIE = "HTTP_COOKIE";
	public static final String REMOTE_USER = "REMOTE_USER";

	private static HashMap< String, String > EnvTable = new HashMap< String, String >();

	static {
		EnvTable.put( HeaderFields.ACCEPT, HTTP_ACCEPT );
		EnvTable.put( HeaderFields.CONTENT_LENGTH, CONTENT_LENGTH );
		EnvTable.put( HeaderFields.CONTENT_TYPE, CONTENT_TYPE );
		EnvTable.put( HeaderFields.REFERER, HTTP_REFERER );
		EnvTable.put( HeaderFields.HOST, REMOTE_HOST );
		EnvTable.put( HeaderFields.USER_AGENT, HTTP_USER_AGENT );
	}

	public static boolean containKey( String key ) {
		return EnvTable.containsKey( key );
	}

	public static String get( String key ) {
		return EnvTable.get( key );
	}

	private EnvVarTable() {
	}

}
