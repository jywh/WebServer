package webServer.response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import webServer.HttpdConf;
import webServer.ulti.Log;
import webServer.ulti.ServerException;

public class CGI {

	/**
	 * Execute the cgi script, save the output to a temp file.
	 * 
	 * @param file
	 * @param queryString
	 * @return { content-type, the path to the tempFile }
	 */
	public String[] execute(File file, String queryString,
			Map<String, String> headers) throws ServerException {
		Process process = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String scriptPath = reader.readLine();
			scriptPath = scriptPath.replace("#!", "");
			Log.debug("script path", scriptPath);
			ProcessBuilder pb = new ProcessBuilder(scriptPath,
					file.getAbsolutePath(), queryString);
			addEnvironmentVariables(pb.environment(), queryString, headers);
			process = pb.start();
			CountableInputStream cin = new CountableInputStream(
					process.getInputStream());
			return interpreteOutputStream(cin);

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(Response.INTERNAL_SERVER_ERROR,
					"Fail to execute python script");
		}

	}

	private void addEnvironmentVariables(Map<String, String> env,
			String queryString, Map<String, String> headers) {
		env.putAll(headers);
		env.put("QUERY_STRING", queryString);
	}

	private String[] interpreteOutputStream(CountableInputStream cin)
			throws IOException {

		int offset = cin.getOffset('\n');
		int size = cin.size();

		String firstLine = extractContentType(cin, offset);
		String tempFileName = writeStreamToFile(cin, size - offset);

		return new String[] { firstLine, tempFileName };
	}

	private String extractContentType(CountableInputStream cin, int offset)
			throws IOException {
		byte[] buf = new byte[offset];
		cin.read(buf, 0, buf.length);
		return new String(buf);
	}

	private String writeStreamToFile(CountableInputStream in, int size)
			throws IOException {

		File tempFile = createTempFile();
		OutputStream out = new FileOutputStream(tempFile);
		in.skip(3); // skip whitespace and newline characters
		byte[] buf = new byte[Response.BUFFER_SIZE];
		int read = -1;
		while ((read = in.read(buf)) >= 0) {
			out.write(buf, 0, read);
		}
		out.close();
		in.close();
		return tempFile.getAbsolutePath();
	}

	protected File createTempFile() {
		String name = Long.toString(System.currentTimeMillis());
		File tempDir = new File(HttpdConf.TEMP_DIRECTORY);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		return new File(tempDir, name);
	}
}
