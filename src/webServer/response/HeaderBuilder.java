package webServer.response;

import java.io.File;

import webServer.MIME;
import webServer.WebServer;
import webServer.constant.HeaderFields;
import webServer.ulti.Ulti;

public class HeaderBuilder {

	public static final String NEWLINE = "\r\n"; //System.getProperty("line.separator");

	private StringBuilder builder;
	
	public HeaderBuilder() {
		builder = new StringBuilder();
	}

	public HeaderBuilder append(String s) {
		builder.append(s);
		return this;
	}

	public HeaderBuilder buildHeaderBegin(String responsePhrase,
			String httpVersion) {
		builder.append(httpVersion).append(" ").append(responsePhrase)
				.append(NEWLINE).append(HeaderFields.DATE).append(": ")
				.append(Ulti.getTimeFull(System.currentTimeMillis()))
				.append(NEWLINE).append(HeaderFields.SERVER).append(": ")
				.append(WebServer.SERVER_NAME).append(NEWLINE);
		return this;
	}

	public HeaderBuilder buildContentTypeAndLength(File file) {
		String mime = MIME.getMIMEType(Ulti.getFileExtension(file));
		long length = file.length();
		buildContentLength((int)length).buildContentType(mime);
		return this;
	}

	public HeaderBuilder buildContentType(String mime){
		builder.append(HeaderFields.CONTENT_TYPE).append(": ")
		.append(mime).append(NEWLINE);
		return this;
	}
	
	public HeaderBuilder buildContentLength(int length) {
		builder.append(HeaderFields.CONTENT_LENGTH).append(": ").append(length)
				.append(NEWLINE);
		return this;
	}

	public HeaderBuilder buildLastModified(File file) {
		long lastModified = file.lastModified();
		builder.append(HeaderFields.LAST_MODIFIED).append(": ")
				.append(Ulti.getTimeFull(lastModified)).append(NEWLINE);
		return this;

	}

	public HeaderBuilder buildCacheControl(String how) {
		builder.append(HeaderFields.CACHE_CONTROL).append(": ").append(how)
				.append(NEWLINE);
		return this;
	}

	public HeaderBuilder buildConnection(boolean keepAlive) {
		builder.append(HeaderFields.CONNECTION).append(": ")
				.append((keepAlive) ? "keep-alive" : "close").append(NEWLINE);
		return this;
	}

	public HeaderBuilder buildExpireTime(long millisFromNow) {
		builder.append(HeaderFields.EXPIRE)
				.append(": ")
				.append(Ulti.getTimeFull(Ulti.currentTimeMillis()
						+ millisFromNow)).append(NEWLINE);
		return this;
	}

	public HeaderBuilder buildAuthentication(String authType, String realm) {
		builder.append(HeaderFields.WWW_AUTHENTICATE).append(": ")
				.append(authType).append(" realm=").append(realm)
				.append(NEWLINE);
		return this;
	}

	@Override
	public String toString() {
		System.out.println(builder.toString());
		return builder.toString();
	}

}
