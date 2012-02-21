package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class CgiHandlerSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if(!(line instanceof String))
			throw new ConfigurationException("CgiHandlerSetter: type String expected");
		
		String[] keywords = ((String)line).split(" ",2);
		HttpdConf.CGI_HANDLER.put(keywords[1], keywords[0].substring(1, keywords[0].length()-1));
		
	}

	
	
}
