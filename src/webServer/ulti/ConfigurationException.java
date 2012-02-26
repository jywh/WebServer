package webServer.ulti;


/**
 * Fatal exception, server should stop when this exception is caught. 
 * It is used in HttpdSetter, and HttpdConfReader
 * 
 *
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
