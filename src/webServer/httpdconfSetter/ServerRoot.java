package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class ServerRoot extends HttpdConfSetter {

	@Override
	public void process( Object line ) throws ConfigurationException {

		if ( !( line instanceof String ) )
			throw new ConfigurationException( "ServerRoot: type String expected" );
		HttpdConf.SERVER_ROOT = Utils.removeQuote( ( ( String ) line ) );
	}

}
