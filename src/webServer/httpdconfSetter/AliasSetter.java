package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class AliasSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if( !(line instanceof String)) 
			throw new WrongTypeException("AliasSetter: String");
		
		String[] keywords = ((String)line).split(" ");
		
		HttpdConf.ALIAS.put(keywords[0], keywords[1].substring(1, keywords[1].length()-1));
	}
	
}
