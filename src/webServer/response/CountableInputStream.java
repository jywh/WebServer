package webServer.response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Wraper class for InputStream
 * 
 * @author Wenhui
 *
 */
public class CountableInputStream {

	private InputStream in;
	
	public CountableInputStream(InputStream in){
		this.in = new BufferedInputStream(in);
	}
	
	/**
	 * Get the first offset of character c in the inputstream
	 * 
	 * @param c
	 * @return The offset of char c, -1 if fails.
	 * @throws IOException 
	 */
	public int getOffset(char c) throws IOException{
		
		if( !in.markSupported() ) return -1;
		in.mark(0);
		int count=0;
		while ( ((char)in.read()) != c ){
			count++;
		}
		in.reset();
		return count;
	}
	
	/**
	 * 
	 * 
	 * @return Size of the inputStream
	 * @throws IOException 
	 */
	public int size() throws IOException{
		
		if( !in.markSupported() ) return -1;
		
		in.mark(0);
		int count = 0;
		while(in.read() >= 0){
			count++;
		}
		
		in.reset();
		return count;
	}
	
	public InputStream getInputStream(){
		return in;
	}
	
	public int read() throws IOException{
		return in.read();
	}
	
	public int read(byte[] b) throws IOException{
		return in.read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}
	
	public void close() throws IOException{
		in.close();
	}
	
	public void skip(long n) throws IOException{
		in.skip(n);
	}
	
	public String toString(){

		try {
			in.mark(0);
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ( reader.ready() ){
				builder.append((char)reader.read());
			}
			in.reset();
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} 
		
	}
}
