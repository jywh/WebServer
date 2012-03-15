package webServer.httpdconfSetter;

import webServer.utils.ConfigurationException;

public abstract class HttpdConfSetter {

	private static String packageName = null;

	/**
	 * Take Object as parameter, so it is more felixiable when handle httpd.conf
	 * configuration
	 * 
	 * @param line
	 * @throws ConfigurationException
	 */
	public abstract void process(Object line) throws ConfigurationException;

	/**
	 * Instaniate subclass object of HttpdConfSetter, the tag name must match
	 * class name.
	 * 
	 * @param className The class to be instaniated
	 * @return
	 */
	public static HttpdConfSetter getInstance(String className) {
		try {
			if (packageName == null) {
				String name = HttpdConfSetter.class.getName();
				int indexLastDot = name.lastIndexOf('.');
				packageName = name.substring(0, indexLastDot + 1);
			}
			return (HttpdConfSetter) Class.forName(packageName + className).newInstance();
		} catch (Exception e) {
			System.out.println("[Error] Class not found: " + packageName + className);
		}
		return null;
	}
}
