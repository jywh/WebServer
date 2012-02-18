package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class ScriptAliasSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if(!(line instanceof String ))
			throw new WrongTypeException("ScriptAliasSetter: String");
		
		String[] keywords = ((String)line).split(" ", 2);
		if(keywords.length < 2) return;
		HttpdConf.SCRIPT_ALIAS.put(keywords[0], keywords[1].substring(1, keywords[1].length()-1));
	}
}
