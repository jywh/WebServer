package webServer.httpdconfSetter;

import java.util.ArrayList;
import java.util.List;

import webServer.ulti.ConfigurationException;

public class DirectorySetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {

		if(!(line instanceof List))
			throw new ConfigurationException("DirectorySetter: type ArrayList expect");
		
		List<String> lines = (ArrayList)line;
		// contiune deal with
		
	}

}
