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

import webServer.constant.HeaderFields;
import webServer.constant.HttpdConf;
import webServer.constant.ResponseTable;
import webServer.httpdconfSetter.Directory.SecureDirectory;
import webServer.request.Request;
import webServer.ulti.AccessLog;
import webServer.ulti.Log;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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

	private Request request = null ;
	private OutputStream outStream = null;

	public Response(Request request, OutputStream outStream) {
		this.request = request;
		this.outStream = outStream;
	}

	/**
	 * Process request, produce appropriate response. Check if the URI contain
	 * secure directory, return three options:
	 * 
	 * 1. NOT_SECURE_DIR: contains no secure directory. 2. NEED_AUTHENTICATE:
	 * contains secure directory, need authentication. 3. AUTHENTICATED:
	 * contains secure directory, and has correct password/username.
	 * 
	 * @param request
	 *            A request object created by RequestParser
	 * @param out
	 *            Client OutputStream
	 * @throws ServerException
	 */
	public void processRequest() throws ServerException {
		int statusCode;

		SecureDirectory secureDirectory = getSecureDirectory(request.getURI());
		switch (authenticate(secureDirectory)) {
		case NOT_SECURE_DIR:
			statusCode = processNormalRequest(true);
			break;
		case NEED_AUTHENTICATE:
			statusCode = sendAuthenticateMessage(secureDirectory);
			break;
		case AUTHENTICATED:
			statusCode = processNormalRequest(false);
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
	 * Check if contains secure directory.
	 * 
	 * @param request
	 * @return Authentication Code.
	 * @throws ServerException
	 */
	private int authenticate(SecureDirectory secureDirectory)
			throws ServerException {

		if (secureDirectory == null)
			return NOT_SECURE_DIR;

		if (!request.getRequestField().containsKey(HeaderFields.AUTHORIZATION))
			return NEED_AUTHENTICATE;

		String auth = request.getRequestField().get(HeaderFields.AUTHORIZATION);
		String[] tokens = auth.split(" ", 2);
		if (tokens[0].equals("Basic")) {
			printSecureKey(secureDirectory);
			try {
				String decodedText = new String(Base64.decode(tokens[1]));
				if (secureDirectory.getUser().contains(decodedText))
					return AUTHENTICATED;
			} catch (Base64DecodingException e) {
				e.printStackTrace();
			}
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

	private void printSecureKey(SecureDirectory sd) {
		System.out.println("Secure keys for " + sd.getPath());
		for (String str : sd.getUser()) {
			System.out.println(str);
		}
	}

	protected int sendAuthenticateMessage(SecureDirectory info)
			throws ServerException {
		String headerMessage = createBasicHeaderMessage(
				ResponseTable.UNAUTHORIZED).buildAuthentication(
				info.getAuthType(), info.getAuthName()).toString();
		writeHeaderMessage(headerMessage);
		return ResponseTable.UNAUTHORIZED;
	}

	/*************************************************************
	 * Process normal request
	 *************************************************************/

	protected int processNormalRequest(boolean cached) throws ServerException {
		if (request.getMethod().equals(Request.PUT)) {
			return processPUT();
		} else if (isScript(request.getURI())) {
			return executeScript();
		} else {
			return retrieveStaticDocument(cached);
		}

	}

	/**
	 * Check if the URI is the path to script file.
	 * 
	 * @param URI
	 * @return
	 */
	protected boolean isScript(String URI) {
		File file = new File(URI);
		return SCRIPT_PATTERN.matcher(file.getName()).matches();
	}

	protected int executeScript() throws ServerException {
		CGIOutputStreamReader cin = new CGIHandler().execute(request);
		try {
			int headerStringLen = cin.getHeaderStringSize();
			byte[] content = cin.readBodyContent();
			int contentLength = content.length - headerStringLen;
			String headerMessage = createBasicHeaderMessage(ResponseTable.OK)
					.buildContentLength(contentLength).toString();

			BufferedOutputStream out = new BufferedOutputStream(outStream);
			out.write(headerMessage.getBytes());
			out.write(content);
			cin.close();
			out.flush();

			return ResponseTable.OK;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieve static file from server.
	 * 
	 * @param allowCache
	 * @return
	 * @throws ServerException
	 */
	protected int retrieveStaticDocument(boolean allowCache)
			throws ServerException {

		if (request.getMethod().equals(Request.HEAD)) {
			String headerMessage = createBasicHeaderMessage(ResponseTable.OK)
					.toString();
			writeHeaderMessage(headerMessage);
			return ResponseTable.OK;
		}

		File document = new File(request.getURI());
		if (!isModified(document)) {
			String headerMessage = createBasicHeaderMessage(
					ResponseTable.NOT_MODIFIED).toString();
			writeHeaderMessage(headerMessage);
			return ResponseTable.NOT_MODIFIED;
		}

		String headerMessage = createSimpleHeaderMessage(ResponseTable.OK,
				document, allowCache).toString();

		writeHeaderMessage(headerMessage);
		serveFile(document);
		return ResponseTable.OK;
	}

	protected boolean isModified(File file) {
		String dateFromClient = request.getRequestField().get(
				HeaderFields.IF_MODIFIED_SINCE);
		if (dateFromClient == null)
			return true;
		// Remove last three significant digits, because convert date from
		// String to long lose
		// last three significant digits.
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
	 * Since all the files will be uploaded to the same directory, synchronized
	 * block will ensure that there is only on thread allow to write the file to
	 * UPLOAD directory at once. It also ensure there won't be multiple threads
	 * uploading files with the same name that the previous one gets
	 * overwritten.
	 * 
	 */
	protected int processPUT() throws ServerException {
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
			String headerMessage = createBasicHeaderMessage(statusCode)
					.toString();
			writeHeaderMessage(headerMessage);
			return statusCode;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR);
		}
	}

	/*************************************************************
	 * Build Header String
	 *************************************************************/

	protected HeaderBuilder createBasicHeaderMessage(int statusCode) {
		HeaderBuilder builder = new HeaderBuilder();
		return builder.buildHeaderBegin(statusCode, request.getHttpVersion())
				.buildConnection("close");
	}

	protected HeaderBuilder createSimpleHeaderMessage(int statusCode,
			File document, boolean allowCache) {
		HeaderBuilder builder = createBasicHeaderMessage(statusCode)
				.buildContentTypeAndLength(document);
		if (allowCache && HttpdConf.CACHE_ENABLE)
			builder.buildLastModified(document).buildCacheControl("public");
		return builder;

	}

	protected String checkPersistentConnection() {
		String connection = request.getRequestField().get(
				HeaderFields.CONNECTION);
		return (connection != null) ? connection : "close";

	}

	/*************************************************************
	 * Response to client
	 *************************************************************/

	protected void writeHeaderMessage(String headerMessage) {
		PrintWriter writer = new PrintWriter(outStream, true);
		writer.println(headerMessage);
	}

	protected void serveFile(File document) throws ServerException {

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
			out.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR,
					"Response: WriteFile");
		}
	}

	public void sendErrorMessage(int statusCode) {

		String httpVersion = HttpdConf.HTTP_VERSION;
		if (request != null && request.getHttpVersion() != null)
			httpVersion = request.getHttpVersion();

		try {

			File errorFile = new File(ERROR_FILE_PATH
					+ Integer.toString(statusCode) + ".html");
			HeaderBuilder builder = new HeaderBuilder();
			String headerMessage = builder
					.buildHeaderBegin(statusCode, httpVersion)
					.buildContentTypeAndLength(errorFile).toString();
			writeHeaderMessage(headerMessage);
			serveFile(errorFile);
			
		} catch (ServerException se) {
			se.printStackTrace();
		}

	}

}
