package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class LogFile extends HttpdConfSetter {

	@Override
	public void process(Object path) throws ConfigurationException {
		
		if( !(path instanceof String ))
			throw new ConfigurationException("LogFileSetter: type String expected");
		
		HttpdConf.LOG_FILE = ((String)path).substring(1, ((String)path).length()-1);
	}

}
