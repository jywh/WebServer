package webServer.ulti;


/**
 * 
 * Signal that various errors happen during server request and reponse. 
 * 
 * statusCode refers to http error code.
 * 
 * @author Wenhui
 *
 */
@SuppressWarnings("serial")
public class ServerException extends Exception{

	private int statusCode;
	
	public ServerException(int statusCode){
		super();
		this.statusCode = statusCode;
	}
	
	public ServerException(int statusCode, String s){
		super(s);
		this.statusCode = statusCode;
	}
	
	public int getStatusCode(){
		return statusCode;
	}
	
	public void printMessage(){
		System.out.println(super.getMessage());
	}
}
