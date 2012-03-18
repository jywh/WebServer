package webServer.response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * A CGIOutputStreamReader is created specifically for handling CGI output result.
 * 
 * <p>
 * Format of CGI output:</b></b>
 * 
 * Header Fields ( Directive ): which consists header field(s) that are needed to be sent back to client </b>
 * Blank line </b> Body </b>
 * </p>
 */
public class CGIOutputStreamReader {

	private BufferedInputStream in;
	private String headerString;

	public CGIOutputStreamReader( InputStream in ) {
		this.in = new BufferedInputStream( in );
	}

	/**
	 * Check for the offset of consecutive line terminators, where is the break of header string and body.
	 * 
	 * A line terminator consistes either a linefeed ('\n'), or a carriage return ('\r') or a carriage return
	 * followed inmediately by a linefeed.
	 * 
	 * @return The size of header string in bytes.
	 * @throws IOException
	 */
	public int getHeaderStringSize() throws IOException {

		if ( !in.markSupported() )
			return -1;
		in.mark( 200 );
		int count = 0;
		char c;
		while ( in.available() > 0 ) {
			count++;
			c = ( char ) in.read();
			if ( c == '\n' || c == '\r' ) {
				if ( in.available() > 0 ) {
					c = ( char ) in.read();
					if ( c == '\n' || c == '\r' )
						break;
				}
				count++;
			}
		}
		in.reset();
		return count;
	}

	/**
	 * Extract header string from input stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readHeaderString() throws IOException {
		if ( headerString == null ) {
			int offset = getHeaderStringSize();
			byte[] buf = new byte[offset];
			in.read( buf, 0, buf.length );
			headerString = new String( buf );
		}
		return headerString;
	}

	public void close() throws IOException {
		in.close();
	}

	/**
	 * Read the body content of the input stream, and return byte array.
	 * 
	 * @return Byte array of the content.
	 * @throws IOException
	 */
	public byte[] readBodyContent() throws IOException {
		ByteArrayOutputStream bos = null;
		try {
			int len, size = 1024;
			bos = new ByteArrayOutputStream();
			byte[] buf = new byte[size];
			while ( ( len = in.read( buf, 0, size ) ) > -1 )
				bos.write( buf, 0, len );
			buf = bos.toByteArray();
			return buf;
		} finally {
			bos.close();
		}

	}

}
