package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class MaxThread extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		try {
			if( !(line instanceof String ))
				throw new ConfigurationException("MaxThread: type String expected");
			
			HttpdConf.MAX_THREAD = Integer.parseInt((String)line);
		} catch (NumberFormatException ne) {
			throw new ConfigurationException("MaxThreadSetter: number format excepton");
		}
	}

}
