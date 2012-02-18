package webServer.httpdconfSetter;


public abstract class HttpdConfSetter {

	/**
	 * Take Object as parameter, so it is more felixiable when handle httpd.conf
	 * @param line
	 * @throws WrongTypeException if the Object is not 
	 */
	public abstract void process(Object line) throws WrongTypeException;
		
}
