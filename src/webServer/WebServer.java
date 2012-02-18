package webServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import webServer.request.Request;
import webServer.request.RequestParser;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

public class WebServer {

	public static final String WEB_SERVER_NAME = "CSC667";
	public static final String HTTPDD_CONF_PATH = "C:/MyWebServer/conf/";
	public static final String HTTPD_CONF_FILE = "httpd.conf";
	public static final String MIME_TYPES_FILE = "mime.types";
	
	public static void main(String[] args) {
		ServerSocket ding = null;
		Socket dong = null;
		Response response = new Response();
		Request request;

		try {

			setup();
			
			ding = new ServerSocket(HttpdConf.LISTEN);
			Log.log("Opened socket ", HttpdConf.LISTEN);

			while (true) {
				try {
					// keeps listening for new clients, one at a time
					dong = ding.accept(); // waits for client here
				
				} catch (IOException ioe) {
					
					ioe.printStackTrace();
					continue;
				}
				
				try {

					request = new RequestParser().parseRequest(dong.getInputStream());
					response.processRequest(request, dong.getOutputStream());

				} catch (ServerException se) {
					
					se.printStackTrace();
					response.writeErrorMessage(dong.getOutputStream(), se.getStatusCode());

				} catch(Exception e){
				
					e.printStackTrace();
					
				}finally {
				
					dong.close();
				}
			}
		} catch (IOException e) {
			
			e.printStackTrace();
			System.exit(1);
		
		}
	}
	
	public static void setup() throws IOException{
		// Read httpd.conf and mime file
		HttpdConfReader reader = new HttpdConfReader(HTTPDD_CONF_PATH+HTTPD_CONF_FILE);
		reader.readHttpdConfFile();

		MIME mime = new MIME(HTTPDD_CONF_PATH+MIME_TYPES_FILE);
		mime.readMIMEType();

	}

}
