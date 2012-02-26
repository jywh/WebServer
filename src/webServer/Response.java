package webServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import webServer.request.Request;
import webServer.ulti.Log;
import webServer.ulti.LogContent;
import webServer.ulti.ServerException;

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

		if (isPythonScript(document)) {
			System.out.println(document.getAbsolutePath());
			CountableInputStream cin = new PythonScript().execute(document, "");

			// BufferedInputStream bis = new BufferedInputStream(inputStream);
			// Log.debug("", str[0]);
			// Log.debug("temp file name: ", str[1]);
			try {
				int size = cin.size();
				int offset = cin.getOffset('\n');
				String contentType = extractContentType(cin, offset);
				Log.debug("content type", contentType);
				// String contentType = readFirstLineOfInputStream(inputStream);
				// Log.debug("First line of return", reader.readLine());
				String headerMessage = createHeaderMessage(
						request.getHttpVersion(), size, contentType,
						Response.OK);
				writeHeaderMessage(out, headerMessage);
//				writeToFile(cin, size-offset);
//				File test = new File(HttpdConf.TEMP_DIRECTORY, "1330240737041");
//				serveFile(out, test);
				writeStreamFromScriptToBrowser(out, cin, size-offset);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new ServerException(Response.INTERNAL_SERVER_ERROR);
			}

		} else {

			String headerMessage = createHeaderMessage(
					request.getHttpVersion(), document, Response.OK);
			writeHeaderMessage(out, headerMessage);

			if (request.getMethod() != Request.HEAD)
				serveFile(out, document);
			log();
		}
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

		String mime = getMIMEType(document);
		long length = document.length();

		logContent.setStatusCode(statusCode);
		logContent.setLength(length);

		StringBuilder builder = new StringBuilder();
		builder.append(httpVersion).append(" ")
				.append(getStatusPhrase(statusCode)).append("\n")
				.append("Date: ").append(getCurrentTimeFull()).append("\n")
				.append("Server: ").append(WebServer.WEB_SERVER_NAME)
				.append("\n").append("Connection: ").append("close")
				.append("\n").append("Content-length: ").append(length)
				.append("\n").append("Content-type: ").append(mime)
				.append("\n");

//		 System.out.println(builder.toString());
		return builder.toString();

	}

	private String createHeaderMessage(String httpVersion, long length,
			String contentType, int statusCode) {
		StringBuilder builder = new StringBuilder();
		builder.append(httpVersion).append(" ")
				.append(getStatusPhrase(statusCode)).append("\n")
				.append("Date: ").append(getCurrentTimeFull()).append("\n")
				.append("Server: ").append(WebServer.WEB_SERVER_NAME)
				// .append("\n").append("Connection: ").append("close")
				.append("\n").append("Content-length: ").append(length)
				.append("\n").append(contentType).append("\n");
		System.out.println(builder.toString());
		return builder.toString();
	}

	protected void serveFile(OutputStream out, File document)
			throws ServerException {

		Log.debug("document path:", document.getAbsolutePath());

		try {
			byte[] bytes = new byte[(int) document.length()];
			BufferedInputStream inStream = new BufferedInputStream(
					new FileInputStream(document));
			BufferedOutputStream outStream = new BufferedOutputStream(out);
			try {
				inStream.read(bytes, 0, bytes.length);
				outStream.write(bytes, 0, bytes.length);
			} finally {
				inStream.close();
				outStream.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.INTERNAL_SERVER_ERROR,
					"Response: WriteFile");
		}
	}

	protected void writeStreamFromScriptToBrowser(OutputStream out,
			CountableInputStream cin, int size)
			throws ServerException {
		try {
			
//			System.out.println(cin.toString());
			cin.skip(3);
			byte[] bytes = new byte[size-3];
			BufferedOutputStream outStream = new BufferedOutputStream(out);
			cin.read(bytes, 0, bytes.length);
			outStream.write(bytes, 0, bytes.length);

			cin.close();
			out.close();

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

	private String getCurrentTimeFull() {
		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat(
				" EEE, d MMM yyy HH:mm:ss z", Locale.US);
		return dateFormat.format(calendar.getTime());
	}

	private String getMIMEType(File document) {
		return MIME.getMIMEType(getFileExtension(document));
	}

	public static String getFileExtension(File document) {
		String name = document.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			return name.substring(index + 1);
		}
		return "";
	}

	private void log() {
		logContent.setRfc1413("-");
		logContent.setUserId("-");
		logContent.setTime(Log.time());
		Log.access(logContent.getLogContent());
	}

	private boolean isPythonScript(File document) {
		String extension = getFileExtension(document);
		if (extension.equalsIgnoreCase("py"))
			return true;
		return false;
	}

	private String readFirstLineOfInputStream(InputStream in)
			throws ServerException {

		try {
			InputStreamReader reader = new InputStreamReader(in, "UTF-8");
			char c;
			StringBuilder builder = new StringBuilder();
			while ((c = (char) reader.read()) != '\n') {
				builder.append(c);
			}

			while ((c = (char) reader.read()) != '\n') {
				// skip all the white space
			}
			System.out.println(builder.toString());
			return builder.toString();

		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			throw new ServerException(INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(INTERNAL_SERVER_ERROR);
		}

	}

	private String extractContentType(CountableInputStream cin, int offset)
			throws IOException {
		byte[] buf = new byte[offset];
		cin.read(buf, 0, buf.length);
		return new String(buf);
	}
	
	private String writeToFile(CountableInputStream in, int size) throws IOException {
		String name = Long.toString(System.currentTimeMillis());
		File tempDir = new File(HttpdConf.TEMP_DIRECTORY);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		File tempFile = new File(tempDir, name);
		OutputStream out = new FileOutputStream(tempFile);
		in.skip(3);
		byte buf[] = new byte[size-3];
//		int len;
//		while ((len = in.read(buf)) >= 0)
//		
		in.read(buf, 0, buf.length);
		out.write(buf, 0, buf.length);
		out.close();
		in.close();
		
		return name;
	}
}
