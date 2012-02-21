package webServer.httpdconfSetter;

import webServer.ulti.ConfigurationException;


public abstract class HttpdConfSetter {

	/**
	 * Take Object as parameter, so it is more felixiable when handle httpd.conf configuration
	 * 
	 * @param line
	 * @throws ConfigurationException if the Object is not 
	 */
	public abstract void process(Object line) throws ConfigurationException;
		
}
