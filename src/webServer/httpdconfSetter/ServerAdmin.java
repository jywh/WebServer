package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class ServerAdmin extends HttpdConfSetter {

	@Override
	public void process( Object line ) throws ConfigurationException {

		if ( !( line instanceof String ) )
			throw new ConfigurationException( "ServerAdmin: type String expected" );

		HttpdConf.SERVER_ADMIN = Utils.removeQuote( ( ( String ) line ) );
	}

}
