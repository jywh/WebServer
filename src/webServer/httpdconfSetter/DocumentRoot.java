package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class DocumentRoot extends HttpdConfSetter{

	@Override
	public void process(Object line) throws ConfigurationException{
		
		if( !(line instanceof String ))
			throw new ConfigurationException("DocumentRoot: type String expected");
		
		HttpdConf.DOCUMENT_ROOT = Utils.removeQuote( ((String)line) );
	}

}
