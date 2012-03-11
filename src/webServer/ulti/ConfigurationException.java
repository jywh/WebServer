package webServer.ulti;


/**
 * <p>
 * Fatal exception, server should stop when this exception is caught. 
 * </p>
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception {

	public ConfigurationException(String s){
		super(s);
	}
	
	public ConfigurationException(){
		super();
	}
	
}
