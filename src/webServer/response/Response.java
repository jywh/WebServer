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
import java.util.Set;
import java.util.regex.Pattern;

import webServer.DirectoryInfo;
import webServer.constant.HeaderFields;
import webServer.constant.HttpdConf;
import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.ulti.AccessLog;
import webServer.ulti.Log;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class Response {

	public static final int BUFFER_SIZE = 2048;
	public static final String ERROR_FILE_PATH = HttpdConf.SERVER_ROOT+"/error/";
	public static String DEFAULT_HTTP_VERSION = "HTTP/1.1";
	private final static Pattern pattern = Pattern
			.compile("([^\\s]+(\\.(?i)(py|pl)))");

	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		int statusCode;
		DirectoryInfo info = authenticate(request);
		if (info == null) {
			System.out.println("pass authentication!");
			statusCode = processNormalRequest(request, out);
		} else {
			statusCode = sendAuthenticateMessage(request, info, out);
		}

		new AccessLog().log(request, statusCode);
	}

	/**
	 * Check secure directory.
	 * 
	 * 
	 * @param request
	 * @return Null if not secure directory or authenticate successfully,
	 *         otherwise return DirectoryInfo for authentication
	 * @throws ServerException
	 */
	private DirectoryInfo authenticate(Request request) throws ServerException {

		DirectoryInfo info = checkSecureDirectory(request.getURI());
		if (info == null)
			return null;

		if (!request.getRequestField().containsKey(HeaderFields.AUTHORIZATION))
			return info;

		String auth = request.getRequestField().get(HeaderFields.AUTHORIZATION);
		String[] tokens = auth.split(" ");
		if (tokens[0].equals("Basic")) {
			String decodeText = new String(Base64.decode(tokens[1]));
			if (info.getUser().contains(decodeText))
				return null;
		}
//		throw new ServerException(ResponseTable.FORBIDDEN);
		return info;
	}

	private DirectoryInfo checkSecureDirectory(String uri) {
		Set<String> secureDirectories = HttpdConf.secureUsers.keySet();
		for (String directory : secureDirectories) {
			if (uri.contains(directory))
				return HttpdConf.secureUsers.get(directory);
		}
		return null;
	}

	protected int sendAuthenticateMessage(Request request, DirectoryInfo info,
			OutputStream out) throws ServerException {
		String headerMessage = createBasicHeaderMessage(
				request.getHttpVersion(), ResponseTable.UNAUTHORIZED)
				.buildAuthentication(info.getAuthType(), info.getAuthName())
				.toString();
		writeHeaderMessage(out, headerMessage);
		serveFile(out, new File(request.getURI()));
		return ResponseTable.UNAUTHORIZED;
	}

	protected int processNormalRequest(Request request, OutputStream out)
			throws ServerException {
		if (request.getMethod().equals(Request.PUT)) {
			return processPUT(request, out);
		} else if (isScript(request.getURI())) {
			return executeScript(request, out);
		} else {
			return retrieveRegularFile(request, out);
		}

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

		headerMessage = createHeaderMessageCache(request.getHttpVersion(),
				ResponseTable.OK, document, "public").toString();
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

	protected HeaderBuilder createHeaderMessageCache(String httpVersion,
			int statusCode, File document, String cache) {
		HeaderBuilder builder = createSimpleHeaderMessage(httpVersion,
				statusCode, document);
		return builder.buildLastModified(document).buildCacheControl(cache);
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
			File errorFile = new File(ERROR_FILE_PATH
					+ Integer.toString(statusCode) + ".html");
			String headerMessage = createSimpleHeaderMessage(
					DEFAULT_HTTP_VERSION, statusCode, errorFile).toString();
			writeHeaderMessage(out, headerMessage);
			serveFile(out, errorFile);
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}

	private boolean isScript(String URI) {
		File file = new File(URI);
		return pattern.matcher(file.getName()).matches();
	}

}
