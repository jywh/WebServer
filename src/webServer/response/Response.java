package webServer.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import webServer.request.Request;
import webServer.ulti.Log;
import webServer.ulti.LogContent;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

public class Response {

	public static final int OK = 200;
	public static final int NO_CONTENT = 204;
	public static final int FOUND = 302;
	public static final int NOT_MODIFIED = 304;
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int NOT_IMPLEMENTED = 501;

	public static final int BUFFER_SIZE = 2048;
	private static HashMap<Integer, String> responsePhrase = new HashMap<Integer, String>();
	public static final String ERROR_FILE_PATH = "C:/MyWebserver/error/";
	public static String DEFAULT_HTTP_VERSION = "HTTP/1.1";


	private LogContent logContent;

	static {
		responsePhrase.put(OK, "200 OK");
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

	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		// Retrieve document
		File document = new File(request.getURI());
		logContent = request.getLogContent();

		if (isCGIScript(document)) {
			System.out.println(document.getAbsolutePath());
			executeScript(document, request, out);
		} else {
			retrieveRegularFile(document, request, out);
			// log();
		}
	}

	protected void executeScript(File document, Request request,
			OutputStream out) throws ServerException {
		String[] tokens = new CGI().execute(document,
				request.getParameterString(), request.getRequestField());
		document = new File(tokens[1]);
		Log.debug("content type", tokens[0]);
		String headerMessage = createHeaderMessage(request.getHttpVersion(),
				document.length(), tokens[0], Response.OK);
		writeHeaderMessage(out, headerMessage);
		serveFile(out, document);
		document.delete();
	}

	protected void retrieveRegularFile(File document, Request request, OutputStream out)
			throws ServerException {
		String headerMessage = createHeaderMessage(request.getHttpVersion(),
				document, Response.OK);
		writeHeaderMessage(out, headerMessage);

		if (request.getMethod() != Request.HEAD)
			serveFile(out, document);
	}

	protected void writeHeaderMessage(OutputStream out, String headerMessage) {
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(headerMessage);
	}

	private String getStatusPhrase(Integer statusCode) {
		String phrase = responsePhrase.get(statusCode);
		if (phrase != null)
			return phrase;
		else
			return responsePhrase.get(Response.OK);
	}

	private String createHeaderMessage(String httpVersion, File document,
			int statusCode) {
		HeaderBuilder builder = new HeaderBuilder();
		builder.buildHeaderBegin(getStatusPhrase(statusCode), httpVersion)
				.buildConnection(false).buildContentTypeAndLength(document)
				.buildLastModified(document).buildCacheControl(3600000);
		
		System.out.println(builder.toString());
		return builder.toString();
	}

	private String createHeaderMessage(String httpVersion, long length,
			String contentType, int statusCode) {
		HeaderBuilder builder = new HeaderBuilder();
		builder.buildHeaderBegin(getStatusPhrase(statusCode), httpVersion)
				.buildConnection(false).buildContentTypeAndLength(length, contentType);
		System.out.println(builder.toString());
		return builder.toString();
	}

	protected void serveFile(OutputStream outStream, File document)
			throws ServerException {

		Log.debug("document path:", document.getAbsolutePath());

		try {
			byte[] buf = new byte[BUFFER_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(document));
			BufferedOutputStream out = new BufferedOutputStream(outStream);
			try {
				int read = -1;
				while ((read = in.read(buf)) >= 0) {
					out.write(buf, 0, read);
				}
			} finally {
				in.close();
				out.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.INTERNAL_SERVER_ERROR,
					"Response: WriteFile");
		}
	}

	public void sendErrorMessage(OutputStream out, int statusCode) {

		try {
			String errorFilePath = ERROR_FILE_PATH
					+ Integer.toString(statusCode) + ".html";
			File errorFile = new File(errorFilePath);
			String headerMessage = createHeaderMessage(DEFAULT_HTTP_VERSION,
					errorFile, statusCode);
			writeHeaderMessage(out, headerMessage);
			serveFile(out, errorFile);
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}

	private void log() {
		logContent.setRfc1413("-");
		logContent.setUserId("-");
		logContent.setTime(Log.time());
		Log.access(logContent.getLogContent());
	}

	private boolean isCGIScript(File document) {
		String extension = Ulti.getFileExtension(document);
		if (extension.equalsIgnoreCase("py")
				|| extension.equalsIgnoreCase("pl"))
			return true;
		return false;
	}

}
