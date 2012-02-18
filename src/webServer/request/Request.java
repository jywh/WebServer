 package webServer.request;

import java.util.HashMap;
import java.util.Set;

import webServer.HttpdConf;

public class Request {

	public static final int GET = 0;
	public static final int POST = 1;
	public static final int HEAD = 2;
	public static final int PUT = 3;
	
	private HashMap<String, String> variables, requestFields;
	private String httpVersion,URI;
	private int method;

	/**
	 * This prevent direct instaniate of Request object. Request can only be created by RequestParser.
	 */
	protected Request(){}
	
	protected Request(int method, String URI, String httpVersion,
			HashMap<String, String> variables,
			HashMap<String, String> requestFields){
		this.method = method;
		this.URI = URI;
		this.httpVersion = httpVersion;
		this.variables = variables;
		this.requestFields = requestFields;
	}
	
	protected void setRequestParameters(int method, String URI, String httpVersion,
			HashMap<String, String> variables,
			HashMap<String, String> requestFields) {
		this.method = method;
		this.URI = URI;
		this.httpVersion = httpVersion;
		this.variables = variables;
		this.requestFields = requestFields;
	}

	public int getMethod() {
		return method;
	}

	public String getURI() {
		return URI;
	}

	public String getVariables(String var) {
		return variables.get(var);
	}

	public String getHttpVersion() {
		return httpVersion;
	}
	
	public Set<String> getAllRequestFieldNames(){
		return requestFields.keySet();
	}
	
	public String getRequestFieldContent(String fieldName){
		return requestFields.get(fieldName);
	}
	
	public String replaceWithDocumentRoot(String URI) {
		return HttpdConf.DOCUMENT_ROOT + URI;
	}
}
