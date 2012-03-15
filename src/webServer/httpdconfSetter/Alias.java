package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;

public class Alias extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String)) 
			throw new ConfigurationException("Alias: type String expected");
		
		String[] keywords = ((String)line).split(" ");
		
		HttpdConf.ALIAS.put(keywords[0], keywords[1].substring(1, keywords[1].length()-1));
	}
	
}
