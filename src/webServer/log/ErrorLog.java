package webServer.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import webServer.constant.HttpdConf;
import webServer.utils.ConfigurationException;
import webServer.utils.Utils;

/**
 * Redirect error message to error log is it is not in debug mode.
 * 
 * @author Wenhui
 * 
 */
public class ErrorLog {

	private static final String ERROR_LOG = HttpdConf.SERVER_ROOT + "/logs/";
	private static final DateFormat DATE_FORMATE = new SimpleDateFormat( "d_MMM_yyyy" );
	private static PrintStream err = null;

	public static void setup() throws ConfigurationException {

		// In debug mode, print all the error message to console.
		if ( Log.DEBUG )
			return;

		String errorLogName = "error_" + DATE_FORMATE.format( Utils.currentTimeMillis() ) + ".txt";
		File errorLog = new File( ERROR_LOG, errorLogName );

		if ( !errorLog.exists() ) {
			try {
				errorLog.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
				throw new ConfigurationException( "Fail to create error log file" );
			}
		}

		try {
			err = new PrintStream( errorLog, "UTF-8" );
			// Redirect err stream to error log file
			System.setErr( err );
			String initialMessage = "[" + Utils.getTimeFull( Utils.currentTimeMillis() ) + "]:";
			System.err.println( initialMessage );
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new ConfigurationException( "Error setup err output stream" );
		}

	}

	public static void close() {
		if ( err != null )
			err.close();
	}

}
