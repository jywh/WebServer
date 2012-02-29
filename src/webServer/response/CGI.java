package webServer.response;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import webServer.WebServer;
import webServer.constant.EnvVarTable;
import webServer.constant.ResponseTable;
import webServer.request.Request;
import webServer.ulti.ServerException;

public class CGI {

	public CountableInputStream execute(Request request) throws ServerException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					request.getURI()));
			String scriptPath = reader.readLine();
			scriptPath = scriptPath.replace("#!", "");
			ProcessBuilder pb = new ProcessBuilder(scriptPath, request.getURI());
			addEnvironmentVariables(pb.environment(), request);
			Process process = pb.start();

			if (request.getMethod().equals(Request.POST)
					|| request.getMethod().equals(Request.PUT)) {
				servePostParameters(process, request);
			}

			return new CountableInputStream(process.getInputStream());

		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(ResponseTable.INTERNAL_SERVER_ERROR,
					"Fail to execute script");
		}

	}

	private void addEnvironmentVariables(Map<String, String> env,
			Request request) {

		if (request.getMethod().equals(Request.GET)
				|| request.equals(Request.HEAD)) {
			env.put(EnvVarTable.QUERY_STRING, request.getParameterString());
		}
		addNonHeaderFieldEnvVar(env, request);
		addHeaderFieldsEnvVar(env, request.getRequestField());

	}

	private void addNonHeaderFieldEnvVar(Map<String, String> env,
			Request request) {
		env.put(EnvVarTable.SERVER_NAME, WebServer.SERVER_NAME);
		env.put(EnvVarTable.SERVER_SOFTWARE, WebServer.SERVER_SOFTWARE);
		env.put(EnvVarTable.GATEWAY_INTERFACE, WebServer.GATEWAY_INTERFACE);
		env.put(EnvVarTable.SERVER_PORT,
				Integer.toString(request.getRemotePort()));
		env.put(EnvVarTable.REMOTE_ADDR, request.getIPAddr());
		env.put(EnvVarTable.SERVER_PROTOCOL, request.getHttpVersion());
		env.put(EnvVarTable.REQUEST_METHOD, request.getMethod());
		env.put(EnvVarTable.PATH_INFO, request.getPathInfo());
		env.put(EnvVarTable.SCRIPT_NAME, request.getScriptName());
		env.put(EnvVarTable.PATH_TRANSLATED, request.getURI());
	}

	private void addHeaderFieldsEnvVar(Map<String, String> env,
			Map<String, String> headers) {
		Set<String> keySet = headers.keySet();
		for (String key : keySet) {
			if (EnvVarTable.containKey(key)) {
				env.put(EnvVarTable.get(key), headers.get(key));
			} else {
				env.put(key.replace('-', '_').toUpperCase(), headers.get(key));
			}
		}
	}

	private void servePostParameters(Process p, Request request)
			throws IOException {
		BufferedOutputStream args = new BufferedOutputStream(
				p.getOutputStream());
		char[] input = request.getParameterString().toCharArray();
		for (char c : input)
			args.write((int) c);
		args.close();

	}

	// private String[] interpreteOutputStream(CountableInputStream cin)
	// throws IOException, ServerException {
	//
	// int offset = cin.getOffset('\n');
	//
	// String firstLine = extractDirective(cin, offset);
	// String[] tokens = firstLine.split(":", 2);
	// if (tokens[0].equalsIgnoreCase("Content-Type")) {
	// String tempFileName = writeStreamToFile(cin);
	// return new String[] { firstLine, tempFileName };
	// }
	//
	// // Handle Location and StatusCode right here
	// throw new ServerException(ResponseTable.NOT_IMPLEMENTED,
	// "Unsupport Directive: " + tokens[0]);
	//
	// }
	//
	// private String extractDirective(CountableInputStream cin, int offset)
	// throws IOException {
	// byte[] buf = new byte[offset];
	// cin.read(buf, 0, buf.length);
	// return new String(buf);
	// }
	//
	// private String writeStreamToFile(CountableInputStream in)
	// throws IOException {
	//
	// File tempFile = createTempFile();
	// OutputStream out = new FileOutputStream(tempFile);
	// in.skip(3); // skip whitespace and newline characters
	// byte[] buf = new byte[Response.BUFFER_SIZE];
	// int read = -1;
	// while ((read = in.read(buf)) >= 0) {
	// out.write(buf, 0, read);
	// }
	// out.close();
	// in.close();
	// return tempFile.getAbsolutePath();
	// }
	//
	// protected File createTempFile() {
	// String name = Long.toString(System.currentTimeMillis());
	// File tempDir = new File(HttpdConf.TEMP_DIRECTORY);
	// if (!tempDir.exists()) {
	// tempDir.mkdirs();
	// }
	// return new File(tempDir, name);
	// }
}
