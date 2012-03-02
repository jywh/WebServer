package webServer.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Pattern;

import webServer.constant.HeaderFields;
import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.ulti.AccessLog;
import webServer.ulti.Log;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

public class Response {

	public static final int BUFFER_SIZE = 2048;
	public static final String ERROR_FILE_PATH = "C:/MyWebserver/error/";
	public static String DEFAULT_HTTP_VERSION = "HTTP/1.1";
	private final static Pattern pattern = Pattern.compile("([^\\s]+(\\.(?i)(py|pl)))");
	
	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		// Retrieve document
		int statusCode;

		if (request.getMethod().equals(Request.PUT)) {
			statusCode = processPUT(request, out);
		} else if (isScript(request.getURI())) {
			statusCode = executeScript(request, out);
		} else {
			statusCode = retrieveRegularFile(request, out);
		}
		new AccessLog().log(request, statusCode);
	}

	protected int executeScript(Request request, OutputStream outStream)
			throws ServerException {
		CGIOutputStreamReader cin = new CGI().execute(request);
		try {
			String headerString = cin.readHeaderString();
			byte[] content = cin.readBodyContent();
			String headerMessage = createHeaderMessageForScript(
					request.getHttpVersion(), ResponseTable.OK, content.length,
					headerString).toString();
			writeHeaderMessage(outStream, headerMessage);
			BufferedOutputStream out = new BufferedOutputStream(outStream);
			out.write(content);
			out.close();
			return ResponseTable.OK;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}

	protected int retrieveRegularFile(Request request, OutputStream out)
			throws ServerException {
		String headerMessage;
		File document = new File(request.getURI());
		if (!isModified(request, document)) {
			headerMessage = createBasicHeaderMessage(request.getHttpVersion(),
					ResponseTable.NOT_MODIFIED).toString();
			writeHeaderMessage(out, headerMessage);
			return ResponseTable.NOT_MODIFIED;
		}

		headerMessage = createHeaderMessage(request.getHttpVersion(),
				ResponseTable.OK, document).toString();
		System.out.println(headerMessage);
		writeHeaderMessage(out, headerMessage);
		serveFile(out, document);
		return ResponseTable.OK;
	}

	protected int processPUT(Request request, OutputStream outStream)
			throws ServerException {
		
		File document = new File(request.getURI());
		if (document.exists())
			throw new ServerException(ResponseTable.NO_CONTENT, "File exist: "
					+ document.getAbsolutePath());
		try {
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(document));
			char[] input = request.getParameterString().toCharArray();
			for (char c : input)
				out.write((int) c);
			out.close();
			String headerMessage = createBasicHeaderMessage(
					request.getHttpVersion(), ResponseTable.FOUND).toString();
			writeHeaderMessage(out, headerMessage);
			return ResponseTable.FOUND;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}

	private boolean isModified(Request request, File file) {
		String dateFromClient = request.getRequestField().get(
				HeaderFields.IF_MODIFIED_SINCE);
		if (dateFromClient == null)
			return true;
		// remove last three significant digits
		long lastModified = (file.lastModified() / 1000) * 1000;
		try {
			Date clientDate = (Date) Ulti.DATE_FORMATE.parse(dateFromClient);
			return lastModified > clientDate.getTime();
		} catch (Exception e) {
			// If there is exception, assume file is modified
		}
		return true;
	}

	protected void writeHeaderMessage(OutputStream out, String headerMessage) {
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(headerMessage);
	}

	protected HeaderBuilder createBasicHeaderMessage(String httpVersion,
			int statusCode) {
		HeaderBuilder builder = new HeaderBuilder();
		return builder.buildHeaderBegin(
				ResponseTable.getResponsePhrase(statusCode), httpVersion)
				.buildConnection(false);
	}

	protected HeaderBuilder createSimpleHeaderMessage(String httpVersion,
			int statusCode, File document) {
		HeaderBuilder builder = createBasicHeaderMessage(httpVersion,
				statusCode);
		return builder.buildContentTypeAndLength(document);

	}

	protected HeaderBuilder createHeaderMessage(String httpVersion,
			int statusCode, File document) {
		HeaderBuilder builder = createSimpleHeaderMessage(httpVersion,
				statusCode, document);
		return builder.buildLastModified(document).buildCacheControl(3600);
	}

	protected HeaderBuilder createHeaderMessageForScript(String httpVersion,
			int statusCode, int length, String headerString) {
		HeaderBuilder builder = createBasicHeaderMessage(httpVersion,
				statusCode);
		return builder.append(headerString).buildContentLength(length);

	}

	protected void serveFile(OutputStream outStream, File document)
			throws ServerException {

		Log.debug("document path:", document.getAbsolutePath());

		try {
			byte[] buf = new byte[BUFFER_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(document));
			BufferedOutputStream out = new BufferedOutputStream(outStream);
			int read = -1;
			while ((read = in.read(buf)) >= 0) {
				out.write(buf, 0, read);
			}
			in.close();
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR,
					"Response: WriteFile");
		}
	}

	public void sendErrorMessage(OutputStream out, int statusCode) {

		try {
			String errorFilePath = ERROR_FILE_PATH
					+ Integer.toString(statusCode) + ".html";
			File errorFile = new File(errorFilePath);
			String headerMessage = createSimpleHeaderMessage(
					DEFAULT_HTTP_VERSION, statusCode, errorFile).toString();
			writeHeaderMessage(out, headerMessage);
			serveFile(out, errorFile);
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}
	
	private boolean isScript(String URI){
		File file = new File(URI);
		return pattern.matcher(file.getName()).matches();
	}

}
