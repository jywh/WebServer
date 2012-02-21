package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class ListenSetter extends HttpdConfSetter {

	@Override
	public void process(Object portNumber) throws ConfigurationException{
		try{
			if( !(portNumber instanceof String ))
				throw new ConfigurationException("ListenSetter: String");
			
			HttpdConf.LISTEN = Integer.parseInt((String)portNumber);
		}catch(NumberFormatException nfe){
			
		}		
	}

	
	
}
