 package webServer.request;

import java.util.HashMap;

import webServer.HttpdConf;

public class Request {

	public static final int GET = 0;
	public static final int POST = 1;
	public static final int HEAD = 2;
	public static final int PUT = 3;
	
	private String requestFields;
	private String httpVersion,URI, parameterString;
	private int method;
	private static HashMap<String, Integer> methodCodeTable = new HashMap<String, Integer>(8);
	
	static {
		
		methodCodeTable.put("GET", GET);
		methodCodeTable.put("POST", POST);
		methodCodeTable.put("HEAD", HEAD);
		methodCodeTable.put("PUT", PUT);
		
	}
	
	/**
	 * This prevent direct instaniate of Request object. Request can only be created by RequestParser.
	 */
	protected Request(){}
	
	protected Request(int method, String URI, String httpVersion,
			String parameterString,
			String requestFields){
		this.method = method;
		this.URI = URI;
		this.httpVersion = httpVersion;
		this.parameterString = parameterString;
		this.requestFields = requestFields;
	}
	

	public int getMethod() {
		return method;
	}

	public static int getMethodCode(String method){
		return methodCodeTable.get(method);
	}
	
	public String getURI() {
		return URI;
	}

	public String getParameterString() {
		return parameterString;
	}

	public String getHttpVersion() {
		return httpVersion;
	}
	
	public String getRequestFieldContent(String fieldName){
		return requestFields;
	}
	
	public String replaceWithDocumentRoot(String URI) {
		return HttpdConf.DOCUMENT_ROOT + URI;
	}
}
