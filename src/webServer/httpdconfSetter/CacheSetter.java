package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class CacheSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if( !(line instanceof String ))
			throw new WrongTypeException("CacheSetter: String");
		
		if(((String)line).equals("ON"))
			HttpdConf.CACHE_ENABLE = true;
	}

	
}
