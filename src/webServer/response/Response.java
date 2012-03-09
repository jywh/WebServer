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

public class Response {

	public static final String TAG = "Response";
	public static final int BUFFER_SIZE = 2048;
	public static final String ERROR_FILE_PATH = HttpdConf.SERVER_ROOT
			+ "/error/";
	private final static Pattern SCRIPT_PATTERN = Pattern
			.compile("([^\\s]+(\\.(?i)(py|pl)))");

	private final static int NOT_SECURE_DIR = 1;
	private final static int NEED_AUTHENTICATE = 2;
	private final static int AUTHENTICATED = 3;

	public void processRequest(Request request, OutputStream out)
			throws ServerException {
		int statusCode;

		SecureDirectory secureDirectory = getSecureDirectory(request.getURI());
		switch (authenticate(request, secureDirectory)) {
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

	/*************************************************************
	 * Check authentication
	 *************************************************************/

	/**
	 * Check secure directory. Null if not secure directory or authenticate
	 * successfully, otherwise return DirectoryInfo for authentication
	 * 
	 * 
	 * @param request
	 * @return Authentication Code.
	 * @throws ServerException
	 */
	private int authenticate(Request request, SecureDirectory secureDirectory)
			throws ServerException {

		if (secureDirectory == null)
			return NOT_SECURE_DIR;

		if (!request.getRequestField().containsKey(HeaderFields.AUTHORIZATION))
			return NEED_AUTHENTICATE;

		String auth = request.getRequestField().get(HeaderFields.AUTHORIZATION);
		String[] tokens = auth.split(" ");
		if (tokens[0].equals("Basic")) {
			if (secureDirectory.getUser().contains(tokens[1]))
				return AUTHENTICATED;
		}
		// Password or username not correct
		throw new ServerException(ResponseTable.FORBIDDEN);
	}

	private SecureDirectory getSecureDirectory(String uri) {
		Set<String> secureDirectories = HttpdConf.secureUsers.keySet();
		for (String directory : secureDirectories) {
			if (uri.contains(directory))
				return HttpdConf.secureUsers.get(directory);
		}
		return null;
	}

	protected int sendAuthenticateMessage(Request request,
			SecureDirectory info, OutputStream out) throws ServerException {
		String headerMessage = createBasicHeaderMessage(request,
				ResponseTable.UNAUTHORIZED).buildAuthentication(
				info.getAuthType(), info.getAuthName()).toString();
		writeHeaderMessage(out, headerMessage, true);
		return ResponseTable.UNAUTHORIZED;
	}

	/*************************************************************
	 * Process normal request
	 *************************************************************/

	protected int processNormalRequest(Request request, OutputStream out,
			boolean cached) throws ServerException {
		if (request.getMethod().equals(Request.PUT)) {
			return processPUT(request, out);
		} else if (isScript(request.getURI())) {
			return executeScript(request, out);
		} else {
			return retrieveStaticDocument(request, out, cached);
		}

	}

	protected boolean isScript(String URI) {
		File file = new File(URI);
		return SCRIPT_PATTERN.matcher(file.getName()).matches();
	}

	protected int executeScript(Request request, OutputStream outStream)
			throws ServerException {
		CGIOutputStreamReader cin = new CGI().execute(request);
		try {
			int headerStringLen = cin.getHeaderStringSize();
			byte[] content = cin.readBodyContent();
			String headerMessage = createBasicHeaderMessage(request,
					ResponseTable.OK).buildContentLength(content.length-headerStringLen).toString();
			
			BufferedOutputStream out = new BufferedOutputStream(outStream);
			out.write(headerMessage.getBytes());
			out.write(content);
			out.close();
			return ResponseTable.OK;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}

	protected int retrieveStaticDocument(Request request, OutputStream out,
			boolean allowCache) throws ServerException {

		if (request.getMethod().equals(Request.HEAD)) {
			String headerMessage = createBasicHeaderMessage(request,
					ResponseTable.OK).toString();
			writeHeaderMessage(out, headerMessage, true);
			return ResponseTable.OK;
		}

		File document = new File(request.getURI());
		if (!isModified(request, document)) {
			String headerMessage = createBasicHeaderMessage(request,
					ResponseTable.NOT_MODIFIED).toString();
			writeHeaderMessage(out, headerMessage, true);
			return ResponseTable.NOT_MODIFIED;
		}

		String headerMessage = createSimpleHeaderMessage(request,
				ResponseTable.OK, document, allowCache).toString();
			
		writeHeaderMessage(out, headerMessage, false);
		serveFile(out, document);
		return ResponseTable.OK;
	}

	protected boolean isModified(Request request, File file) {
		String dateFromClient = request.getRequestField().get(
				HeaderFields.IF_MODIFIED_SINCE);
		if (dateFromClient == null)
			return true;
		// remove last three significant digits
		long lastModified = (file.lastModified() / 1000L) * 1000L;
		try {
			Date clientDate = (Date) Ulti.DATE_FORMATE.parse(dateFromClient);
			return lastModified > clientDate.getTime();
		} catch (Exception e) {
			// If there is exception, assume file is modified
		}
		return true;
	}

	/**
	 * Since all the files will be uploaded to the same directory, synchronized block will
	 * ensure there is only on thread can write file to UPLOAD directory at
	 * once. It also ensure there won't be multiple threads uploading files with
	 * the same name that the forth one gets overwritten.
	 * 
	 */
	protected int processPUT(Request request, OutputStream outStream)
			throws ServerException {
		int statusCode;
		File document = new File(request.getURI());
		try {
			synchronized (this) {
				if (!document.exists()) {
					BufferedOutputStream out = new BufferedOutputStream(
							new FileOutputStream(document));
					out.write(request.getParameterByteArray());
					statusCode = ResponseTable.CREATED;
					out.close();
				} else {
					statusCode = ResponseTable.NO_CONTENT;
				}
			}
			String headerMessage = createBasicHeaderMessage(request, statusCode)
					.toString();
			writeHeaderMessage(outStream, headerMessage, true);
			return statusCode;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}



	/*************************************************************
	 * Build Header String
	 *************************************************************/

	protected HeaderBuilder createBasicHeaderMessage(Request request,
			int statusCode) {
		HeaderBuilder builder = new HeaderBuilder();
		return builder.buildHeaderBegin(statusCode, request.getHttpVersion())
				.buildConnection("close");
	}

	protected HeaderBuilder createSimpleHeaderMessage(Request request,
			int statusCode, File document, boolean allowCache) {
		HeaderBuilder builder = createBasicHeaderMessage(request, statusCode)
				.buildContentTypeAndLength(document);
		if ( allowCache )
			builder.buildLastModified(document).buildCacheControl("public");
		return builder;

	}

	protected String checkPersistentConnection(Request request) {
		String connection = request.getRequestField().get(
				HeaderFields.CONNECTION);
		return (connection != null) ? connection : "close";

	}

	/*************************************************************
	 * Response to client
	 *************************************************************/

	protected void writeHeaderMessage(OutputStream out, String headerMessage,
			boolean close) {
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(headerMessage);
		if (close)
			writer.close();
	}

	protected void serveFile(OutputStream outStream, File document)
			throws ServerException {

		Log.debug(TAG, "document path:" + document.getAbsolutePath());

		try {
			byte[] buf = new byte[BUFFER_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(document));
			BufferedOutputStream out = new BufferedOutputStream(outStream);
			int read = -1;
			while ((read = in.read(buf)) > -1) {
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
			HeaderBuilder builder = new HeaderBuilder();
			String headerMessage = builder
					.buildHeaderBegin(statusCode, HttpdConf.HTTP_VERSION)
					.buildContentTypeAndLength(errorFile).toString();
			writeHeaderMessage(out, headerMessage, false);
			serveFile(out, errorFile);
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}

}
