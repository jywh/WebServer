package webServer.response;

import java.io.File;

import webServer.MIME;
import webServer.WebServer;
import webServer.constant.HeaderFields;
import webServer.ulti.Ulti;

public class HeaderBuilder {

	private StringBuilder builder;

	public HeaderBuilder() {
		builder = new StringBuilder();
	}

	public HeaderBuilder buildHeaderBegin(String responsePhrase,
			String httpVersion) {
		builder.append(httpVersion).append(" ").append(responsePhrase)
				.append("\n").append(HeaderFields.DATE).append(": ")
				.append(Ulti.getTimeFull(System.currentTimeMillis()))
				.append("\n").append(HeaderFields.SERVER).append(": ")
				.append(WebServer.SERVER_NAME).append("\n");
		return this;
	}

	public HeaderBuilder buildContentTypeAndLength(File file) {
		String mime = MIME.getMIMEType(Ulti.getFileExtension(file.getName()));
		long length = file.length();
		builder.append(HeaderFields.CONTENT_LENGTH)
				.append(": ").append(length).append("\n")
				.append(HeaderFields.CONTENT_TYPE).append(": ").append(mime)
				.append("\n");
		return this;
	}

//	public HeaderBuilder buildContentTypeAndLength(long length, String contentType) {
//		builder.append(HeaderFields.CONTENT_LENGTH)
//				.append(": ").append(length).append("\n")
//				.append(contentType)
//				.append("\n");
//		return this;
//	}
	
	public HeaderBuilder buildContentLength(int length){
		builder.append(HeaderFields.CONTENT_LENGTH)
		.append(": ").append(length).append("\n");
		return this;
	}
	
	public HeaderBuilder buildLastModified(File file) {
		builder.append(HeaderFields.LAST_MODIFIED).append(": ")
				.append(lastModified(file)).append("\n");
		return this;

	}

	private String lastModified(File document) {
		long lastModified = document.lastModified();
		return Ulti.getTimeFull(lastModified);
	}

	public HeaderBuilder buildCacheControl(int howLong) {
		builder.append(HeaderFields.CACHE_CONTROL).append(": max-age=")
				.append(howLong).append("\n");
		return this;
	}

	public HeaderBuilder buildConnection(boolean keepAlive) {
		builder.append(HeaderFields.CONNECTION).append(": ")
				.append((keepAlive) ? "keep-alive" : "close").append("\n");
		return this;
	}

	public HeaderBuilder buildExpireTime(long millisFromNow) {

		builder.append(HeaderFields.EXPIRE)
				.append(": ")
				.append(Ulti.getTimeFull(Ulti.currentTimeMillis()
						+ millisFromNow)).append("\n");
		return this;
	}

	public HeaderBuilder append(String s){
		builder.append(s);
		return this;
	}
	@Override
	public String toString() {
		return builder.toString();
	}
	
}
