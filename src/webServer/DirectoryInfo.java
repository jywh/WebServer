package webServer;

import java.util.List;

public class DirectoryInfo {
	public static final int USER = 1;
	public static final int GROUP = 2;
	public static final int VALID_USER = 3;
	
	private String authName;
	private String authType;
	private int userType;
	private List<String> validUsers;
	
	public DirectoryInfo(String authName,
			String authType, String userType,
			List<String> validUsers) {
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
	
	public String getAuthType(){
		return authType;
	}
	
	public int getUserType(){
		return userType;
	}
	
	public List<String> getUser(){
		return validUsers;
	}
}
