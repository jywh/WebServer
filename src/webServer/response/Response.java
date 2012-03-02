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

import webServer.SecureDirectory;
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
	public static final String ERROR_FILE_PATH = HttpdConf.SERVER_ROOT
			+ "/error/";
	public final static String DEFAULT_HTTP_VERSION = "HTTP/1.1";
	private final static Pattern pattern = Pattern
			.compile("([^\\s]+(\\.(?i)(py|pl)))");
	private final static int NOT_SECURE_DIR = 1;
	private final static int NEED_AUTHENTICATE = 2;
	private final static int AUTHENTICATED = 3;
	private SecureDirectory secureDirectory=null;
	
	public void processRequest(Request request, OutputStream out)
			throws ServerException {

		int statusCode;
		switch (authenticate(request)) {
		case NOT_SECURE_DIR:
			statusCode = processNormalRequest(request, out, true);
			break;
		case NEED_AUTHENTICATE:
			statusCode = sendAuthenticateMessage(request, secureDirectory, out);
			break;
		case AUTHENTICATED:
			statusCode = processNormalRequest(request, out, false);
			break;
		default:
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
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
	private int authenticate(Request request) throws ServerException {

		secureDirectory = checkSecureDirectory(request.getURI());
		if (secureDirectory == null)
			return NOT_SECURE_DIR;

		if (!request.getRequestField().containsKey(HeaderFields.AUTHORIZATION))
			return NEED_AUTHENTICATE;
		
		String auth = request.getRequestField().get(HeaderFields.AUTHORIZATION);
		String[] tokens = auth.split(" ");
		if (tokens[0].equals("Basic")) {
			String decodeText = new String(Base64.decode(tokens[1]));
			if (secureDirectory.getUser().contains(decodeText))
				return AUTHENTICATED;
		}
		throw new ServerException(ResponseTable.FORBIDDEN);
	}

	private SecureDirectory checkSecureDirectory(String uri) {
		Set<String> secureDirectories = HttpdConf.secureUsers.keySet();
		for (String directory : secureDirectories) {
			if (uri.contains(directory))
				return HttpdConf.secureUsers.get(directory);
		}
		return null;
	}

	protected int sendAuthenticateMessage(Request request, SecureDirectory info,
			OutputStream out) throws ServerException {
		String headerMessage = createBasicHeaderMessage(
				request.getHttpVersion(), ResponseTable.UNAUTHORIZED)
				.buildAuthentication(info.getAuthType(), info.getAuthName())
				.toString();
		writeHeaderMessage(out, headerMessage);
		serveFile(out, new File(ERROR_FILE_PATH + "403.html"));
		return ResponseTable.UNAUTHORIZED;
	}

	protected int processNormalRequest(Request request, OutputStream out,
			boolean cached) throws ServerException {
		if (request.getMethod().equals(Request.PUT)) {
			return processPUT(request, out);
		} else if (isScript(request.getURI())) {
			return executeScript(request, out);
		} else {
			return retrieveRegularFile(request, out, cached);
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

	protected int retrieveRegularFile(Request request, OutputStream out,
			boolean cached) throws ServerException {

		File document = new File(request.getURI());
		if (!isModified(request, document)) {
			String headerMessage = createBasicHeaderMessage(
					request.getHttpVersion(), ResponseTable.NOT_MODIFIED)
					.toString();
			writeHeaderMessage(out, headerMessage);
			return ResponseTable.NOT_MODIFIED;
		}

		HeaderBuilder builder = createSimpleHeaderMessage(
				request.getHttpVersion(), ResponseTable.OK, document);
		if (cached)
			builder.buildLastModified(document).buildCacheControl("public");
		writeHeaderMessage(out, builder.toString());
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
			String headerMessage = createBasicHeaderMessage(
					DEFAULT_HTTP_VERSION, statusCode).toString();
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
