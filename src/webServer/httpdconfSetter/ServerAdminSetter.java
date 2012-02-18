package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class ServerAdminSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException {
		
		if( !(line instanceof String ))
			throw new WrongTypeException("ServerAdminSetter: String");
		
		HttpdConf.SERVER_ADMIN = ((String)line).substring(1,((String)line).length()-1);
	}

}
