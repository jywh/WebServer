package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class MaxThreadSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		try {
			if( !(line instanceof String ))
				throw new ConfigurationException("MaxThreadSetter: String");
			
			HttpdConf.MAX_THREAD = Integer.parseInt((String)line);
		} catch (NumberFormatException ne) {
			HttpdConf.MAX_THREAD = 100;
		}
	}

}
