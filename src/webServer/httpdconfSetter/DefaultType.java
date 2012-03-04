package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class DefaultType extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {
		
		if ( !(line instanceof String) )
			throw new ConfigurationException("DefaultType: String expected");
		
		HttpdConf.DEFAULT_TYPE = (String) line;
	}

}
