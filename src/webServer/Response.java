package webServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import webServer.request.Request;
import webServer.ulti.Log;
import webServer.ulti.ServerException;


public class Response {

	public static final int OK_STATUS_CODE = 200;
	public static final int NO_CONTENT_STATUS_CODE = 204;
	public static final int FOUND_STATUS_CODE = 302;
	public static final int NOT_MODIFIED_STATUS_CODE = 304;
	public static final int BAD_REQUEST_STATUS_CODE = 400;
	public static final int UNAUTHORIZED_STATUS_CODE = 401;
	public static final int FORBIDDEN_STATUS_CODE = 403;
	public static final int NOT_FOUND_STATUS_CODE = 404;
	public static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
	public static final int NOT_IMPLEMENTED_STATUS_CODE = 501;

	private static HashMap<Integer, String> responsePhrase = new HashMap<Integer, String>();
	public static final String ERROR_FILE_PATH = "C:/MyWebserver/error/";
	public static String DEFAULT_HTTP_VERSION = "HTTP/1.1";
	
	static {
		responsePhrase.put(OK_STATUS_CODE, "200 OK");
		responsePhrase.put(NO_CONTENT_STATUS_CODE, "204 No Content");
		responsePhrase.put(FOUND_STATUS_CODE, "302 Found");
		responsePhrase.put(NOT_MODIFIED_STATUS_CODE, "304 Not Modified");
		responsePhrase.put(BAD_REQUEST_STATUS_CODE, "400 Bad Request");
		responsePhrase.put(UNAUTHORIZED_STATUS_CODE, "401 Unauthorized");
		responsePhrase.put(FORBIDDEN_STATUS_CODE, "403 Fobidden");
		responsePhrase.put(NOT_FOUND_STATUS_CODE, "404 Not Found");
		responsePhrase.put(INTERNAL_SERVER_ERROR_STATUS_CODE,
				"500 Internal Server Error");
		responsePhrase.put(NOT_IMPLEMENTED_STATUS_CODE, "501 Not Implemented");
	}

	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		// Retrieve document
		File document = new File(request.getURI());
		
		String headerMessage = createHeaderMessage(request.getHttpVersion(),
				document, Response.OK_STATUS_CODE);
		writeHeaderMessage(out, headerMessage);
		
		if(request.getMethod() != Request.HEAD)
			writeFile(out, document);

	}

	protected void writeHeaderMessage(OutputStream out, String headerMessage) {
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(headerMessage);
	}

	private String getStatusPhrase(Integer statusCode) {
		String phrase = responsePhrase.get(statusCode);
		if( phrase != null )
			return phrase;
		else
			return responsePhrase.get(Response.OK_STATUS_CODE);
	}

	private String createHeaderMessage(String httpVersion, File document,
			int statusCode) {

		String mime = getMIMEType(document);
		long length = document.length();

		StringBuilder builder = new StringBuilder();
		builder.append(httpVersion).append(" ")
				.append(getStatusPhrase(statusCode)).append("\n")
				.append("Date: ").append(getCurrentTimeFull()).append("\n")
				.append("Server: ").append(WebServer.WEB_SERVER_NAME)
//				.append("\n").append("Connection: ").append("close")
				.append("\n").append("Content-length: ").append(length)
				.append("\n").append("Content-type: ").append(mime)
				.append("\n");

		System.out.println(builder.toString());
		return builder.toString();

	}

	protected void writeFile(OutputStream out, File document)
			throws ServerException {
		
		Log.log("document path:", document.getAbsolutePath());
		
		try {
			byte[] bytes = new byte[(int) document.length()];
			BufferedInputStream inStream = new BufferedInputStream(
					new FileInputStream(document));
			BufferedOutputStream outStream = new BufferedOutputStream(out);
			inStream.read(bytes, 0, bytes.length);
			outStream.write(bytes, 0, bytes.length);
			outStream.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(
					Response.INTERNAL_SERVER_ERROR_STATUS_CODE,
					"Response: WriteFile");
		}
	}

	public void writeErrorMessage(OutputStream out, int statusCode)
			throws IOException {
		try {
			String errorFilePath = ERROR_FILE_PATH+Integer.toString(statusCode)+".html";
			Log.log("Error file", errorFilePath);
			File errorFile = new File(errorFilePath);
			String headerMessage = createHeaderMessage(DEFAULT_HTTP_VERSION, errorFile,
					statusCode);
			writeHeaderMessage(out, headerMessage);
			writeFile(out, errorFile);
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}

	public String getCurrentTimeFull() {
		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat(
				" EEE, d MMM yyy HH:mm:ss z", Locale.US);
		return dateFormat.format(calendar.getTime());
	}

	public String getMIMEType(File document) {
		String extension = document.getName();
		int index = extension.lastIndexOf('.');
		if (index > 0) {
			extension = extension.substring(index + 1);
			return MIME.getMIMEType(extension);
		}
		return MIME.DEFAULT_MIME_TYPE;
	}
}
