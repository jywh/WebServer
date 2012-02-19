package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class CgiHandlerSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws WrongTypeException {
		
		if(!(line instanceof String))
			throw new WrongTypeException("CgiHandlerSetter: String");
		
		String[] keywords = ((String)line).split(" ",2);
		HttpdConf.CGI_HANDLER.put(keywords[1], keywords[0].substring(1, keywords[0].length()-1));
		
	}

	
	
}