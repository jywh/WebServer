package webServer.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import webServer.constant.HttpdConf;
import webServer.request.Request;
import webServer.utils.Utils;

public class AccessLog {

	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat( "dd/MMM/yyyy :HH:mm:ss Z" );

	public static void initialize() throws IOException {
		if ( !verifyFile( HttpdConf.LOG_FILE ) ) {
			throw new IOException( "Log file not found: " + HttpdConf.LOG_FILE );
		}
	}

	private static boolean verifyFile( String path ) throws IOException {
		File logFile = new File( path );
		if ( !logFile.exists() ) {
			return logFile.createNewFile();
		}
		return true;
	}

	public void log( Request request, int statusCode ) {
		String IP, requestLine, userId, rfc1413, time;
		IP = request.getIPAddr();
		requestLine = request.getScriptName();
		userId = ( request.getRemoteUser().isEmpty() ) ? "-" : request.getRemoteUser();
		rfc1413 = "-";
		time = LOG_DATE_FORMAT.format( Utils.currentTimeMillis() );
		String content = String.format( "%s %s %s [%s] \"%s\" %d", IP, rfc1413, userId, time, requestLine,
				statusCode );
		writeLog( content, HttpdConf.LOG_FILE );

	}

	private synchronized void writeLog( String content, String path ) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter( new FileWriter( path, true ) );
			out.write( content );
			out.newLine();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		} finally {
			try {
				out.close();
			} catch ( Exception ioe ) {

			}
		}
	}

}
