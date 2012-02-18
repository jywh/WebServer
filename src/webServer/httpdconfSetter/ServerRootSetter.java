package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class ServerRootSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException {
		
		if(!(line instanceof String))
			throw new WrongTypeException("ServerRootSetter: String");
		HttpdConf.SERVER_ROOT = ((String)line).substring(1,((String)line).length()-1);
	}

}
