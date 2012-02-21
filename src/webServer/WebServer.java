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

	/**
	 * 
	 * Web server needs to be configured before start running, so user must
	 * provide path to configuration directory, where web server will look for
	 * httpd.conf and mime.types files.
	 * 
	 * @param confDiretory The path to web server configuration directory.
	 * 
	 */
	public WebServer(String confDiretory) throws FileNotFoundException,
			IOException, ConfigurationException {

		this.configure(confDiretory);
		this.prepareMIMETypes(confDiretory);
		server = new ServerSocket(HttpdConf.LISTEN);
		System.out.println("Opened socket " + HttpdConf.LISTEN);

	}

	protected void configure(String confDirectory)
			throws FileNotFoundException, IOException, ConfigurationException {

		File confFile = new File(confDirectory, HTTPD_CONF_FILE);
		if (!confFile.exists())
			throw new FileNotFoundException("File not found: "
					+ confFile.getAbsolutePath());
		new HttpdConfReader(confFile).readHttpdConfFile();

	}

	protected void prepareMIMETypes(String confDirectory)
			throws FileNotFoundException, IOException {

		File mimeFile = new File(confDirectory, MIME_TYPES_FILE);
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
	public void start() throws IOException {

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

				response.sendErrorMessage(client.getOutputStream(),
						se.getStatusCode());
				se.printStackTrace();

			} finally {
				client.close();
			}
		}

	}

	/**
	 * 
	 * @param args The path to web server configurateion directory.
	 */
	public static void main(String[] args) {

		try {
			if (args.length != 1){
				System.out.println("Exact one argument: path of web server configuration directory");
				return;
			}
			new WebServer(args[0]).start();

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
