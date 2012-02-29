package webServer.response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraper class for InputStream
 * 
 */
public class CountableInputStream {

	private InputStream in;

	public CountableInputStream(InputStream in) {
		this.in = new BufferedInputStream(in);
	}

	/**
	 * Get the first offset of character c in the inputstream
	 * 
	 * @param c
	 * @return The offset of char c, -1 if fails.
	 * @throws IOException
	 */
	public int getOffset(char c) throws IOException {

		if (!in.markSupported())
			return -1;
		in.mark(0);
		int count = 0;
		while (((char) in.read()) != c) {
			count++;
		}
		in.reset();
		return count;
	}

	private int getEndHeaderOffset() throws IOException {

		if (!in.markSupported())
			return -1;

		in.mark(0);
		int c, count = 0;
		while ((c = in.read()) >= 0) {
			count++;
			// System.out.print((char)c);
			if (((char) c) == '\n') {
				skip(1);
				if (((char) in.read()) == '\n') {
					System.out.print("Get the offset ");
					break;
				}
				count ++;
			}
			
		}
		in.reset();
		return count;
	}

	public String getHeaderString() throws IOException{
		int offset = getEndHeaderOffset();
		byte[] buf = new byte[offset];
		in.read(buf, 0, buf.length);
		return new String(buf);
	}
	
	/**
	 * This method should call after getHeaderString.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readBodyContent() throws IOException {

		int len;
		int size = 1024;
		byte[] buf;
		in.skip(2);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		buf = new byte[size];
		while ((len = in.read(buf, 0, size)) != -1)
			bos.write(buf, 0, len);
		buf = bos.toByteArray();
		return buf;
	}

	public InputStream getInputStream() {
		return in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public void close() throws IOException {
		in.close();
	}

	public void skip(long n) throws IOException {
		in.skip(n);
	}

}
