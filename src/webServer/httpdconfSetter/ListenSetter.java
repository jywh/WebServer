package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class ListenSetter extends HttpdConfSetter {

	@Override
	public void process(Object portNumber) throws WrongTypeException{
		try{
			if( !(portNumber instanceof String ))
				throw new WrongTypeException("ListenSetter: String");
			
			HttpdConf.LISTEN = Integer.parseInt((String)portNumber);
		}catch(NumberFormatException nfe){
			
		}		
	}

	
	
}
