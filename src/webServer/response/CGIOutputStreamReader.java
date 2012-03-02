package webServer.response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraper class for InputStream
 * 
 */
public class CGIOutputStreamReader {

	private InputStream in;
	private String headerString;

	public CGIOutputStreamReader(InputStream in) {
		this.in = new BufferedInputStream(in);
	}

	private int getHeaderStringSize() throws IOException {

		if (!in.markSupported())
			return -1;
		in.mark(0);
		int c, count = 0;
		while ((c = in.read()) >= 0) {
			count++;
			if (((char) c) == '\n') {
				in.skip(1);
				if (((char) in.read()) == '\n') {
					break;
				}
				// This is for the byte that is skipped
				count++;
			}

		}
		in.reset();
		return count;
	}

	public String readHeaderString() throws IOException {
		if (headerString == null) {
			int offset = getHeaderStringSize();
			byte[] buf = new byte[offset];
			in.read(buf, 0, buf.length);
			headerString = new String(buf);
		}
		return headerString;
	}

	/**
	 * This method should call after getHeaderString.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readBodyContent() throws IOException {
		
		if ( headerString == null )
			readHeaderString();
		
		int len, size = 1024;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[size];
		in.skip(2);
		while ((len = in.read(buf, 0, size)) != -1)
			bos.write(buf, 0, len);
		buf = bos.toByteArray();
		return buf;
	}

}