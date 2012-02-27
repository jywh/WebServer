package webServer.request;

import java.util.Map;

import webServer.constant.HttpdConf;
import webServer.ulti.LogContent;

public class Request {

	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String HEAD = "HEAD";
	public static final String PUT = "PUT";

	private Map<String, String> requestFields;
	private String httpVersion, URI, parameterString;
	private String method;
	private LogContent logContent;


	/**
	 * This prevent direct instaniate of Request object. Request can only be
	 * created by RequestParser.
	 */
	protected Request() {
	}

	protected Request(String method, String URI, String httpVersion,
			String parameterString, Map<String,String> requestFields, LogContent logContent) {
		this.method = method;
		this.URI = URI;
		this.httpVersion = httpVersion;
		this.parameterString = parameterString;
		this.requestFields = requestFields;
		this.logContent = logContent;
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
	
	public LogContent getLogContent(){
		return logContent;
	}

}
