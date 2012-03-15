package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;

public class CacheEnabled extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String ))
			throw new ConfigurationException("CacheEnabled: type String expected");
		
		if(((String)line).equals("ON"))
			HttpdConf.CACHE_ENABLE = true;
	}

	
}
