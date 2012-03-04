package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class DirectoryIndex extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String ))
			throw new ConfigurationException("DirectoryIndex: type String expected");
		
		String[] indexes = ((String)line).trim().split(" ");
		HttpdConf.DIRECTORY_INDEX = indexes;
	}
	
}
