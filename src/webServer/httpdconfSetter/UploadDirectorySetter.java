package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class UploadDirectorySetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String))
			throw new ConfigurationException("UploadDirectorySetter: type String expected");
		
		HttpdConf.UPLOAD = ((String)line).substring(1,((String)line).length()-1);
	}

	
	
}
