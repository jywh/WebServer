package webServer.httpdconfSetter;

public class WrongTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1190103348936763303L;
	private String message="";
	
	public WrongTypeException(String message){
		this.message = message;
	}
	
	public WrongTypeException(){}
	
	public String getMessage(){
		return message;
	}
	
	public void printMessage(){
		System.out.println(message);
	}
}
