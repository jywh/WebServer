package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class PersistentConnectionSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if(!(line instanceof String))
			throw new ConfigurationException("PersistenConnectionSetter: type String expected");
		
		if(line.equals("ON"))
			HttpdConf.PERSISTENT_CONNECTION = true;
	}

}
