package webServer.ulti;

import webServer.request.Request;

public class AccessLog {

	public void log(Request request, int statusCode){
		String IP, requestLine, userId, rfc1413, time;
		IP = request.getIPAddr();
		requestLine = request.getScriptName();
		userId = "-";
		rfc1413 = "-";
		time = Ulti.timeInLogFormat();
		String content=String.format("%s %s %s [%s] \"%s\" %d", IP, rfc1413, userId, time,
				requestLine, statusCode);
		Log.access(content);
		
	}

}
