package webServer;

import java.io.InputStream;
import java.io.OutputStream;

import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.request.RequestParser;
import webServer.response.Response;
import webServer.ulti.ServerException;

/**
 * 
 * This class handles request and response to client. It is final class, not
 * allow to be extended.
 * 
 */
public final class ClientThread extends Thread {

	private InputStream inputStream = null;
	private OutputStream outStream = null;
	private String IP = "";

	private ClientThread(InputStream in, OutputStream out, String IP) {
		this.inputStream = in;
		this.outStream = out;
		this.IP = IP;
	}

	public static ClientThread instantiate(InputStream in, OutputStream out,
			String IP) {
		return new ClientThread(in, out, IP);
	}

	@Override
	public void run() {

		Response response = new Response();
		Request request;
		try {
			request = new RequestParser().parse(inputStream, IP);
			response.processRequest(request, outStream);
		} catch (ServerException e) {
			e.printMessage();
			e.printStackTrace();
			response.sendErrorMessage(outStream, e.getStatusCode());
		} catch(Exception e){
			e.printStackTrace();
			response.sendErrorMessage(outStream, ResponseTable.INTERNAL_SERVER_ERROR);
		} finally {
			WebServer.removeThread();
		}
	}

}
