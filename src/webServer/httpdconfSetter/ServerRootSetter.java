package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class ServerRootSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if(!(line instanceof String))
			throw new ConfigurationException("ServerRootSetter: String");
		HttpdConf.SERVER_ROOT = ((String)line).substring(1,((String)line).length()-1);
	}

}
