package webServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Constant class
 * 
 * @author Wenhui
 *
 */
public final class HttpdConf {

	private HttpdConf() {
	}

	public static String SERVER_ROOT;

	public static String DOCUMENT_ROOT;

	public static int LISTEN = 80;

	public static String LOG_FILE;

	public static Map<String, String> SCRIPT_ALIAS = new HashMap<String, String>();

	public static Map<String, String> ALIAS = new HashMap<String, String>();

	public static String SERVER_ADMIN;

	public static String UPLOAD;

	public static String TEMP_DIRECTORY;

	public static int MAX_THREAD = 10;

	public static boolean CACHE_ENABLE = false;

	public static boolean PERSISTENT_CONNECTION = false;

	public static String[] DIRECTORY_INDEX = { "index.html", "index.htm",
			"default.html", "default.htm" };

	public static Map<String, String> CGI_HANDLER = new HashMap<String, String>();

}
