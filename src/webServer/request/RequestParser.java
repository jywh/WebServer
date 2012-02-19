package webServer.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import webServer.HttpdConf;
import webServer.Response;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

/**
 * RequestParser is responsiable to parse the incomming request message, and
 * create a request object. All the incomming message must go through
 * RequestParser.
 * 
 * @author Wenhui
 * 
 */
public class RequestParser {

	public static final String URI_SEPARATOR = "/";
	private BufferedReader incommingMessage;

	public Request parseRequest(InputStream inputStream) throws ServerException {
		HashMap<String, String> requestFields;
		String[] parameters;

		try {

			incommingMessage = new BufferedReader(new InputStreamReader(
					inputStream));

			// Parse first line of request message
			parameters = parseFirstLine(incommingMessage.readLine());

			requestFields = getRequestFields();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"RequestParser: parseRequest");
		}

		return new Request(getRequestMethodCode(parameters[0]), parameters[1],
				parameters[2], parameters[3], requestFields);
	}

	/*****************************************************************
	 * 
	 * Parsing the first line of request message
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

		String methos = tokens[0],  URI = tokens[1],  httpVersion = tokens[2], parameterString;

		tokens = extractParameterString(URI);
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
	protected int getRequestMethodCode(String method) throws ServerException {
		if (method.equals("GET"))
			return Request.GET;
		else if (method.equals("POST"))
			return Request.POST;
		else if (method.equals("HEAD"))
			return Request.HEAD;
		else if (method.equals("PUT"))
			return Request.PUT;
		else
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
	private String[] extractParameterString(String URI) {

		int index = URI.indexOf('?');
		if (index > 0) {
			// there is parameters
			return URI.split("\\?");
		}
		return new String[] { URI, null };

	}

	/*************************************************************
	 * 
	 * Parsing header field
	 * 
	 *************************************************************/

	private HashMap<String, String> getRequestFields() throws ServerException {
		try {
			String currentLine = incommingMessage.readLine();
			HashMap<String, String> requestFields = new HashMap<String, String>(
					40);
			while (currentLine.trim().length() != 0) {
				try {
					String[] tokens = currentLine.split(":");
					requestFields.put(tokens[0], tokens[1].trim());
					currentLine = incommingMessage.readLine();
				} catch (NullPointerException npe) {
					npe.printStackTrace();
					throw new ServerException(Response.BAD_REQUEST,
							"Request: getRequestFields");
				}
			}
			return requestFields;
		} catch (IOException ioe) {
			throw new ServerException(Response.BAD_REQUEST,
					"Request: getRequestFields");
		}
	}

	/*************************************************************
	 * 
	 * Parsing body
	 * 
	 *************************************************************/

}
