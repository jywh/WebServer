package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class TempDirectorySetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if(!(line instanceof String ))
			throw new WrongTypeException("TempDirectorySetter: String");
		HttpdConf.TEMP_DIRECTORY = ((String)line).substring(1,((String)line).length()-1);
	}

	
}
