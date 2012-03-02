package webServer;

import java.util.Map;

public class DirectoryInfo {
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
		} else if (userType.equalsIgnoreCase("valid-user")) {
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
