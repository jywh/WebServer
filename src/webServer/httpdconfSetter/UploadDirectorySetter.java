package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class UploadDirectorySetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if( !(line instanceof String))
			throw new WrongTypeException("UploadDirectorySetter: String");
		
		HttpdConf.UPLOAD = ((String)line).substring(1,((String)line).length()-1);
	}

	
	
}
