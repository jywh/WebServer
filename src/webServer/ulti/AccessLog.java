package webServer.ulti;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import webServer.constant.HttpdConf;
import webServer.request.Request;

public class AccessLog {

	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy :HH:mm:ss Z");

	public static void initialize() throws IOException {
		if (!verifyFile(HttpdConf.LOG_FILE)) {
			throw new IOException("Log file not found: " + HttpdConf.LOG_FILE);
		}
	}

	private static boolean verifyFile(String path) throws IOException {
		File logFile = new File(path);
		if (!logFile.exists()) {
			return logFile.createNewFile();
		}
		return true;
	}

	public void log(Request request, int statusCode) {
		String IP, requestLine, userId, rfc1413, time;
		IP = request.getIPAddr();
		requestLine = request.getScriptName();
		userId = "-";
		rfc1413 = "-";
		time = LOG_DATE_FORMAT.format(Ulti.currentTimeMillis());
		String content = String.format("%s %s %s [%s] \"%s\" %d", IP, rfc1413, userId, time, requestLine,
				statusCode);
		writeFile(content, HttpdConf.LOG_FILE);

	}

	public void writeFile(String content, String path) {

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
			synchronized (this) {
				out.write(content);
			}
			out.newLine();
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
