package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class MaxThreadSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		try {
			if( !(line instanceof String ))
				throw new WrongTypeException("MaxThreadSetter: String");
			
			HttpdConf.MAX_THREAD = Integer.parseInt((String)line);
		} catch (NumberFormatException ne) {
			HttpdConf.MAX_THREAD = 100;
		}
	}

}
