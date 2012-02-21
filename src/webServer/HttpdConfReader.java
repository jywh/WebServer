package webServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import webServer.httpdconfSetter.HttpdConfSetter;
import webServer.httpdconfSetter.SetHttpdConf;
import webServer.ulti.ConfigurationException;

public class HttpdConfReader {

	private BufferedReader reader;
	private String currentLine;
	private String[] tokens;
	private ArrayList<String> lines = new ArrayList<String>();
	private HttpdConfSetter httpdConfSetter;

	public HttpdConfReader(String path) throws IOException {
		reader = new BufferedReader(new FileReader(path));

	}

	public HttpdConfReader(File confFile) throws IOException {
		reader = new BufferedReader(new FileReader(confFile));
	}

	/**
	 * 
	 * @param path
	 */
	public void readHttpdConfFile() throws IOException, ConfigurationException {

		SetHttpdConf.init();
		currentLine = reader.readLine();

		while (currentLine != null) {

			// trim white space at the beginning, middle and end
			currentLine = currentLine.trim().replaceAll(" +", " ");

			// skip comment and blink line
			if (isCommentOrEmtpryLine(currentLine)) {
				currentLine = reader.readLine();
				continue;
			}

			// Check tag which start with <>
			if (currentLine.charAt(0) == '<') {
				processTag();
				currentLine = reader.readLine();
				continue;
			}

			tokens = currentLine.split(" ", 2);
			httpdConfSetter = SetHttpdConf.getSetter(tokens[0]);

			if (httpdConfSetter != null)
				httpdConfSetter.process(tokens[1]);

			currentLine = reader.readLine();

		}
	}

	private boolean isCommentOrEmtpryLine(String line) {
		if (line.length() == 0 || line.charAt(0) == '#') {
			return true;
		}
		return false;
	}

	/**
	 * Processing any line with <> and </>
	 * 
	 * @throws ConfigurationException
	 * 
	 * @throws Exception
	 */
	private void processTag() throws IOException, ConfigurationException {

		if (!currentLine.endsWith(">")) {
			throw new ConfigurationException("<> not paired");
		}

		// Error: close tag before open it
		if (currentLine.charAt(1) == '/') {
			throw new ConfigurationException("<> close tag before open");
		}

		currentLine = currentLine.substring(1, currentLine.length() - 1).trim();
		tokens = currentLine.split(" ", 2);

		httpdConfSetter = SetHttpdConf.getSetter(tokens[0]);
		lines.clear();
		lines.add(tokens[1]);
		currentLine = reader.readLine().trim();

		while (currentLine != null) {

			if (isCommentOrEmtpryLine(currentLine)) {
				currentLine = reader.readLine().trim();
				continue;
			}

			if (currentLine.charAt(0) == '<') {
				break;
			}

			lines.add(currentLine);
			currentLine = reader.readLine().trim();
		}

		if (currentLine == null) {
			throw new ConfigurationException("Tag is not closed");
		}

		currentLine = currentLine.replaceAll(" +", "");

		if (!currentLine.equals("</" + tokens[0] + ">")) {
			throw new ConfigurationException("Close tag not match open tag");
		}

		httpdConfSetter.process(lines);
	}

	public void testPrint() {
		System.out.println("ServerRoot: " + HttpdConf.SERVER_ROOT);
		System.out.println("DocumentRoot: " + HttpdConf.DOCUMENT_ROOT);
		System.out.println("ListenPort: " + HttpdConf.LISTEN);
		System.out.println("LogFile: " + HttpdConf.LOG_FILE);
		System.out.println("ScriptAlias: " + HttpdConf.SCRIPT_ALIAS + " "
				+ HttpdConf.ALIAS.get(HttpdConf.SCRIPT_ALIAS));
	}

	public static void main(String[] args) {
		try {
			File file = new File(".");
			System.out.println(file.getAbsolutePath());
			HttpdConfReader reader = new HttpdConfReader(
					"src/Sample Files/httpd.conf");
			reader.readHttpdConfFile();
			reader.testPrint();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		} catch (Exception e){
			
		}

	}
}
