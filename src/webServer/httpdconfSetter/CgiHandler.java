package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class CgiHandler extends HttpdConfSetter {

	@Override
	public void process( Object line ) throws ConfigurationException {

		if ( !( line instanceof String ) )
			throw new ConfigurationException( "CgiHandler: type String expected" );

		String[] keywords = ( ( String ) line ).split( " ", 2 );
		HttpdConf.CGI_HANDLER.put( keywords[1], Utils.removeQuote( keywords[0] ) );
	}

}
