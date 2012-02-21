package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class DirectoryIndexSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String ))
			throw new ConfigurationException("DirectoryIndexSetter: String");
		
		String[] indexes = ((String)line).trim().split(" ");
		HttpdConf.DIRECTORY_INDEX = indexes;
	}
	
}
