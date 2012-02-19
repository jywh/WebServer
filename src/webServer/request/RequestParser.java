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

	public Request parseRequest(InputStream inputStream) throws ServerException {
		String currentLine;
		HashMap<String, String> requestFields, variables;
		int method;
		String[] parameters;

		try {

			BufferedReader incommingMessage = new BufferedReader(
					new InputStreamReader(inputStream));

			currentLine = incommingMessage.readLine();
			// Here should throws exception
			if (currentLine == null) {
				throw new ServerException(Response.BAD_REQUEST_STATUS_CODE,
						"RequestParser: empty header message");
			}

			parameters = parseFirstLine(currentLine);
			method = getRequestMethodCode(parameters[0]);
			variables = getVariablesFromURI(parameters[1]);

			// save all the request fields to HashMap
			requestFields = getRequestFields(incommingMessage);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST_STATUS_CODE,
					"RequestParser: parseRequest");
		}

		return new Request(method, parameters[1], parameters[2], variables,
				requestFields);
	}

	/*****************************************************************
	 * 
	 * Parsing the first line of request message
	 * 
	 *****************************************************************/

	protected String[] parseFirstLine(String firstLine) throws ServerException {

		String[] tokens = firstLine.split(" ");
		if (tokens.length == 0 || tokens.length < 3) {
			throw new ServerException(Response.BAD_REQUEST_STATUS_CODE,
					"RequestParser: parseFirstLine");
		}

		tokens[1] = resolveAlias(tokens[1]);
		if (!(new File(tokens[1]).isAbsolute())) // there is no alias
			tokens[1] = addDocumentRoot(tokens[1]);
		return tokens;

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
			throw new ServerException(Response.NOT_IMPLEMENTED_STATUS_CODE);
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
		File uri = new File(URI);

		if (!uri.isDirectory() && uri.exists()) {
			return URI;
		}

		Log.log("URI is", URI);
		if (!uri.exists())
			throw new ServerException(Response.NOT_FOUND_STATUS_CODE,
					"RequestParser: addDocumentRoot");

		for (String indexFile : HttpdConf.DIRECTORY_INDEX) {
			URI = uri.getAbsolutePath() + File.separator + indexFile;
			if (new File(URI).exists())
				return URI;
		}

		throw new ServerException(Response.NOT_FOUND_STATUS_CODE,
				"RequestParse: addDocumentRoot");

	}

	/**
	 * This is for GET and HEAD method that carries parameters at URI
	 * 
	 * @param parameters
	 */
	private HashMap<String, String> getVariablesFromURI(String URI) {

		HashMap<String, String> variables = new HashMap<String, String>(20);
		String[] tokens = URI.split("\\?");
		if (tokens.length > 1) {
			String[] variablePairs = tokens[1].split("&");
			String[] pairs;

			for (String variable : variablePairs) {
				pairs = variable.split("=");
				variables.put(pairs[0], pairs[1]);
			}
		}
		return variables;

	}

	/*************************************************************
	 * 
	 * Parsing header field
	 * 
	 *************************************************************/

	private HashMap<String, String> getRequestFields(BufferedReader body)
			throws ServerException {
		try {
			String currentLine = body.readLine();
			HashMap<String, String> requestFields = new HashMap<String, String>(
					40);
			while (currentLine.trim().length() != 0) {
				try {
					String[] tokens = currentLine.split(":");
					requestFields.put(tokens[0], tokens[1].trim());
					currentLine = body.readLine();
				} catch (NullPointerException npe) {
					npe.printStackTrace();
					throw new ServerException(Response.BAD_REQUEST_STATUS_CODE,
							"Request: getRequestFields");
				}
			}
			return requestFields;
		} catch (IOException ioe) {
			throw new ServerException(Response.BAD_REQUEST_STATUS_CODE,
					"Request: getRequestFields");
		}
	}

	/*************************************************************
	 * 
	 * Parsing body
	 * 
	 *************************************************************/

}
