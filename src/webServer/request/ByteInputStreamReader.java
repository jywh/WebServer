package webServer.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * <p>
 * A ByteInputStreamReader combines the functionality of BufferedReader and
 * BufferedInputStream. As the name suggest, a ByteInputStreamReader can both
 * read lines and read bytes.
 * </p>
 * 
 */
public class ByteInputStreamReader {

	private InputStream in;

	public ByteInputStreamReader(InputStream in) {
		this.in = new BufferedInputStream(in);
	}

	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Reads a line of text. A line is considered to be terminated by any one of
	 * a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 **/
	public String readLine() throws IOException {
		if (!in.markSupported())
			return null;
		int count = 0;
		in.mark(200);
		char c;
		while (in.available() > 0) {
			c = (char) in.read();
			if (c == '\r' || c == '\n')
				break;
			count++;
		}
		in.reset();
		byte[] buf = new byte[count];
		in.read(buf, 0, buf.length);
		// skip newline char, each char 2 bytes
		skip(2);
		// System.out.println(new String(buf));
		return new String(buf);
	}

	/**
	 * 
	 * A newline char is considered to be either a line feed ('\n'), a carriage
	 * return ('\r'), or a carriage return followed immediately by a linefeed.
	 * 
	 **/
	public void skipNewLineChar() throws IOException {
		if (!in.markSupported())
			return;
		int s;
		char c;
		while (in.available() > 0) {
			in.mark(1);
			s = in.read();
			c = (char) s;
			if (s == -1 || (c != '\n' && c != '\r'))
				break;
		}

		in.reset();

	}

	/**
	 * Close input stream.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		in.close();
	}

	/**
	 * Refer to BufferedInputStream read(byte[] b, int off, int len) method.
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	/**
	 * Refer to BufferedInputStream skip(long n) method.
	 * 
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	/**
	 * Convert the available input stream to byte array
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

	/**
	 * Convert certain size of input stream to byte array.
	 * 
	 * @param size The size of the input stream
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray(int size) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[size];
		int len = in.read(buf, 0, size);
		bos.write(buf, 0, len);
		buf = bos.toByteArray();
		return buf;
	}
}
