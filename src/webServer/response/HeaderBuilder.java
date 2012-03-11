package webServer.response;

import java.io.File;

import webServer.MIME;
import webServer.WebServer;
import webServer.constant.HeaderFields;
import webServer.constant.ResponseTable;
import webServer.ulti.Ulti;

/**
 * <p>
 * Helper class for building header message for response.
 * </p>
 * 
 */
public class HeaderBuilder {

	private static final String CRLF = "\r\n";

	private StringBuilder builder;

	public HeaderBuilder() {
		builder = new StringBuilder();
	}

	public HeaderBuilder append(String s) {
		builder.append(s);
		return this;
	}

	public HeaderBuilder buildHeaderBegin(int statusCode, String httpVersion) {
		builder.append(httpVersion).append(" ").append(ResponseTable.getResponsePhrase(statusCode))
				.append(CRLF).append(HeaderFields.DATE).append(": ")
				.append(Ulti.getTimeFull(System.currentTimeMillis())).append(CRLF)
				.append(HeaderFields.SERVER).append(": ").append(WebServer.SERVER_NAME).append(CRLF);
		return this;
	}

	public HeaderBuilder buildContentTypeAndLength(File file) {
		String mime = MIME.getMIMEType(Ulti.getFileExtension(file));
		long length = file.length();
		buildContentLength((int) length).buildContentType(mime);
		return this;
	}

	public HeaderBuilder buildContentType(String mime) {
		builder.append(HeaderFields.CONTENT_TYPE).append(": ").append(mime).append(CRLF);
		return this;
	}

	public HeaderBuilder buildContentLength(int length) {
		builder.append(HeaderFields.CONTENT_LENGTH).append(": ").append(length).append(CRLF);
		return this;
	}

	public HeaderBuilder buildLastModified(long date) {
		builder.append(HeaderFields.LAST_MODIFIED).append(": ").append(Ulti.getTimeFull(date))
				.append(CRLF);
		return this;

	}

	public HeaderBuilder buildCacheControl(String how) {
		builder.append(HeaderFields.CACHE_CONTROL).append(": ").append(how).append(CRLF);
		return this;
	}

	public HeaderBuilder buildConnection(String connection) {
		builder.append(HeaderFields.CONNECTION).append(": ").append(connection).append(CRLF);
		return this;
	}

	public HeaderBuilder buildExpireTime(long millisFromNow) {
		builder.append(HeaderFields.EXPIRE).append(": ")
				.append(Ulti.getTimeFull(Ulti.currentTimeMillis() + millisFromNow)).append(CRLF);
		return this;
	}

	public HeaderBuilder buildAuthentication(String authType, String realm) {
		builder.append(HeaderFields.WWW_AUTHENTICATE).append(": ").append(authType).append(" realm=")
				.append(realm).append(CRLF);
		return this;
	}

	@Override
	public String toString() {
		System.out.println(builder.toString());
		return builder.toString();
	}

}
