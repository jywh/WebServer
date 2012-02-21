package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class ScriptAliasSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if(!(line instanceof String ))
			throw new ConfigurationException("ScriptAliasSetter: type String expected");
		
		String[] keywords = ((String)line).split(" ", 2);
		if(keywords.length < 2) return;
		HttpdConf.SCRIPT_ALIAS.put(keywords[0], keywords[1].substring(1, keywords[1].length()-1));
	}
}
