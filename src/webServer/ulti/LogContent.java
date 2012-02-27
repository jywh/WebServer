package webServer.ulti;

public class LogContent {

	private String IP, requestLine, userId, time, rfc1413;
	private int statusCode;
	private long length;

	public LogContent() {
		IP = "-";
		requestLine = "-";
		userId = "-";
		time = "-";
		rfc1413 = "-";
		statusCode = 0;
		length = 0L;
	}

	public void setIP(String aIP) {
		this.IP = aIP;
	}

	public void setRequestLine(String aRequestLine) {
		this.requestLine = aRequestLine;
	}

	public void setUserId(String aUserId) {
		this.userId = aUserId;
	}

	public void setTime(String aTime) {
		this.time = aTime;
	}

	public void setRfc1413(String aRfc1413) {
		this.rfc1413 = aRfc1413;
	}

	public void setStatusCode(int aStatusCode) {
		this.statusCode = aStatusCode;
	}

	public void setLength(long aLength) {
		this.length = aLength;
	}

	public String getIP() {
		return IP;
	}

	public String getRequestLine() {
		return requestLine;
	}

	public String getUserId() {
		return userId;
	}

	public String getTime() {
		return time;
	}

	public String getRFC1413() {
		return rfc1413;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public long getLength() {
		return length;
	}

	public String getLogContent() {
		return String.format("%s %s %s [%s] \"%s\" %d %d", IP, rfc1413, userId, time,
				requestLine, statusCode, length);
	}
	
}
