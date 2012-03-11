package webServer;

import java.io.InputStream;
import java.io.OutputStream;

import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.request.RequestParser;
import webServer.response.Response;
import webServer.ulti.ServerException;

/**
 * <p>
 * ClientTread handles request and response of each client request.
 * </p>
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

	public static ClientThread instantiate(InputStream in, OutputStream out, String IP) {
		return new ClientThread(in, out, IP);
	}

	@Override
	public void run() {

		Response response = null;
		try {

			Request request = new RequestParser(inputStream).parse(IP);
			response = new Response(request, outStream);
			response.processRequest();
		} catch (ServerException e) {
			e.printStackTrace();
			if (response == null)
				response = new Response(outStream);
			response.sendErrorMessage(e.getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			if (response == null)
				response = new Response(outStream);
			response.sendErrorMessage(ResponseTable.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				inputStream.close();
				outStream.close();
			} catch (Exception e) {

			}
			WebServer.removeThread();
		}
	}

}
