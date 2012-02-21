package webServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import webServer.request.Request;
import webServer.request.RequestParser;
import webServer.ulti.ConfigurationException;
import webServer.ulti.ServerException;

public class WebServer {

	public static final String WEB_SERVER_NAME = "MyServer";
	public static final String HTTPDD_CONF_PATH = "C:/MyWebServer/conf/";
	public static final String HTTPD_CONF_FILE = "httpd.conf";
	public static final String MIME_TYPES_FILE = "mime.types";

	private ServerSocket server;
	private Socket client;

	public WebServer() throws FileNotFoundException, IOException, ConfigurationException {
		
		this.configure();
		this.prepareMIMETypes();
		server = new ServerSocket(HttpdConf.LISTEN);
		System.out.println("Opened socket " + HttpdConf.LISTEN);
	
	}

	protected void configure() throws FileNotFoundException, IOException, ConfigurationException {
		
		File confFile = new File(HTTPDD_CONF_PATH, HTTPD_CONF_FILE);
		if (!confFile.exists())
			throw new FileNotFoundException("File not found: "
					+ confFile.getAbsolutePath());
		new HttpdConfReader(confFile).readHttpdConfFile();
	
	}

	protected void prepareMIMETypes() throws FileNotFoundException, IOException {
		
		File mimeFile = new File(HTTPDD_CONF_PATH, MIME_TYPES_FILE);
		if (!mimeFile.exists())
			throw new FileNotFoundException("File not found: "
					+ mimeFile.getAbsolutePath());
		new MIME(mimeFile).readMIMEType();
	
	}

	/**
	 * 
	 * Start the server. This will be infinite loop for listening to client's
	 * request.
	 * 
	 */
	public void start() throws FileNotFoundException, IOException {

		Response response = new Response();
		Request request;

		while (true) {
			try {
				// keeps listening for new clients, one at a time
				client = server.accept(); // waits for client here
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			try {

				request = new RequestParser().parseRequest(client
						.getInputStream());
				response.processRequest(request, client.getOutputStream());

			} catch (ServerException se) {

				se.printStackTrace();
				response.sendErrorMessage(client.getOutputStream(),
						se.getStatusCode());

			} finally {
				client.close();
			}
		}

	}

	public static void main(String[] args) {

		try {

			new WebServer().start();

		} catch (FileNotFoundException fne) {

			System.out.println(fne.getMessage());
			System.exit(1);

		} catch (ConfigurationException wte) {

			System.out.println(wte.getMessage());
			System.exit(1);

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(1);

		}
	}

}
