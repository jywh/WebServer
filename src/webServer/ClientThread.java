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
 * 
 */
public class ClientThread extends Thread {

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

		try {

			Request request = new RequestParser().parse(inputStream, IP);
			new Response(request, outStream).processRequest();

		} catch (ServerException e) {
			e.printMessage();
			e.printStackTrace();
			new Response(outStream).sendErrorMessage(e.getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			new Response(outStream)
					.sendErrorMessage(ResponseTable.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				inputStream.close();
				outStream.close();
			} catch (Exception ie) {

			}
			WebServer.removeThread();
		}
	}

}
