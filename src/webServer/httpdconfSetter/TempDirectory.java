package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class TempDirectory extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if(!(line instanceof String ))
			throw new ConfigurationException("TempDirectorySetter: type String expected");
		HttpdConf.TEMP_DIRECTORY = ((String)line).substring(1,((String)line).length()-1);
	}

	
}
