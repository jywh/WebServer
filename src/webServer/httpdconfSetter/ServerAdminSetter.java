package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class ServerAdminSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if( !(line instanceof String ))
			throw new ConfigurationException("ServerAdminSetter: type String expected");
		
		HttpdConf.SERVER_ADMIN = ((String)line).substring(1,((String)line).length()-1);
	}

}
