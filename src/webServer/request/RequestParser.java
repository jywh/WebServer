package webServer.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	private BufferedReader incommingMessage;

	public Request parse(InputStream inputStream, String IP, int remotePort)
			throws ServerException {
		if (inputStream == null)
			throw new ServerException(ResponseTable.BAD_REQUEST);

		incommingMessage = new BufferedReader(
				new InputStreamReader(inputStream));
		try {

			// debug(incommingMessage);
			// Parse first line of request message
			String[] parameters = parseFirstLine(incommingMessage.readLine());
			Map<String, String> requestFields = extractRequestFields();

			if (parameters[0].equals(Request.POST)
					|| parameters[0].equals(Request.PUT)) {
				parameters[3] = extractParameterStringFromBody();
				Log.debug(TAG, "POST parameter " + parameters[3]);
			}
			return new Request(parameters[0], parameters[1], parameters[2],
					parameters[3], parameters[4], parameters[5], requestFields,
					IP, remotePort);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseRequest");
		}

	}

	/*****************************************************************
	 * 
	 * Parsing the first line
	 * 
	 *****************************************************************/

	protected String[] parseFirstLine(String firstLine) throws ServerException {

		Log.debug(TAG, "firstLine: " + firstLine);
		if (firstLine == null || firstLine.isEmpty())
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseFirstLine");

		String[] tokens = firstLine.split(" ");

		if (tokens.length != 3) {
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"RequestParser: parseFirstLine");
		}
		System.out.println("Method: "+tokens[0]);
		if (!verifyMethod(tokens[0]))
			throw new ServerException(ResponseTable.NOT_IMPLEMENTED);

		if (tokens[0].equals(Request.PUT)) {
			String URI = HttpdConf.UPLOAD+File.separator+tokens[1];
			Log.debug(TAG, "Upload path: "+URI);
			return new String[] {tokens[0], URI, tokens[2], "", "", tokens[1]};
		} else {

			String[] newTokens = extractParameterString(tokens[1]);
			String[] anotherTokens = extractPathInfo(newTokens[0]);
			String URI = resolveURI(anotherTokens[0], tokens[0]);
			// return {method, resolvedURI, httpversion, parameterString,
			// pathInfo, scriptName}
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

		URI = resolveAlias(URI);
		if (!(new File(URI).isAbsolute())) // there is no alias
			URI = addDocumentRoot(URI);

		if (!(new File(URI)).exists() && !method.equals(Request.PUT))
			throw new ServerException(ResponseTable.NOT_FOUND,
					"RequestParser: addDocumentRoot");
		Log.debug(TAG, "URI: " + URI);
		return URI;
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

		URI = HttpdConf.DOCUMENT_ROOT + URI;
		File path = new File(URI);

		if (!path.isDirectory() && path.exists()) {
			return URI;
		}

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
		if (index > 0) {
			// there is parameters
			return URI.split("\\?");
		}
		return new String[] { URI, "" };

	}

	/*************************************************************
	 * 
	 * Parsing header field
	 * 
	 *************************************************************/

	private Map<String, String> extractRequestFields() throws ServerException {

		try {
			String currentLine = incommingMessage.readLine();
			Map<String, String> headers = new HashMap<String, String>();
			String[] tokens;
			while ((currentLine != null) && !(currentLine.trim().isEmpty())) {
				tokens = currentLine.split(":", 2);
				headers.put(tokens[0], tokens[1].trim());
				currentLine = incommingMessage.readLine();
			}

			return headers;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"Request: getRequestFields");
		}
	}

	/*************************************************************
	 * 
	 * Parsing body
	 * 
	 *************************************************************/

	/**
	 * 
	 */
	protected String extractParameterStringFromBody() throws ServerException {

		try {
			System.out.println("Read body: ");
			// return debug(incommingMessage);
			StringBuilder builder = new StringBuilder();
			while (incommingMessage.ready()) {
				// System.out.print((char)incommingMessage.read());
				builder.append((char) incommingMessage.read());
			}
			System.out.println(builder.toString());
			return builder.toString();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(ResponseTable.BAD_REQUEST,
					"Request: extractParameterStringFromBody");
		}

	}

	public String debug(BufferedReader reader) throws IOException {
		String line;
		StringBuilder builder = new StringBuilder();
		// while (reader.ready()) {
		// builder.append((char) reader.read());
		// }
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		System.out.println("debug put");
		System.out.println(builder.toString());
		return builder.toString();
	}
}