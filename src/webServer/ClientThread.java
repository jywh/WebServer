package webServer;

import java.io.InputStream;
import java.io.OutputStream;

import webServer.request.Request;
import webServer.request.RequestParser;
import webServer.response.Response;
import webServer.ulti.ServerException;

public final class ClientThread extends Thread {

	private InputStream inputStream = null;
	private OutputStream outStream = null;
	private String IP="";
	
	private ClientThread(InputStream aIn, OutputStream aOut, String IP) {
		this.inputStream = aIn;
		this.outStream = aOut;
		this.IP = IP;
	}

	public static ClientThread instantiate(InputStream aIn, OutputStream aOut, String IP) {
		return new ClientThread(aIn, aOut, IP);
	}

	@Override
	public void run() {

		Response response = new Response();
		try {
			Request request = new RequestParser(inputStream, IP).parseRequest();
			response.processRequest(request, outStream);
		} catch (ServerException se) {
			se.printStackTrace();
			response.sendErrorMessage(outStream, se.getStatusCode());
		}finally{
			WebServer.removeThread();
		}
	}

}
