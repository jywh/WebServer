package webServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;
import webServer.ulti.Log;

public class WebServer {

	public static final String SERVER_NAME = "MyServer";
	public static final String SERVER_SOFTWARE = "MyJava";
	public static final String GATEWAY_INTERFACE = "";

	public static final String HTTPDD_CONF_PATH = "C:/MyWebServer/conf/";
	public static final String HTTPD_CONF_FILE = "httpd.conf";
	public static final String MIME_TYPES_FILE = "mime.types";

	private static int threadCount = 0;

	private ServerSocket server;
	private Socket client;

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
	public WebServer(String confDiretory) throws IOException,
			ConfigurationException {

		this.configure(confDiretory);
		this.prepareMIMETypes(confDiretory);
		server = new ServerSocket(HttpdConf.LISTEN);
		Log.initialize();
		System.out.println("Opened socket " + HttpdConf.LISTEN);

	}

	protected void configure(String confDirectory) throws IOException,
			ConfigurationException {

		File confFile = new File(confDirectory, HTTPD_CONF_FILE);
		if (!confFile.exists())
			throw new IOException("File not found: "
					+ confFile.getAbsolutePath());
		new HttpdConfReader(confFile).readHttpdConfFile();

	}

	protected void prepareMIMETypes(String confDirectory) throws IOException {

		File mimeFile = new File(confDirectory, MIME_TYPES_FILE);
		if (!mimeFile.exists())
			throw new IOException("File not found: "
					+ mimeFile.getAbsolutePath());
		new MIME(mimeFile).readMIMEType();

	}

	public static void addThread() {
		threadCount++;
	}

	public synchronized static void removeThread() {
		threadCount--;
	}

	public boolean allowMoreThread() {
		return threadCount <= HttpdConf.MAX_THREAD;
	}

	/**
	 * 
	 * Start the server. This will be infinite loop for listening to client's
	 * request.
	 * 
	 */
	public void start() throws IOException {

		while (true) {
			try {
				// keeps listening for new clients, one at a time
				client = server.accept(); // waits for client here
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			if (allowMoreThread()) {
				ClientThread.instantiate(client.getInputStream(),
						client.getOutputStream(),
						client.getInetAddress().getHostAddress()).start();
				addThread();
			} else {
				Log.debug("max thread exceed", "no more thread can be added");
			}

		}

	}

	/**
	 * 
	 * @param args
	 *            The path to web server configurateion directory.
	 */
	public static void main(String[] args) {

		try {
			if (args.length != 1) {
				System.out
						.println("Exact one argument: path of web server configuration directory");
				return;
			}
			new WebServer(args[0]).start();

		} catch (ConfigurationException wte) {

			System.out.println(wte.getMessage());
			System.exit(1);

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(1);

		}
	}

}
