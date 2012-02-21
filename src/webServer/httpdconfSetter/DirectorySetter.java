package webServer.httpdconfSetter;

import java.util.ArrayList;

import webServer.ulti.ConfigurationException;

public class DirectorySetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {

		if(!(line instanceof ArrayList))
			throw new ConfigurationException("DirectorySetter: ArrayList");
		
		ArrayList<String> lines = (ArrayList)line;
		// contiune deal with
		
	}

}
