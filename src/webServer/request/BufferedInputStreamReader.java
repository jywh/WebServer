package webServer.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Since BufferedRead can not read bytes, so I write this class that can both
 * readLine and read bytes. I also provide method to convert the rest stream to
 * byte[] which can be easily write to any output stream.
 * 
 */
public class BufferedInputStreamReader {

	private InputStream in;

	public BufferedInputStreamReader(InputStream in) {
		this.in = new BufferedInputStream(in);
	}

	public int read() throws IOException {
		return in.read();
	}

	/**
	 * 
	 * Reads a line of text. A line is considered to be terminated by any one of
	 * a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 **/
	public String readLine() throws IOException {
		char c = (char) in.read();
		StringBuilder builder = new StringBuilder();
		while (c != '\r' && c != '\n') {
			builder.append(c);
			c = (char) in.read();
		}
		skipLineFeed();
		// System.out.println(builder.toString());
		return builder.toString();
	}

	private void skipLineFeed() throws IOException {
		if (in.markSupported()) {
			in.mark(1);
			char c = (char) in.read();
			if (c != '\n') 
				in.reset();
		}

	}

	public void close() throws IOException {
		in.close();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	/**
	 * Convert the rest of the input stream to byte[]
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {
		int len, size = 1024;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[size];
		while (in.available() > 0) {
			len = in.read(buf, 0, size);
			bos.write(buf, 0, len);
		}
		buf = bos.toByteArray();
		return buf;
	}
}
