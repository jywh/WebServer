package webServer.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

import webServer.constant.HeaderFields;
import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.ulti.Log;
import webServer.ulti.LogContent;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

public class Response {

	public static final int BUFFER_SIZE = 2048;
	public static final String ERROR_FILE_PATH = "C:/MyWebserver/error/";
	public static String DEFAULT_HTTP_VERSION = "HTTP/1.1";

	private LogContent logContent;

	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		// Retrieve document
		File document = new File(request.getURI());
		logContent = request.getLogContent();

		if (isCGIScript(document)) {
			System.out.println(document.getAbsolutePath());
			executeScript(request, out, document);
		} else {
			retrieveRegularFile(request, out, document);
			// log();
		}
	}

	protected void executeScript(Request request, OutputStream out,
			File document) throws ServerException {
		String[] tokens = new CGI().execute(document,
				request.getParameterString(), request.getRequestField());
		document = new File(tokens[1]);
		Log.debug("content type", tokens[0]);
		String headerMessage = createHeaderMessageForScript(
				request.getHttpVersion(), ResponseTable.OK, document.length(),
				tokens[0]).toString();
		writeHeaderMessage(out, headerMessage);
		serveFile(out, document);
		document.delete();
	}

	protected void retrieveRegularFile(Request request, OutputStream out,
			File document) throws ServerException {
		String headerMessage;

		if (!isModified(request, document)) {
			headerMessage = createBasicHeaderMessage(request.getHttpVersion(),
					ResponseTable.NOT_MODIFIED).toString();
			System.out.println(headerMessage);
			writeHeaderMessage(out, headerMessage);
			return;
		}

		headerMessage = createHeaderMessage(request.getHttpVersion(),
				ResponseTable.OK, document).toString();
		System.out.println(headerMessage);
		writeHeaderMessage(out, headerMessage);
		if (request.getMethod() != Request.HEAD)
			serveFile(out, document);
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
			int statusCode, long length, String contentType) {
		HeaderBuilder builder = createBasicHeaderMessage(httpVersion,
				statusCode);
		return builder.buildContentTypeAndLength(length, contentType);

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

	private void log() {
		logContent.setRfc1413("-");
		logContent.setUserId("-");
		logContent.setTime(Ulti.timeInLogFormat());
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
