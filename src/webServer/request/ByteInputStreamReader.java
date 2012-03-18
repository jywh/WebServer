package webServer.request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * <p>
 * A ByteInputStreamReader combines the functionality of BufferedReader and BufferedInputStream. As the name
 * suggest, a ByteInputStreamReader can both read lines and read bytes.
 * </p>
 * 
 */
public class ByteInputStreamReader {

	private BufferedInputStream in;

	public ByteInputStreamReader( InputStream in ) {
		this.in = new BufferedInputStream( in );
	}

	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'), a
	 * carriage return ('\r'), or a Carriage Return followed immediately by a LineFeed (CRLF).
	 * 
	 * @return A text line.
	 **/
	public String readLine() throws IOException {
		if ( !in.markSupported() )
			return null;
		int size = 0;
		in.mark( 200 );
		char c = ( char ) in.read();
		while ( c != '\r' && c != '\n' ) {
			size++;
			c = ( char ) in.read();
		}
		in.reset();
		byte[] buf = new byte[size];
		in.read( buf, 0, size );
		// skip CRLF
		skip( 2 );
		return new String( buf );
	}

	/**
	 * 
	 * A newline char is considered to be either a line feed ('\n'), a carriage return ('\r'), or a carriage
	 * return followed immediately by a linefeed.
	 * 
	 **/
	public void skipNewLineChar() throws IOException {
		if ( !in.markSupported() )
			return;
		int s;
		char c;
		while ( in.available() > 0 ) {
			in.mark( 2 );
			s = in.read();
			c = ( char ) s;
			if ( s == -1 || ( c != '\n' && c != '\r' ) )
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
	 * Refer to BufferedInputStream read(byte[] b, int off, int len).
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int read( byte[] b, int off, int len ) throws IOException {
		return in.read( b, off, len );
	}

	/**
	 * Refer to BufferedInputStream skip(long n).
	 * 
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public long skip( long n ) throws IOException {
		return in.skip( n );
	}

	/**
	 * Convert the available input stream to byte array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {

		int len, size = 1024;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[size];
			while ( in.available() > 0 ) {
				len = in.read( buf, 0, size );
				bos.write( buf, 0, len );
			}
			buf = bos.toByteArray();
			return buf;
		} finally {
			bos.close();
		}
	}

	/**
	 * Convert certain size of input stream to byte array.
	 * 
	 * @param size
	 *            The size of the input stream
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray( int size ) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[size];
			int len = in.read( buf, 0, size );
			bos.write( buf, 0, len );
			buf = bos.toByteArray();
			return buf;
		} finally {
			bos.close();
		}
	}
}
