package webServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class MIME {

	public static final String DEFAULT_MIME_TYPE = "text/html";

	private BufferedReader reader;

	private static HashMap<String, String> MIMETable = new HashMap<String, String>(
			1200, 0.9f);

	public MIME(String path) throws IOException {
		reader = new BufferedReader(new FileReader(path));
	}

	public MIME(File mimeFile) throws IOException {
		reader = new BufferedReader(new FileReader(mimeFile));
	}

	public void readMIMEType() throws IOException {
		String line;
		String[] tokens;
		int size = 0;
		
		while ((line = reader.readLine()) != null) {
			// skip comment and blink line
			if (line.trim().length() == 0 || line.charAt(0) == '#') {
				continue;
			}

			line = line.trim().replaceAll("[ \t]+", "#");
			tokens = line.split("#");
			if (tokens.length > 1) {
				size = tokens.length;
				for (int i = 1; i < size; i++) {
					MIMETable.put(tokens[i], tokens[0]);
				}
			}
		}

	}

	public static String getMIMEType(String extension) {
		String mime = MIMETable.get(extension);
		if (mime != null)
			return mime;
		else
			return DEFAULT_MIME_TYPE;
	}

	public void print() {
		Set<String> keys = MIMETable.keySet();
		String[] keyArray = keys.toArray(new String[keys.size()]);
		Arrays.sort(keyArray);
		for (String str : keyArray) {
			System.out.println(str + "---->" + MIMETable.get(str));
		}
		System.out.println("\n\nEnd of MIME");
	}

	public int length() {
		return MIMETable.size();
	}

}
