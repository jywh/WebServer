package webServer.httpdconfSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import webServer.ulti.ConfigurationException;

public class Directory extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {

		if (!(line instanceof List))
			throw new ConfigurationException(
					"DirectorySetter: type ArrayList expect");

		List<String> lines = (ArrayList) line;
		// contiune deal with
		String secureDirectory = lines.get(0);
		String[] tokens;
		String authName="", authType="Basic", userType = "valid_user";  
		for ( int i=1; i < lines.size(); i++ ){
			tokens = lines.get(i).split(" ");
			if ( tokens[0].equals("AuthName")){
				authName = tokens[1];
			} else if ( tokens[0].equals("AuthType")){
				authType = tokens[1];
			} else if ( tokens[0].equals("AuthUserFile")){
				
			} else if ( tokens[0].equals("require")){
				
			} else {
				throw new ConfigurationException("Directory");
			}
		}
	}

}

class DirectoryInfo {

	public static final int USER = 1;
	public static final int GROUP = 2;
	public static final int VALID_USER = 3;
	
	private String secureDirectory;
	private String authName;
	private String authType;
	private int userType;
	private Map<String, String> validUsers;
	
	public DirectoryInfo(String secureDirectory, String authName,
			String authType, String userType,
			Map<String, String> validUsers) {
		this.secureDirectory = secureDirectory;
		this.authName = authName;
		this.authType = authType;
		this.validUsers = validUsers;
		if ( userType.equalsIgnoreCase("group")){
			this.userType = GROUP;
		} else if (userType.equalsIgnoreCase("valid_user")) {
			this.userType = VALID_USER;
		} else {
			this.userType = USER;
		}
	}
	
	public DirectoryInfo(){}
	
	public String getAuthName() {
		return authName;
	}
	
	public String getSecureDiretory() {
		return secureDirectory;
	}
	
	public String getAuthType(){
		return authType;
	}
	
	public int getUserType(){
		return userType;
	}
	
	public boolean validUser(String user){
		return validUsers.containsKey(user);
	}
	
	public String getUserPassword(String user){
		return validUsers.get(user);
	}
	
}
