package webServer.httpdconfSetter;

import webServer.HttpdConf;
import webServer.ulti.ConfigurationException;

public class DocumentRootSetter extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String ))
			throw new ConfigurationException("DocumentRootSetter: String");
		
		HttpdConf.DOCUMENT_ROOT = ((String)line).substring(1,((String)line).length()-1);
	}

}
