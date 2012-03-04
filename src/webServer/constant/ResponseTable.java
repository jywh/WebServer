package webServer.constant;

import java.util.HashMap;

public final class ResponseTable {

	public static final int OK = 200;
	public static final int CREATED = 201;
	public static final int NO_CONTENT = 204;
	public static final int FOUND = 302;
	public static final int NOT_MODIFIED = 304;
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int NOT_IMPLEMENTED = 501;
	
	private static HashMap<Integer, String> responsePhrase = new HashMap<Integer, String>();
	
	static {
		responsePhrase.put(OK, "200 OK");
		responsePhrase.put(CREATED, "201 Created");
		responsePhrase.put(NO_CONTENT, "204 No Content");
		responsePhrase.put(FOUND, "302 Found");
		responsePhrase.put(NOT_MODIFIED, "304 Not Modified");
		responsePhrase.put(BAD_REQUEST, "400 Bad Request");
		responsePhrase.put(UNAUTHORIZED, "401 Unauthorized");
		responsePhrase.put(FORBIDDEN, "403 Fobidden");
		responsePhrase.put(NOT_FOUND, "404 Not Found");
		responsePhrase.put(INTERNAL_SERVER_ERROR, "500 Internal Server Error");
		responsePhrase.put(NOT_IMPLEMENTED, "501 Not Implemented");
	}
	
	public static String getResponsePhrase(Integer statusCode) {
		String phrase = responsePhrase.get(statusCode);
		if (phrase != null)
			return phrase;
		else
			return responsePhrase.get(OK);
	}

	private ResponseTable(){}
}
