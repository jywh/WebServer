package webServer.ulti;

/**
 * Fatal exception. It is used in HttpdSetter, and HttpdConfReader
 * 
 * @author Wenhui
 *
 */
public class ConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1190103348936763303L;
	private String message="";
	
	public ConfigurationException(String message){
		this.message = message;
	}
	
	public ConfigurationException(){}
	
	public String getMessage(){
		return message;
	}
	
	public void printMessage(){
		System.out.println(message);
	}
}
