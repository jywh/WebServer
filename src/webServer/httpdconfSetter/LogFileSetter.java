package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class LogFileSetter extends HttpdConfSetter {

	@Override
	public void process(Object path) throws WrongTypeException {
		
		if( !(path instanceof String ))
			throw new WrongTypeException("LogFileSetter: String");
		
		HttpdConf.LOG_FILE = ((String)path).substring(1, ((String)path).length()-1);
	}

}
