package webServer.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import webServer.constant.HeaderFields;
import webServer.constant.HttpdConf;
import webServer.constant.ResponseTable;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * <p>
 * RequestParser is responsiable for parsing the incomming request stream, and create a Request object.
 * </p>
 */
public class RequestParser {

	public static final String TAG = "RequestParser";
	public static final String URI_SEPARATOR = "/";
	private static final Pattern SCRIPT_PATTERN = Pattern.compile("/([^\\s]+(\\.(?i)(py|pl)))/");

	private ByteInputStreamReader requestStream;

	public RequestParser(InputStream inputStream) throws ServerException {
		if (inputStream == null)
			throw new ServerException(ResponseTable.BAD_REQUEST);

		this.requestStream = new ByteInputStreamReader(inputStream);
	}

	/**
	 * Parse request stream.
	 * 
	 * @param IP
	 *            Client IP address.
	 * @return A Request object.
	 * @throws ServerException
	 */
	public Request parse(String IP) throws ServerException {
		try {

			String[] parameters = parseFirstLine(requestStream.readLine());
			Map<String, String> headerFields = extractHeaderFields();

			byte[] parameterByteArray = null;
			if (parameters[0].equals(Request.POST) || parameters[0].equals(Request.PUT))
				parameterByteArray = extractBodyContent(headerFields.get(HeaderFields.CONTENT_LENGTH));

			String remoteUser = getRemoteUser(headerFields);

			return new Request(parameters, parameterByteArray, headerFields, remoteUser, IP);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST, "RequestParser: parse()");
		}

	}

	/*****************************************************************
	 * Parsing the first line
	 *****************************************************************/

	/**
	 * Parse the first line of http request.
	 * 
	 * @return String array with elements: {method, resolvedURI, httpversion, parameterString, pathInfo,
	 *         scriptName }
	 */
	protected String[] parseFirstLine(String firstLine) throws ServerException {

		Log.debug(TAG, "firstLine: " + firstLine);
		if (firstLine == null || firstLine.isEmpty())
			throw new ServerException(ResponseTable.BAD_REQUEST, "parseFirstLine");

		String[] tokens = firstLine.split(" ");

		if (tokens.length != 3)
			throw new ServerException(ResponseTable.BAD_REQUEST, "parseFirstLine");

		if (!verifyMethod(tokens[0]))
			throw new ServerException(ResponseTable.NOT_IMPLEMENTED);

		String[] result;
		if (tokens[0].equals(Request.PUT)) {
			String URI = (tokens[1].startsWith(URI_SEPARATOR)) ? tokens[1] : URI_SEPARATOR + tokens[1];
			URI = HttpdConf.UPLOAD + URI;
			result = new String[] { tokens[0], URI, tokens[2], "", "", tokens[1] };
		} else {
			String[] newTokens = extractParameterString(tokens[1]);
			String[] anotherTokens = extractPathInfo(newTokens[0]);
			String URI = resolveURI(anotherTokens[0], tokens[0]);
			result = new String[] { tokens[0], URI, tokens[2], newTokens[1], anotherTokens[1],
					anotherTokens[0] };
		}
		return result;

	}

	private boolean verifyMethod(String method) {
		if (method.equals(Request.GET) || method.equals(Request.POST) || method.equals(Request.HEAD)
				|| method.equals(Request.PUT))
			return true;
		return false;

	}

	protected String resolveURI(String URI, String method) throws ServerException {

		String newURI = resolveAlias(URI);
		if (URI.equals(newURI)) // there is no alias
			newURI = addDocumentRoot(URI);

		Log.debug(TAG, "URI: " + newURI);

		if (!(new File(newURI)).exists() && !method.equals(Request.PUT))
			throw new ServerException(ResponseTable.NOT_FOUND, "resolveURI");

		return newURI;
	}

	/**
	 * Resolve alias that contain in the URI, if URI contains no alias, return the original URI.
	 * 
	 * @throws ServerException
	 */
	protected String resolveAlias(String URI) throws ServerException {

		String[] tokens = URI.split(URI_SEPARATOR);

		if (tokens.length < 2)
			return URI;

		String alias = URI_SEPARATOR + tokens[1];

		if (HttpdConf.SCRIPT_ALIAS.containsKey(alias)) {

			URI = URI.replace(alias, HttpdConf.SCRIPT_ALIAS.get(alias));

		} else if (HttpdConf.ALIAS.containsKey(alias)) {

			URI = URI.replace(alias, HttpdConf.ALIAS.get(alias));

		}

		return URI;
	}

	/**
	 * Add document root when a URI contains no alais.
	 * 
	 * @param URI
	 * @return
	 * @throws ServerException
	 */
	protected String addDocumentRoot(String URI) throws ServerException {

		URI = HttpdConf.DOCUMENT_ROOT + URI;
		File path = new File(URI);

		// If URI is the path to a file and the file exist, no need to look for index file
		if (!path.isDirectory() && path.exists())
			return URI;

		URI = path.getAbsolutePath() + File.separator;
		for (String indexFile : HttpdConf.DIRECTORY_INDEX) {
			indexFile = URI + indexFile;
			if (new File(indexFile).exists())
				return indexFile;
		}

		throw new ServerException(ResponseTable.NOT_FOUND, "addDocumentRoot");

	}

	/**
	 * Extract script path info.
	 * 
	 * @param URI
	 * @return Script path info, empty if there is no path info.
	 */
	private String[] extractPathInfo(String URI) {

		// Use script file name as delimiter to separate URI.
		String[] tokens = SCRIPT_PATTERN.split(URI);
		if (tokens.length < 2) // Contain no script
			return new String[] { URI, "" };

		tokens[1] = URI_SEPARATOR + tokens[1];
		// Add file name back URI.
		tokens[0] = URI.replace(tokens[1], "");
		return tokens;
	}

	/**
	 * Extract GET and HEAD method that carry parameters at URI.
	 * 
	 * @param parameters
	 */
	private String[] extractParameterString(String URI) {

		int index = URI.indexOf('?');
		if (index > 0)
			// there is parameter string
			return URI.split("\\?");

		return new String[] { URI, "" };

	}

	/*************************************************************
	 * Parsing header fields
	 *************************************************************/

	private Map<String, String> extractHeaderFields() throws ServerException {

		try {
			Map<String, String> headers = new HashMap<String, String>();
			String currentLine = requestStream.readLine();
			String[] tokens;
			while ((currentLine != null) && !(currentLine.trim().isEmpty())) {
				tokens = currentLine.split(":", 2);
				headers.put(tokens[0], tokens[1].trim());
				currentLine = requestStream.readLine();
			}

			return headers;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST, "getHeaderFields");
		}
	}

	/**
	 * Get REMOTE_USER from AUTHORIZATION fiels. REMOTE_USER is used by CGI script.
	 * 
	 * @param headerFields
	 * @return
	 */
	private String getRemoteUser(Map<String, String> headerFields) {
		String encodedText = headerFields.get(HeaderFields.AUTHORIZATION);
		if (encodedText == null)
			return "";

		String[] tokens = encodedText.split(" +", 2);
		if (!tokens[0].equals("Basic"))
			// Right now, this web server only supports Basic encryption
			return "";

		try {
			String decodedText = new String(Base64.decode(tokens[1]));
			tokens = decodedText.split(":", 2);
			String remoteUser = (tokens[0] != null) ? tokens[0] : "";
			return remoteUser;
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*************************************************************
	 * Parsing body
	 *************************************************************/

	/**
	 * Extract request body, if request comes with content-length, then extract exactly the size of
	 * content-length, otherwise, extract all the avaiable bytes.
	 * 
	 * @param contentLength
	 *            The size of request body.
	 * @return ByteArray of request body.
	 * 
	 */
	private byte[] extractBodyContent(String contentLength) throws IOException {
		byte[] bytes;
		if (contentLength != null)
			bytes = requestStream.toByteArray(Integer.parseInt(contentLength));
		else
			bytes = requestStream.toByteArray();
		return bytes;
	}

}