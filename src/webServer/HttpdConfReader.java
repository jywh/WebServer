package webServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import webServer.httpdconfSetter.HttpdConfSetter;
import webServer.utils.ConfigurationException;

/**
 * 
 * <p>
 * A HttpdConfReader parses httpd.conf and call appropriate HttpdConfSetter to handle corresponding lines or
 * tags.
 * </p>
 * 
 */
public class HttpdConfReader {

	private BufferedReader reader;

	public HttpdConfReader( String path ) throws IOException {
		reader = new BufferedReader( new FileReader( path ) );
	}

	public HttpdConfReader( File confFile ) throws IOException {
		reader = new BufferedReader( new FileReader( confFile ) );
	}

	public void readHttpdConfFile() throws ConfigurationException {

		String currentLine;
		try {
			while ( ( currentLine = reader.readLine() ) != null ) {

				// trim white space at the beginning, the middle and the end
				currentLine = currentLine.trim().replaceAll( " +", " " );

				// skip comment and blink line
				if ( isCommentOrEmptyLine( currentLine ) )
					continue;

				// Check tag ('<>') which starts with '<'
				if ( currentLine.charAt( 0 ) == '<' )
					parseTag( currentLine );
				else
					parseLine( currentLine );

			}
		} catch ( IOException ioe ) {
			throw new ConfigurationException( "Fail to read httpdconf" );
		} finally {
			try {
				reader.close();
			} catch ( Exception e ) {
			}
		}
	}

	private boolean isCommentOrEmptyLine( String line ) {
		if ( line.length() == 0 || line.charAt( 0 ) == '#' ) {
			return true;
		}
		return false;
	}

	/*****************************************************************
	 * Parsing single line
	 *****************************************************************/

	private void parseLine( String currentLine ) throws ConfigurationException {
		String[] tokens = currentLine.split( " ", 2 );
		HttpdConfSetter httpdConfSetter = HttpdConfSetter.getInstance( tokens[0] );

		if ( httpdConfSetter != null )
			httpdConfSetter.process( tokens[1] );
	}

	/*****************************************************************
	 * Parsing tag <>
	 *****************************************************************/

	/**
	 * Processing lines between open tag ('<>') and close tag ('</>')
	 * 
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	private void parseTag( String currentLine ) throws IOException, ConfigurationException {

		if ( !checkOpenTag( currentLine ) )
			throw new ConfigurationException( "Illegal open tag: " + currentLine );

		String[] tokens = parseOpenTag( currentLine );

		HttpdConfSetter httpdConfSetter = HttpdConfSetter.getInstance( tokens[0] );

		if ( httpdConfSetter == null )
			return;

		List< String > lines = new ArrayList< String >();
		lines.add( tokens[1] );
		currentLine = readTagContent( lines );

		if ( !checkCloseTag( tokens[0], currentLine ) )
			throw new ConfigurationException( "Illegal close tag: " + currentLine );

		httpdConfSetter.process( lines );
	}

	/**
	 * Check the open tag.
	 * 
	 * @param line
	 * @return
	 */
	private boolean checkOpenTag( String line ) {

		String regex = "^<[a-zA-Z][a-zA-Z1-9]* .+>$";

		if ( line != null && line.trim().matches( regex ) )
			return true;

		return false;

	}

	/**
	 * Check close tag.
	 * 
	 * @param tag
	 * @param line
	 * @return
	 */
	private boolean checkCloseTag( String tag, String line ) {

		String regex = "^</" + tag + " *>$";

		if ( line != null && line.trim().matches( regex ) )
			return true;

		return false;
	}

	private String[] parseOpenTag( String line ) {
		// Eliminate <>
		line = line.substring( 1, line.length() - 1 ).trim();
		return line.split( " ", 2 );
	}

	/**
	 * 
	 * @param list
	 *            The list to store each tag content
	 * @return The line with close tag.
	 */
	private String readTagContent( List< String > list ) throws IOException {

		String currentLine = null;

		while ( ( currentLine = reader.readLine().trim() ) != null ) {

			if ( isCommentOrEmptyLine( currentLine ) )
				continue;

			if ( currentLine.charAt( 0 ) == '<' ) // reach close tag
				break;

			list.add( currentLine );
		}

		return currentLine;
	}

}
