package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class PersistentConnectionSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws WrongTypeException {
		
		if(!(line instanceof String))
			throw new WrongTypeException("PersistenConnectionSetter: String");
		
		if(line.equals("ON"))
			HttpdConf.PERSISTENT_CONNECTION = true;
	}

}
