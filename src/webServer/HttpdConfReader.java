package webServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import webServer.httpdconfSetter.HttpdConfSetter;
import webServer.httpdconfSetter.HttpdConfSetterTable;
import webServer.ulti.ConfigurationException;

public class HttpdConfReader {

	private BufferedReader reader;

	public HttpdConfReader(String path) throws IOException {
		reader = new BufferedReader(new FileReader(path));
		HttpdConfSetterTable.init();

	}

	public HttpdConfReader(File confFile) throws IOException {
		reader = new BufferedReader(new FileReader(confFile));
		HttpdConfSetterTable.init();
	}

	/**
	 * 
	 * @param path
	 */
	public void readHttpdConfFile() throws IOException, ConfigurationException {

		String currentLine;
		
		while ( ( currentLine = reader.readLine()) != null) {

			// trim white space at the beginning, the middle and the end
			currentLine = currentLine.trim().replaceAll(" +", " ");

			// skip comment and blink line
			if (isCommentOrEmptyLine(currentLine)) {
				continue;
			}

			// Check tag which starts with <>
			if (currentLine.charAt(0) == '<') {
				parseTag(currentLine);
				continue;
			}
			
			parseLine(currentLine);

		}
	}

	private boolean isCommentOrEmptyLine(String line) {
		if (line.length() == 0 || line.charAt(0) == '#') {
			return true;
		}
		return false;
	}

	/*****************************************************************
	 * 
	 * Parsing single line
	 * 
	 *****************************************************************/
	
	protected void parseLine(String currentLine) throws ConfigurationException{
		String[] tokens = currentLine.split(" ", 2);
		HttpdConfSetter httpdConfSetter = HttpdConfSetter.instaniate(tokens[0]);

		if (httpdConfSetter != null)
			httpdConfSetter.process(tokens[1]);
	}
	
	/*****************************************************************
	 * 
	 * Parsing tag <>
	 * 
	 *****************************************************************/
	
	/**
	 * Processing any line with <> and </>
	 * 
	 * @throws ConfigurationException
	 * 
	 * @throws IOException
	 */
	private void parseTag(String currentLine) throws IOException, ConfigurationException {

		if( !checkOpenTag(currentLine))
			throw new ConfigurationException("Illegal open tag: "+currentLine);

		String[] tokens = parseOpenTag(currentLine);

		HttpdConfSetter httpdConfSetter = HttpdConfSetter.instaniate(tokens[0]);
		
		if( httpdConfSetter == null ) return;
		
		List<String> lines = new ArrayList<String>();
		lines.add(tokens[1]);
		currentLine = readTagContent(lines);

		if(!checkCloseTag(tokens[0], currentLine)){
			throw new ConfigurationException("Illegal close tag: "+currentLine);
		}

		httpdConfSetter.process(lines);
	}

	/**
	 * Check the open tag. It should be HTML/XML style.
	 * 
	 * @param line
	 * @return
	 */
	private boolean checkOpenTag(String line) {
		
		String regex = "^<[a-zA-Z][a-zA-Z1-9]* .+>$";
		
		if ( line != null && line.trim().matches(regex) ) 
			return true;
		
		return false;
	
	}
	
	/**
	 * Check close tag. It should be HTML/XML style
	 * 
	 * @param tag
	 * @param line
	 * @return
	 */
	private boolean checkCloseTag(String tag, String line){
		
		String regex = "^</"+tag+" *>$";
		
		if ( line != null && line.trim().matches(regex) ) 
			return true;

		return false;
	}
	
	private String[] parseOpenTag(String line){
		// Eliminate <>
		line = line.substring(1, line.length() - 1).trim();
		return line.split(" ", 2);
	}
	
	/**
	 * 
	 * 
	 * @param list The list to store each tag content
	 * @return The last line being read
	 */
	private String readTagContent(List<String> list) throws IOException {
		
		String currentLine = reader.readLine().trim();

		while (currentLine != null) {

			if (isCommentOrEmptyLine(currentLine)) {
				currentLine = reader.readLine().trim();
				continue;
			}

			if (currentLine.charAt(0) == '<') {
				break;
			}

			list.add(currentLine);
			currentLine = reader.readLine().trim();
		}
		
		return currentLine;
	}
	
}
