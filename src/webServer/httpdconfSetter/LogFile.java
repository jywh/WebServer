package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class LogFile extends HttpdConfSetter {

	@Override
	public void process( Object path ) throws ConfigurationException {

		if ( !( path instanceof String ) )
			throw new ConfigurationException( "LogFile: type String expected" );

		HttpdConf.LOG_FILE = Utils.removeQuote( ( ( String ) path ) );
	}

}
