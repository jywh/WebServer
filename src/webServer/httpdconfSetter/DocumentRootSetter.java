package webServer.httpdconfSetter;

import webServer.HttpdConf;

public class DocumentRootSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws WrongTypeException{
		
		if( !(line instanceof String ))
			throw new WrongTypeException("DocumentRootSetter: String");
		
		HttpdConf.DOCUMENT_ROOT = ((String)line).substring(1,((String)line).length()-1);
	}

}
