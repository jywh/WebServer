package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class AliasSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String)) 
			throw new ConfigurationException("AliasSetter: String");
		
		String[] keywords = ((String)line).split(" ");
		
		HttpdConf.ALIAS.put(keywords[0], keywords[1].substring(1, keywords[1].length()-1));
	}
	
}
