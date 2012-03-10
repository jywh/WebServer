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

	/**
	 * Check for the offset of double '\n', where is the break of header string
	 * and body
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getHeaderStringSize() throws IOException {

		if (!in.markSupported())
			return -1;
		in.mark(1);
		int c, count = 0;
		char s;
		while ((c = in.read()) >= 0) {
			count++;
			s = (char) c;
			if (s == '\n' || s == '\r') {
				if ((c = in.read()) >= 0) {
					s = (char) c;
					if (s == '\n' || s == '\r') {
						break;
					}
				}
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

	public void close() throws IOException{
		in.close();
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readBodyContent() throws IOException {

		if (headerString == null)
			readHeaderString();
		int len, size = 1024;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[size];
		while ((len = in.read(buf, 0, size)) > -1)
			bos.write(buf, 0, len);
		buf = bos.toByteArray();
		return buf;
	}

}
