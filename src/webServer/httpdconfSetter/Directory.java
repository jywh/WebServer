package webServer.httpdconfSetter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import webServer.DirectoryInfo;
import webServer.constant.HttpdConf;
import webServer.ulti.ConfigurationException;

public class Directory extends HttpdConfSetter {

	@Override
	public void process(Object line) throws ConfigurationException {

		if (!(line instanceof List))
			throw new ConfigurationException(
					"DirectorySetter: type ArrayList expect");

		@SuppressWarnings("unchecked")
		List<String> lines = (ArrayList<String>) line;
		String[] tokens;
		String secureDirectory = lines.get(0).substring(1,
				lines.get(0).length() - 1);
		String authName = null, authType = null, userType = null, authFile = null, user = null;
		for (int i = 1; i < lines.size(); i++) {
			tokens = lines.get(i).split(" ", 2);
			if (tokens[0].equals("AuthName")) {
				authName = tokens[1].substring(1, tokens[1].length() - 1);
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
		Map<String, String> users = retrieveAuthUser(userType, user, authFile);
		HttpdConf.secureUsers.add(new DirectoryInfo(secureDirectory, authName,
				authType, userType, users));
	}

	private Map<String, String> retrieveAuthUser(String userType, String user,
			String path) throws ConfigurationException {
		if (userType.equals("valid-user")) {
			return readAuthUserFileForValidUser(path);
		} else {
			return readAuthFileForUser(path, user);
		}
	}

	private Map<String, String> readAuthUserFileForValidUser(String path)
			throws ConfigurationException {
		File file = new File(path);
		if (!file.exists())
			throw new ConfigurationException("File not found: " + path);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Map<String, String> result = new HashMap<String, String>();
			String line;
			String[] tokens;
			while ((line = reader.readLine()) != null) {
				tokens = line.split(":", 2);
				result.put(tokens[0], tokens[1]);
			}

			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}

	private Map<String, String> readAuthFileForUser(String path, String user)
			throws ConfigurationException {

		File file = new File(path);
		if (!file.exists())
			throw new ConfigurationException("File not found: " + path);
		List<String> users = Arrays.asList(user.split(","));
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Map<String, String> result = new HashMap<String, String>();
			String line;
			String[] tokens;
			while ((line = reader.readLine()) != null) {
				tokens = line.split(":", 2);
				if (users.contains(tokens[0]))
					result.put(tokens[0], tokens[1]);
			}

			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}

	}

}
