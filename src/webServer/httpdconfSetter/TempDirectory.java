package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class TempDirectory extends HttpdConfSetter {

	@Override
	public void process( Object line ) throws ConfigurationException {

		if ( !( line instanceof String ) )
			throw new ConfigurationException( "TempDirectory: type String expected" );
		HttpdConf.TEMP_DIRECTORY = Utils.removeQuote( ( ( String ) line ) );
	}

}
