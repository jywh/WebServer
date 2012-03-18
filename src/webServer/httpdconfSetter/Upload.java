package webServer.httpdconfSetter;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

public class Upload extends HttpdConfSetter {

	@Override
	public void process( Object line ) throws ConfigurationException {

		if ( !( line instanceof String ) )
			throw new ConfigurationException( "UploadDirectory: type String expected" );

		HttpdConf.UPLOAD = Utils.removeQuote( ( ( String ) line ) );
	}

}
