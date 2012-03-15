package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;

public class PersistentConnection extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if(!(line instanceof String))
			throw new ConfigurationException("PersistenConnection: type String expected");
		
		if(line.equals("ON"))
			HttpdConf.PERSISTENT_CONNECTION = true;
	}

}
