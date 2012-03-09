package webServer.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import webServer.constant.HttpdConf;
import webServer.constant.ResponseTable;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

/**
 * 
 * RequestParser is responsiable to parse the incomming request message, and
 * create a request object. All the incomming message must go through
 * RequestParser.
 * 
 */
public class RequestParser {

	public static String TAG = "RequestParser";
	public static final String URI_SEPARATOR = "/";
	private final static Pattern PATTERN = Pattern
			.compile("/([^\\s]+(\\.(?i)(py|pl)))/");

	private BufferedInputStreamReader requestStream;

	public Request parse(InputStream inputStream, String IP)
			throws ServerException {
		if (inputStream == null)
			throw new ServerException(ResponseTable.BAD_REQUEST);

		requestStream = new BufferedInputStreamReader(inputStream);
		try {

			// Parse first line of request message
			String[] parameters = parseFirstLine(requestStream.readLine());
			Map<String, String> headerFields = extractHeaderFields();
			// Read body if it is POST or PUT, otherwise it is an empty array
			byte[] parameterByteArray = extractBodyContent();

			return new Request(parameters, parameterByteArray, headerFields,
					IP);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseRequest");
		}

	}

	/*****************************************************************
	 * Parsing the first line
	 *****************************************************************/

	/**
	 * 
	 * @return String array with elements: {method, resolvedURI, httpversion,
	 *         parameterString, pathInfo, scriptName }
	 */
	protected String[] parseFirstLine(String firstLine) throws ServerException {

		Log.debug(TAG, "firstLine: " + firstLine);
		if (firstLine == null || firstLine.isEmpty())
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseFirstLine");

		String[] tokens = firstLine.split(" ");

		if (tokens.length != 3)
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseFirstLine");

		if (!verifyMethod(tokens[0]))
			throw new ServerException(ResponseTable.NOT_IMPLEMENTED);

		if (tokens[0].equals(Request.PUT)) {
			String URI = HttpdConf.UPLOAD + File.separator + tokens[1];
			return new String[] { tokens[0], URI, tokens[2], "", "", tokens[1] };
		} else {
			String[] newTokens = extractParameterString(tokens[1]);
			String[] anotherTokens = extractPathInfo(newTokens[0]);
			String URI = resolveURI(anotherTokens[0], tokens[0]);
			return new String[] { tokens[0], URI, tokens[2], newTokens[1],
					anotherTokens[1], anotherTokens[0] };
		}

	}

	private boolean verifyMethod(String method) {
		if (method.equals(Request.GET) || method.equals(Request.POST)
				|| method.equals(Request.HEAD) || method.equals(Request.PUT))
			return true;
		return false;

	}

	protected String resolveURI(String URI, String method)
			throws ServerException {

		String newURI = resolveAlias(URI);
		if ( URI.equals(newURI)) // there is no alias
			newURI = addDocumentRoot(URI);

		if (!(new File(newURI)).exists() && !method.equals(Request.PUT))
			throw new ServerException(ResponseTable.NOT_FOUND,
					"RequestParser: addDocumentRoot");
		Log.debug(TAG, "URI: " + URI);
		return newURI;
	}

	/**
	 * This will resolve alias that contains in the URI
	 * 
	 * @throws ServerException
	 */
	protected String resolveAlias(String URI) throws ServerException {

		String[] tokens = URI.split(URI_SEPARATOR);

		if (tokens.length < 1)
			return URI;

		String alias = URI_SEPARATOR + tokens[1];

		if (HttpdConf.SCRIPT_ALIAS.containsKey(alias)) {
			URI = URI.replace(alias, HttpdConf.SCRIPT_ALIAS.get(alias));
		} else if (HttpdConf.ALIAS.containsKey(alias)) {
			URI = URI.replace(alias, HttpdConf.ALIAS.get(alias));
		}
		return URI;
	}

	protected String addDocumentRoot(String URI) throws ServerException {

		Log.debug(TAG, "document root: "+HttpdConf.DOCUMENT_ROOT);
		URI = HttpdConf.DOCUMENT_ROOT + URI;
		File path = new File(URI);

		if (!path.isDirectory() && path.exists()) 
			return URI;

		URI = path.getAbsolutePath() + File.separator;

		for (String indexFile : HttpdConf.DIRECTORY_INDEX) {
			indexFile = URI + indexFile;
			if (new File(indexFile).exists())
				return indexFile;
		}

		throw new ServerException(ResponseTable.NOT_FOUND,
				"RequestParse: addDocumentRoot");

	}

	public String[] extractPathInfo(String URI) {

		String[] tokens = PATTERN.split(URI);
		if (tokens.length < 2)
			return new String[] { URI, "" };

		tokens[1] = URI_SEPARATOR + tokens[1];
		// Add file name to URI.
		tokens[0] = URI.replace(tokens[1], "");
		return tokens;
	}

	/**
	 * This is for GET and HEAD method that carries parameters at URI
	 * 
	 * @param parameters
	 */
	private String[] extractParameterString(String URI) {

		int index = URI.indexOf('?');
		if (index > 0) 
			// there is parameters
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
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"Request: getRequestFields");
		}
	}

	/*************************************************************
	 * Parsing body
	 * @throws IOException 
	 *************************************************************/
	
	
	private byte[] extractBodyContent() throws IOException{
		return requestStream.toByteArray();
	}
	
}