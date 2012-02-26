package webServer.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import webServer.HttpdConf;
import webServer.WebServer;
import webServer.response.Response;
import webServer.ulti.Log;
import webServer.ulti.LogContent;
import webServer.ulti.ServerException;

/**
 * 
 * RequestParser is responsiable to parse the incomming request message, and
 * create a request object. All the incomming message must go through
 * RequestParser.
 * 
 */
public class RequestParser {

	public static final String URI_SEPARATOR = "/";
	private static final String HTTP_PREFIX = "HTTP_";
	private static final String REMOTE_ADDR = "REMOTE_ADDR";
	private static final String SERVER_NAME = "SERVER_NAME";
	private static final String SERVER_SOFTWARE = "SERVER_SOFTWARE";
	private static final String SERVER_PROTOCOL = "SERVER_PROTOCOL";
	private static final String SERVER_PORT = "SERVER_PORT";
	private static final String REQUEST_METHOD = "REQUEST_METHOD";
	
	private BufferedReader incommingMessage;
	private LogContent logContent;
	private String IP;
	
	public RequestParser(InputStream inputStream, String IP) throws ServerException{
		
		if (inputStream == null)
			throw new ServerException(Response.BAD_REQUEST);
		
		incommingMessage = new BufferedReader(
				new InputStreamReader(inputStream));
		this.IP = IP;
		logContent = new LogContent();
	}
	
	public Request parseRequest() throws ServerException {
		
		try {

			// Parse first line of request message
			String[] parameters = parseFirstLine(incommingMessage.readLine());
			Map<String, String> requestFields = extractRequestFields(parameters[0], parameters[2]);
			Log.debug("URI is", parameters[3]);
			if (parameters[0].equals(Request.POST) || parameters[0].equals(Request.PUT)){
				parameters[3] = extractParameterStringFromBody();
				Log.debug("POST parameter", parameters[3]);
			}

			logContent.setIP(IP);
			return new Request(parameters[0], parameters[1], parameters[2],
					parameters[3], requestFields, logContent);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"RequestParser: parseRequest");
		}

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
		
		logContent.setRequestLine(firstLine);
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
		
		Log.debug("URI ", URI);
		
		if (!(new File(URI)).exists())
			throw new ServerException(Response.NOT_FOUND,
					"RequestParser: addDocumentRoot");
		return new String[] { methos, URI, httpVersion, parameterString };

	}

	/**
	 * This will resolve alias that contains in the URI
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

	private Map<String, String> extractRequestFields(String method, String protocol) throws ServerException {

		try {
			String currentLine = incommingMessage.readLine();
			Map<String, String> requestHeaders = createPrefilledRequestHeaderList(method, protocol);
			String[] tokens;
			while( currentLine != null && !currentLine.trim().isEmpty()){
				tokens = convertStringToEnvironmentVaraible(currentLine);
				requestHeaders.put(tokens[0], tokens[1]);
				currentLine = incommingMessage.readLine();
			}
			
			return requestHeaders;
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"Request: getRequestFields");
		}
	}

	protected String[] convertStringToEnvironmentVaraible(String line) {

		String[] tokens = line.split(":", 2);
		tokens[1] = tokens[1] != null ? tokens[1].trim() : "";
		tokens[0] = HTTP_PREFIX + tokens[0].replace('-', '_').toUpperCase();
		return new String[]{tokens[0], tokens[1]};

	}

	/**
	 * Prefilled not-request specific and filed not in request header for environment variables
	 * 
	 * @return
	 */
	protected Map<String, String> createPrefilledRequestHeaderList(String method, String protocol){
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(SERVER_NAME, WebServer.SERVER_NAME);
		headers.put(SERVER_SOFTWARE, WebServer.SERVER_SOFTWARE);
		headers.put(REMOTE_ADDR,IP);
		headers.put(SERVER_PORT,Integer.toString(HttpdConf.LISTEN));
		headers.put(SERVER_PROTOCOL,protocol);
		headers.put(REQUEST_METHOD,method);
		return headers;
		
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
			
			StringBuilder builder = new StringBuilder();
			while ( incommingMessage.ready() ){
				builder.append((char)incommingMessage.read());
			}

			return builder.toString();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ServerException(Response.BAD_REQUEST,
					"Request: extractParameterStringFromBody");
		}

	}
}