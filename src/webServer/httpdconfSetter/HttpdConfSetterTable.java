package webServer.httpdconfSetter;

import java.util.HashMap;

public class HttpdConfSetterTable {

	private static HashMap<String,HttpdConfSetter> setter = new HashMap<String, HttpdConfSetter>(20);
	
	public static void init(){
		
		setter.put("ServerRoot", new ServerRoot());
		setter.put("DocumentRoot", new DocumentRoot());
		setter.put("Listen", new Listen());
		setter.put("LogFile", new LogFile());
		setter.put("ScriptAlias", new ScriptAlias());
		setter.put("Alias", new Alias());
		setter.put("DirectoryIndex", new DirectoryIndex());
		setter.put("Directory", new Directory());
		setter.put("CgiHandler", new CgiHandler());
		setter.put("TempDirectory", new TempDirectory());
		
	}
	
	public static HttpdConfSetter getSetter(String key){
		return setter.get(key);
	}
	
	
}
