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
	private String scriptName;
	private String pathInfo;
	private byte[] parameterByteArray;
	private String remoteUser;

	/**
	 * This prevent direct instaniate of Request object. Request object can only
	 * be created by RequestParser.
	 */
	protected Request() {
	}

	protected Request(String[] parameters, byte[] parameterByteArray,
			Map<String, String> requestFields, String remoteUser, String IP) {
		this.method = parameters[0];
		this.URI = parameters[1];
		this.httpVersion = parameters[2];
		this.parameterString = parameters[3];
		this.pathInfo = parameters[4];
		this.scriptName = parameters[5];
		this.parameterByteArray = parameterByteArray;
		this.remoteUser = remoteUser;
		this.IP = IP;
		this.requestFields = requestFields;

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

	public String getScriptName() {
		return scriptName;
	}

	public String getIPAddr() {
		return IP;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public byte[] getParameterByteArray() {
		return parameterByteArray;
	}
	
	public String getRemoteUser(){
		return remoteUser;
	}
}
