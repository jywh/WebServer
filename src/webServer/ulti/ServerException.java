package webServer.ulti;

/**
 * 
 * Signal that various errors happen during parsing request or reponse.
 * 
 * statusCode refers to http error code.
 * 
 */
@SuppressWarnings("serial")
public class ServerException extends Exception {

	private int statusCode;

	public ServerException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public ServerException(int statusCode, String s) {
		super(s);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void printMessage() {
		System.out.println(Integer.toString(statusCode) + ": " + super.getMessage());
	}
}
