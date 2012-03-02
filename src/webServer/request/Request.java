package webServer.request;

import java.util.Map;

import webServer.constant.HttpdConf;

public class Request {

	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String HEAD = "HEAD";
	public static final String PUT = "PUT";

	private Map<String, String> requestFields;
	private String httpVersion, URI, parameterString;
	private String method;
	private String IP;
	private int remotePort;
	private String scriptName;
	private String pathInfo;
	
	/**
	 * This prevent direct instaniate of Request object. Request object can only be
	 * created by RequestParser.
	 */
	protected Request() {
	}

	protected Request(String method, String URI, String httpVersion,
			String parameterString, String pathInfo, String scriptName, Map<String, String> requestFields, 
			String IP, int remotePort) {
		this.method = method;
		this.URI = URI;
		this.httpVersion = httpVersion;
		this.parameterString = parameterString;
		this.requestFields = requestFields;
		this.scriptName = scriptName;
		this.IP = IP;
		this.remotePort = remotePort;
		this.pathInfo = pathInfo;
	}

	public String getMethod() {
		return method;
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

	public Map<String, String> getRequestField() {
		return requestFields;
	}

	public String replaceWithDocumentRoot(String URI) {
		return HttpdConf.DOCUMENT_ROOT + URI;
	}

	public String getScriptName(){
		return scriptName;
	}
	
	public String getIPAddr(){
		return IP;
	}
	
	public int getRemotePort(){
		return remotePort;
	}
	
	public String getPathInfo(){
		return pathInfo;
	}

}
