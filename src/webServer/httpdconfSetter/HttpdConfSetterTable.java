package webServer.httpdconfSetter;

import java.util.HashMap;

public class HttpdConfSetterTable {

	private static HashMap<String,HttpdConfSetter> setter = new HashMap<String, HttpdConfSetter>(20);
	
	public static void init(){
		
		setter.put("ServerRoot", new ServerRootSetter());
		setter.put("DocumentRoot", new DocumentRootSetter());
		setter.put("Listen", new ListenSetter());
		setter.put("LogFile", new LogFileSetter());
		setter.put("ScriptAlias", new ScriptAliasSetter());
		setter.put("Alias", new AliasSetter());
		setter.put("DirectoryIndex", new DirectoryIndexSetter());
		setter.put("Directory", new DirectorySetter());
		setter.put("CgiHandler", new CgiHandlerSetter());
		
	}
	
	public static HttpdConfSetter getSetter(String key){
		return setter.get(key);
	}
	
	
}
