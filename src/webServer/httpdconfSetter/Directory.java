package webServer.httpdconfSetter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

/**
 * <p>
 * Directory handles the Directory tag of httpd.conf file. It parses the body of
 * the tag and store the information to a SecureDirectory Object
 * </p>
 */
public class Directory extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {

		if (!(line instanceof List))
			throw new ConfigurationException("Directory: type ArrayList expect");

		@SuppressWarnings("unchecked")
		List<String> lines = (ArrayList<String>) line;
		String[] tokens;
		String secureDirectory = lines.get(0).substring(1,
				lines.get(0).length() - 1);
		String authName = null, authType = null, userType = null, authFile = null, user = null;
		for (int i = 1; i < lines.size(); i++) {
			tokens = lines.get(i).split(" ", 2);
			if (tokens[0].equals("AuthName")) {
				authName = tokens[1];
			} else if (tokens[0].equals("AuthType")) {
				authType = tokens[1];
			} else if (tokens[0].equals("AuthUserFile")) {
				authFile = tokens[1].substring(1, tokens[1].length() - 1);
			} else if (tokens[0].equals("require")) {
				userType = tokens[1];
				tokens = tokens[1].split(" ");
				user = (tokens.length > 1) ? tokens[1] : null;
			} else {
				throw new ConfigurationException(
						"Directory contains  unknown element");
			}
		}

		if (authName == null || authType == null || userType == null
				|| authFile == null)
			throw new ConfigurationException(
					"Configuration: Fail to read directory tag");
		List<String> users = retrieveAuthUser(userType, user, authFile);
		HttpdConf.secureUsers.put(secureDirectory, new SecureDirectory(
				secureDirectory, authName, authType, userType, users));
	}

	private List<String> retrieveAuthUser(String userType, String user,
			String path) throws ConfigurationException {
		if (userType.equals("valid-user")) {
			return readAuthUserFileForValidUser(path);
		} else {
			return readAuthFileForUser(path, user);
		}
	}

	/**
	 * All the users in sercure file are allowed to access this directory and
	 * its subdirectories.
	 * 
	 * @param path
	 * @return
	 * @throws ConfigurationException
	 */
	private List<String> readAuthUserFileForValidUser(String path)
			throws ConfigurationException {
		File file = new File(path);
		if (!file.exists())
			throw new ConfigurationException("File not found: " + path);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<String> result = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}

	/**
	 * Only the users that is listed are allowed to access this directory and
	 * its subdirectories.
	 * 
	 * @param path
	 * @param user
	 * @return
	 * @throws ConfigurationException
	 */
	private List<String> readAuthFileForUser(String path, String user)
			throws ConfigurationException {

		File file = new File(path);
		if (!file.exists())
			throw new ConfigurationException("File not found: " + path);
		List<String> users = Arrays.asList(user.split(","));
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<String> result = new ArrayList<String>();
			String line;
			String[] tokens;
			while ((line = reader.readLine()) != null) {
				tokens = line.split(":", 2);
				if (users.contains(tokens[0]))
					result.add(line);
			}

			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}

	}

	/**
	 * <p>
	 * A SecureDirectory holds all the information of a Directory tag.
	 * </p>
	 */
	public class SecureDirectory {

		public static final int USER = 1;
		public static final int GROUP = 2;
		public static final int VALID_USER = 3;

		private String path;
		private String authName;
		private String authType;
		private int userType;
		private List<String> validUsers;

		public SecureDirectory(String path, String authName, String authType,
				String userType, List<String> validUsers) {
			this.path = path;
			this.authName = authName;
			this.authType = authType;
			this.validUsers = validUsers;
			if (userType.equalsIgnoreCase("user")) {
				this.userType = USER;
			} else if (userType.equalsIgnoreCase("valid-user")) {
				this.userType = VALID_USER;
			} else {
				this.userType = GROUP;
			}
		}

		public SecureDirectory() {
		}

		public String getPath() {
			return path;
		}

		public String getAuthName() {
			return authName;
		}

		public String getAuthType() {
			return authType;
		}

		public int getUserType() {
			return userType;
		}

		public List<String> getUser() {
			return validUsers;
		}
	}

}
