package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class DirectoryIndexSetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if( !(line instanceof String ))
			throw new WrongTypeException("DirectoryIndexSetter: String");
		
		String[] indexes = ((String)line).trim().split(" ");
		HttpdConf.DIRECTORY_INDEX = indexes;
	}
	
}
