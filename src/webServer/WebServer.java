package webServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import webServer.constant.HttpdConf;
import webServer.utils.AccessLog;
import webServer.utils.ConfigurationException;

public class WebServer {

	public static final String SERVER_NAME = "MyServer02";
	public static final String SERVER_SOFTWARE = "MyJava02";
	public static final String GATEWAY_INTERFACE = "CGI/1.0";

	public static final String HTTPD_CONF_FILE = "httpd.conf";
	public static final String MIME_TYPES_FILE = "mime.types";

	private static int threadCount = 0;
	private ServerSocket server;

	/**
	 * 
	 * Web server needs to be configured before start running, so user must
	 * provide path to configuration directory, where web server will look for
	 * httpd.conf and mime.types files.
	 * 
	 * @param confDiretory
	 *            The path to web server configuration directory.
	 * 
	 */
	public WebServer( String confDiretory ) throws IOException, ConfigurationException {

		this.prepareMIMETypes( confDiretory );
		this.configure( confDiretory );
		AccessLog.initialize();
		server = new ServerSocket( HttpdConf.LISTEN );
		System.out.println( "Opened socket " + HttpdConf.LISTEN );

	}

	protected void configure( String confDirectory ) throws IOException, ConfigurationException {

		File confFile = new File( confDirectory, HTTPD_CONF_FILE );
		if ( !confFile.exists() )
			throw new IOException( "File not found: " + confFile.getAbsolutePath() );
		new HttpdConfReader( confFile ).readHttpdConfFile();

	}

	protected void prepareMIMETypes( String confDirectory ) throws IOException, ConfigurationException {

		File mimeFile = new File( confDirectory, MIME_TYPES_FILE );
		if ( !mimeFile.exists() )
			throw new IOException( "File not found: " + mimeFile.getAbsolutePath() );
		new MIME( mimeFile ).readMIMEType();

	}

	public void addThread() {
		threadCount++;
	}

	public synchronized static void removeThread() {
		threadCount--;
	}

	public boolean moreThreadAllowed() {
		return threadCount <= HttpdConf.MAX_THREAD;
	}

	public void close() throws IOException {
		if ( server != null )
			server.close();
	}

	/**
	 * 
	 * Start the server. This will be infinite loop for listening to client's
	 * request.
	 * 
	 */
	public void start() throws IOException {

		Socket client = null;

		while ( true ) {
			try {
				// keeps listening for new clients, one at a time
				client = server.accept(); // waits for client here
			} catch ( IOException ioe ) {
				ioe.printStackTrace();
				continue;
			}

			if ( moreThreadAllowed() ) {
				ClientThread.instantiate( client.getInputStream(), client.getOutputStream(),
						client.getInetAddress().getHostAddress() ).start();
				addThread();
			} else {
				System.out.println( "Reach maximum thread capacity." );
			}

			client = null;
		}

	}

	public static void main( String[] args ) {

		WebServer webServer = null;
		try {
			if ( args.length != 1 ) {
				System.out.println( "\nError: Need one arg, the directory to httpd.conf file" );
				System.out.println( "Usage: java -jar /home/student/667.02/WebServer.jar /home/student/667.02/conf/\n" );
				return;
			}
			webServer = new WebServer( args[0] );
			webServer.start();

		} catch ( ConfigurationException ce ) {

			System.out.println( ce.getMessage() );
			System.exit( 1 );

		} catch ( IOException e ) {

			e.printStackTrace();
			System.exit( 1 );

		} finally {
			try {
				if ( webServer != null )
					webServer.close();
			} catch ( IOException ioe ) {

			}
		}
	}

}
