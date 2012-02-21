package webServer.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import webServer.HttpdConf;
import webServer.Response;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

/**
 * 
 * RequestParser is responsiable to parse the incomming request message, and
 * create a request object. All the incomming message must go through
 * RequestParser.
 * 
 * @author Wenhui
 * 
 */
public class RequestParser {

	public static final String URI_SEPARATOR = "/";
	private static final String HTTP_PREFIX = "HTTP_";

	private BufferedReader incommingMessage;

	public Request parseRequest(InputStream inputStream) throws ServerException {

		String requestFields;
		String[] parameters;
		int methodCode;

		incommingMessage = new BufferedReader(
				new InputStreamReader(inputStream));

		try {

			// Parse first line of request message
			parameters = parseFirstLine(incommingMessage.readLine());
			methodCode = getMethodCode(parameters[0]);

			if (methodCode == Request.POST || methodCode == Request.PUT)
				parameters[3] = extractParameterStringFromBody();

			requestFields = extractRequestFields();

			Log.log("request field:", requestFields);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"RequestParser: parseRequest");
		}

		return new Request(methodCode, parameters[1], parameters[2],
				parameters[3], requestFields);
	}

	/*****************************************************************
	 * 
	 * Parsing the first line
	 * 
	 *****************************************************************/

	protected String[] parseFirstLine(String firstLine) throws ServerException {

		if (firstLine == null || firstLine.isEmpty())
			throw new ServerException(Response.BAD_REQUEST,
					"RequestParser: parseFirstLine");

		String[] tokens = firstLine.split(" ");

		if (tokens.length != 3) {
			throw new ServerException(Response.BAD_REQUEST,
					"RequestParser: parseFirstLine");
		}

		String methos = tokens[0], URI = tokens[1], httpVersion = tokens[2], parameterString;

		tokens = extractParameterStringFromURI(URI);
		URI = tokens[0];
		parameterString = tokens[1];

		URI = resolveAlias(URI);
		if (!(new File(URI).isAbsolute())) // there is no alias
			URI = addDocumentRoot(URI);

		return new String[] { methos, URI, httpVersion, parameterString };

	}

	/**
	 * 
	 * 
	 * @param method
	 * @return
	 * @throws ServerException
	 */
	protected int getMethodCode(String method) throws ServerException {

		Integer methodCode = Request.getMethodCode(method);

		if (methodCode != null)
			return methodCode;

		throw new ServerException(Response.NOT_IMPLEMENTED);
	}

	/**
	 * This will resolve alias that contains in the URI
	 */
	protected String resolveAlias(String URI) {

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

		Log.log("URI is", URI);
		if (!path.exists())
			throw new ServerException(Response.NOT_FOUND,
					"RequestParser: addDocumentRoot");

		URI = path.getAbsolutePath() + File.separator;

		for (String indexFile : HttpdConf.DIRECTORY_INDEX) {
			indexFile = URI + indexFile;
			if (new File(indexFile).exists())
				return indexFile;
		}

		throw new ServerException(Response.NOT_FOUND,
				"RequestParse: addDocumentRoot");

	}

	/**
	 * This is for GET and HEAD method that carries parameters at URI
	 * 
	 * @param parameters
	 */
	private String[] extractParameterStringFromURI(String URI) {

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

	private String extractRequestFields() throws ServerException {

		try {

			StringBuilder builder = new StringBuilder();
			String currentLine = incommingMessage.readLine();

			while (currentLine != null && !currentLine.trim().isEmpty()) {

				builder.append(convertStringToEnvironmentVaraible(currentLine))
						.append("&");
				currentLine = incommingMessage.readLine();

			}

			return (!builder.toString().isEmpty()) ? builder.toString()
					.substring(0, builder.toString().length() - 1) : "";

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"Request: getRequestFields");
		}
	}

	protected String convertStringToEnvironmentVaraible(String line) {

		String[] tokens = line.split(":", 2);
		tokens[1] = tokens[1] != null ? tokens[1].trim() : "";
		tokens[0] = HTTP_PREFIX + tokens[0].replace('-', '_').toUpperCase();
		return tokens[0] + "=" + tokens[1];

	}

	/*************************************************************
	 * 
	 * Parsing body
	 * 
	 *************************************************************/

	protected String extractParameterStringFromBody() throws ServerException {
		
		try {
			Log.log("request body", "parsing request body");
			String currentLine = incommingMessage.readLine();
			if (currentLine == null)
				return "";

			StringBuilder builder = new StringBuilder();

			while (incommingMessage.ready())
				builder.append((char) incommingMessage.read());

			return builder.toString();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"Request: extractParameterStringFromBody");
		}

	}
}