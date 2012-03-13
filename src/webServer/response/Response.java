package webServer.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import webServer.constant.HeaderFields;
import webServer.constant.HttpdConf;
import webServer.constant.ResponseTable;
import webServer.httpdconfSetter.Directory.SecureDirectory;
import webServer.request.Request;
import webServer.ulti.AccessLog;
import webServer.ulti.HttpDigest;
import webServer.ulti.ServerException;
import webServer.ulti.Ulti;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * 
 * <p>
 * Reponse to client request.
 * </p>
 * 
 */
public class Response {

	public final static String TAG = "Response";
	public final static String ERROR_FILE_PATH = HttpdConf.SERVER_ROOT + "/error/";
	private final static Pattern SCRIPT_PATTERN = Pattern.compile( "([^\\s]+(\\.(?i)(py|pl)))" );

	private final static int NOT_SECURE_DIR = 1;
	private final static int NEED_AUTHENTICATE = 2;
	private final static int AUTHENTICATED = 3;

	private Request request = null;
	private OutputStream outStream = null;

	public Response( Request request, OutputStream outStream ) {
		this.request = request;
		this.outStream = outStream;
	}

	public Response( OutputStream outStream ) {
		this.outStream = outStream;
	}

	/**
	 * Process request, produce appropriate response. Check if the URI contains
	 * secure directory.
	 * 
	 * options:
	 * 
	 * 1. NOT_SECURE_DIR: contains no secure directory. 2. NEED_AUTHENTICATE:
	 * contains secure directory, need authentication. 3. AUTHENTICATED: pass
	 * authentication.
	 * 
	 * @param request
	 *            A request object created by RequestParser
	 * @param out
	 *            Client OutputStream
	 * @throws ServerException
	 */
	public void processRequest() throws ServerException {
		int statusCode;

		SecureDirectory secureDirectory = getSecureDirectory( request.getURI() );
		switch ( checkAuthentication( secureDirectory ) ) {
		case NOT_SECURE_DIR:
			statusCode = processNormalRequest( true );
			break;
		case NEED_AUTHENTICATE:
			statusCode = sendAuthenticateMessage( secureDirectory );
			break;
		case AUTHENTICATED:
			statusCode = processNormalRequest( false );
			break;
		default:
			throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR );
		}

		new AccessLog().log( request, statusCode );
	}

	/*************************************************************
	 * Check authentication
	 *************************************************************/

	/**
	 * Check if the URI contain any secure directory that is defined in
	 * httpd.conf file.
	 * 
	 * @param uri
	 * @return The secure directory if it exists, null otherwise.
	 */
	private SecureDirectory getSecureDirectory( String uri ) {
		Set< String > secureDirectories = HttpdConf.secureUsers.keySet();
		for ( String directory : secureDirectories ) {
			if ( uri.contains( directory ) )
				return HttpdConf.secureUsers.get( directory );
		}
		return null;
	}

	/**
	 * Check if the uri contains secure directory.
	 * 
	 * @param request
	 * @return Authentication Code.
	 * @throws ServerException
	 */
	private int checkAuthentication( SecureDirectory secureDirectory ) throws ServerException {

		if ( secureDirectory == null )
			return NOT_SECURE_DIR;

		if ( !request.getHeaderField().containsKey( HeaderFields.AUTHORIZATION ) )
			return NEED_AUTHENTICATE;

		String auth = request.getHeaderField().get( HeaderFields.AUTHORIZATION );
		String[] tokens = auth.split( " ", 2 );
		if ( authenticate( secureDirectory, tokens[0], tokens[1] ) )
			return AUTHENTICATED;

		// Password or username not correct
		throw new ServerException( ResponseTable.FORBIDDEN );
	}

	private boolean authenticate( SecureDirectory sd, String authType, String authInfo )
			throws ServerException {

		if ( !sd.getAuthType().equals( authType ) )
			return false;

		if ( authType.equals( SecureDirectory.AUTH_TYPE_BASIC ) ) {
			try {
				String decodedText = new String( Base64.decode( authInfo ) );
				String[] tokens = decodedText.split( ":", 2 );
				String passwd = sd.getValidUsers().get( tokens[0] );
				if ( passwd == null || !passwd.equals( tokens[1] ) )
					return false;
				return true;
			} catch ( Base64DecodingException e ) {
				throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR );
			}
		}

		if ( authType.equals( SecureDirectory.AUTH_TYPE_DIGEST ) ) {
			String[] tokens = authInfo.split( ", " );
			String[] elements;
			HashMap< String, String > tags = new HashMap< String, String >();
			for ( String s : tokens ) {
				elements = s.split( "=", 2 );
				tags.put( elements[0], elements[1].substring( 1, elements[1].length() - 1 ) );
			}
			String username = tags.get( "username" );
			String pwd = sd.getValidUsers().get( username );

			if ( pwd == null )
				return false;

			String uri = tags.get( "uri" );
			String realm = tags.get( "realm" );
			String nonce = tags.get( "nonce" );
			String method = request.getMethod();
			String algorithm = "MD5";
			try {
				String digest = HttpDigest.createDigest( username, pwd, uri, realm, nonce, method, algorithm );
				return tags.get( "response" ).equals( digest );
			} catch ( NoSuchAlgorithmException e ) {
				throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR );
			}
		}

		return false;
	}

	/**
	 * Send authentication request to client.
	 * 
	 * @param info
	 * @return
	 * @throws ServerException
	 */
	private int sendAuthenticateMessage( SecureDirectory info ) throws ServerException {
		String headerMessage = createBasicHeaderMessage( ResponseTable.UNAUTHORIZED ).buildAuthentication(
				info.getAuthType(), info.getAuthName() ).toString();
		writeHeaderMessage( headerMessage );
		return ResponseTable.UNAUTHORIZED;
	}

	/*************************************************************
	 * Process normal request
	 *************************************************************/

	private int processNormalRequest( boolean allowCache ) throws ServerException {

		if ( request.getMethod().equals( Request.PUT ) ) {
			return processPUT();
		} else if ( isScript( request.getURI() ) ) {
			return executeScript();
		} else {
			return retrieveStaticDocument( allowCache );
		}
	}

	/**
	 * Check if the URI is a path to a script file.
	 * 
	 * @param URI
	 * @return
	 */
	private boolean isScript( String URI ) {
		File file = new File( URI );
		return SCRIPT_PATTERN.matcher( file.getName() ).matches();
	}

	/**
	 * 
	 * Send the script to CGIHandler, then capture the output of script
	 * execution and send back to client.
	 * 
	 * @return The status code
	 * @throws ServerException
	 */
	private int executeScript() throws ServerException {
		CGIOutputStreamReader cin = new CGIHandler().sendScript( request );
		try {
			int headerStringLen = cin.getHeaderStringSize();
			byte[] content = cin.readBodyContent();
			int contentLength = content.length - headerStringLen;
			String headerMessage = createBasicHeaderMessage( ResponseTable.OK ).buildContentLength(
					contentLength ).toString();

			BufferedOutputStream out = new BufferedOutputStream( outStream );
			out.write( headerMessage.getBytes( "UTF-8" ) );
			out.write( content );
			cin.close();
			out.flush();

			return ResponseTable.OK;

		} catch ( IOException ioe ) {
			ioe.printStackTrace();
			throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR );
		}
	}

	/**
	 * Retrieve static file from server.
	 * 
	 * @param allowCache
	 * @return
	 * @throws ServerException
	 */
	private int retrieveStaticDocument( boolean allowCache ) throws ServerException {

		if ( request.getMethod().equals( Request.HEAD ) ) {
			String headerMessage = createBasicHeaderMessage( ResponseTable.OK ).toString();
			writeHeaderMessage( headerMessage );
			return ResponseTable.OK;
		}

		File document = new File( request.getURI() );
		if ( !isModified( document ) ) {
			String headerMessage = createBasicHeaderMessage( ResponseTable.NOT_MODIFIED ).toString();
			writeHeaderMessage( headerMessage );
			return ResponseTable.NOT_MODIFIED;
		}

		String headerMessage = createSimpleHeaderMessage( ResponseTable.OK, document, allowCache ).toString();
		writeHeaderMessage( headerMessage );
		serveFile( document );
		return ResponseTable.OK;
	}

	/**
	 * Check if file is modified since IF_MODIFIED_SINCE date that sent from
	 * client.
	 * 
	 * @param file
	 * @return Ture if it is modified, false otherwise.
	 */
	private boolean isModified( File file ) {
		String dateFromClient = request.getHeaderField().get( HeaderFields.IF_MODIFIED_SINCE );
		if ( dateFromClient == null )
			return true;
		// Remove last three significant digits, because convert date from
		// String to long lose last three significant digits.
		long lastModified = ( file.lastModified() / 1000L ) * 1000L;
		try {
			Date clientDate = ( Date ) Ulti.DATE_FORMATE.parse( dateFromClient );
			return lastModified > clientDate.getTime();
		} catch ( Exception e ) {
			// If there is exception, assume file is modified
		}
		return true;
	}

	/**
	 * Response to PUT method. Since all the files will be uploaded to the same
	 * directory, synchronized block will ensure that there is only on thread
	 * allow to write the file to UPLOAD directory at once. It also ensure there
	 * won't be multiple threads uploading files with the same name that the
	 * previous one gets overwritten.
	 * 
	 */
	private int processPUT() throws ServerException {
		int statusCode;
		File document = new File( request.getURI() );
		try {
			synchronized ( this ) {
				if ( !document.exists() ) {
					BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( document ) );
					out.write( request.getParameterByteArray() );
					statusCode = ResponseTable.CREATED;
					out.close();
				} else {
					statusCode = ResponseTable.NO_CONTENT;
				}
			}
			String headerMessage = createBasicHeaderMessage( statusCode ).toString();
			writeHeaderMessage( headerMessage );
			return statusCode;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
			throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR );
		}
	}

	/*************************************************************
	 * Build Header String
	 *************************************************************/

	/**
	 * Build header message for basic response with header field connection.
	 * 
	 */
	private HeaderBuilder createBasicHeaderMessage( int statusCode ) {
		HeaderBuilder builder = new HeaderBuilder();
		return builder.buildHeaderBegin( statusCode, request.getHttpVersion() ).buildConnection( "close" );
	}

	/**
	 * Build header message for reponse that requires content type, content
	 * lenght and cache control.
	 * 
	 * @param statusCode
	 * @param document
	 * @param allowCache
	 * @return
	 */
	private HeaderBuilder createSimpleHeaderMessage( int statusCode, File document, boolean allowCache ) {
		HeaderBuilder builder = createBasicHeaderMessage( statusCode ).buildContentTypeAndLength( document );
		if ( allowCache && HttpdConf.CACHE_ENABLE )
			builder.buildLastModified( document.lastModified() ).buildCacheControl( "public" );
		return builder;

	}

	/*************************************************************
	 * Response to client
	 *************************************************************/

	protected void writeHeaderMessage( String headerMessage ) {
		PrintWriter writer = new PrintWriter( outStream, true );
		writer.println( headerMessage );
	}

	protected void serveFile( File document ) throws ServerException {

		try {
			byte[] buf = new byte[1024];
			BufferedInputStream in = new BufferedInputStream( new FileInputStream( document ) );
			BufferedOutputStream out = new BufferedOutputStream( outStream );
			int read = -1;
			while ( ( read = in.read( buf ) ) > -1 ) {
				out.write( buf, 0, read );
			}
			in.close();
			out.flush();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
			throw new ServerException( ResponseTable.INTERNAL_SERVER_ERROR, "ServeFile" );
		}
	}

	/**
	 * Send error message when ServerException is caught.
	 * 
	 * @param statusCode
	 */
	public void sendErrorMessage( int statusCode ) {

		String httpVersion = HttpdConf.DEFAULT_HTTP_VERSION;
		if ( request != null && request.getHttpVersion() != null )
			httpVersion = request.getHttpVersion();

		try {

			File errorFile = new File( ERROR_FILE_PATH + Integer.toString( statusCode ) + ".html" );
			HeaderBuilder builder = new HeaderBuilder();
			String headerMessage = builder.buildHeaderBegin( statusCode, httpVersion )
					.buildContentTypeAndLength( errorFile ).toString();
			writeHeaderMessage( headerMessage );
			serveFile( errorFile );

		} catch ( ServerException se ) {
			se.printStackTrace();
		}

	}

}
