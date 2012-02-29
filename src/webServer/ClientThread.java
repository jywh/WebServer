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
	private String IP = "";
	private int remotePort;

	private ClientThread(InputStream in, OutputStream out, String IP,
			int remotePort) {
		this.inputStream = in;
		this.outStream = out;
		this.IP = IP;
		this.remotePort = remotePort;
	}

	public static ClientThread instantiate(InputStream in, OutputStream out,
			String IP, int remotePort) {
		return new ClientThread(in, out, IP, remotePort);
	}

	@Override
	public void run() {

		Response response = new Response();
		Request request;
		try {
			request = new RequestParser().parseRequest(inputStream, IP,
					remotePort);
			response.processRequest(request, outStream);
		} catch (ServerException e) {
			e.printStackTrace();
		} finally {
			WebServer.removeThread();
		}
	}

}
