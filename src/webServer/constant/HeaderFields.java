package webServer.constant;

/**
 * <p>
 * Header fields parameters.
 * </p>
 * 
 */
public final class HeaderFields {

	public static final String CONNECTION = "Connection";
	public static final String DATE = "Date";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CACHE_CONTROL = "Cache-Control";
		
	/*******************************************
	 * For request specific
	 ********************************************/
	
	public static final String HOST = "Host";
	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
	public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
	public static final String IF_NONE_MATCH = "If-None-Match";
	public static final String AUTHORIZATION = "Authorization";
	public static final String ACCEPT = "Accept";
	public static final String ACCEPT_CHARSET = "Accept-Charset";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String FROM = "From";
	public static final String USER_AGENT = "User-Agent";
	public static final String REFERER = "Referer";
	
	/*******************************************
	 * For reponse specific
	 ********************************************/
	
	public static final String AGE = "Age";
	public static final String EXPIRE = "Expire";
	public static final String LAST_MODIFIED = "Last-Modified";
	public static final String SERVER = "Server";
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String SET_COOKIE = "Set-Cookie";
	
	private HeaderFields(){}
}
