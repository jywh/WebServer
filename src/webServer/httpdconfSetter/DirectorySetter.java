package webServer.httpdconfSetter;

import java.util.ArrayList;

public class DirectorySetter extends HttpdConfSetter {

	@Override
	public void process(Object line) throws WrongTypeException {

		if(!(line instanceof ArrayList))
			throw new WrongTypeException("DirectorySetter: ArrayList");
		
		ArrayList<String> lines = (ArrayList)line;
		// contiune deal with
		
	}

}
