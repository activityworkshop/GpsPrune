package tim.prune.function.gpsies;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.io.OutputStream;

/**
 * Taken from the Client HTTP Request class at com.myjavatools.web
 * and subsequently simplified and modified
 * @author Vlad Patryshev
 */
public class FormPoster
{
	private URLConnection _connection = null;
	private OutputStream _os = null;
	private static final Random RANDOM_GEN = new Random();
	private static final String BOUNDARY = "---------------------------"
		+ randomString() + randomString() + randomString();


	/** Connect (if not already connected) */
	protected void connect() throws IOException {
		if (_os == null) _os = _connection.getOutputStream();
	}

	/** Write a single character */
	protected void write(char c) throws IOException {
		connect();
		_os.write(c);
	}

	/** Write a string */
	protected void write(String s) throws IOException {
		connect();
		_os.write(s.getBytes());
	}

	/** Write a -r-n newline sequence */
	protected void newline() throws IOException {
		write("\r\n");
	}

	/** Write a string followed by a newline */
	protected void writeln(String s) throws IOException {
		write(s);
		newline();
	}

	/** Generate a random alphanumeric string */
	private static String randomString() {
		return Long.toString(RANDOM_GEN.nextLong(), 36);
	}

	/** Write a boundary marker */
	private void boundary() throws IOException {
		write("--");
		write(BOUNDARY);
	}


	/**
	 * Creates a new multipart POST HTTP request for a specified URL
	 * @param url the URL to send request to
	 * @throws IOException
	 */
	public FormPoster(URL inUrl) throws IOException
	{
		_connection = inUrl.openConnection();
		_connection.setDoOutput(true);
		_connection.setRequestProperty("Content-Type",
			"multipart/form-data; boundary=" + BOUNDARY);
	}

	/** Write a header with the given name */
	private void writeName(String inName) throws IOException
	{
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(inName);
		write('"');
	}

	/**
	 * adds a string parameter to the request
	 * @param name parameter name
	 * @param value parameter value
	 * @throws IOException
	 */
	public void setParameter(String inName, String inValue) throws IOException
	{
		boundary();
		writeName(inName);
		newline(); newline();
		writeln(inValue);
	}

	/** Pipe the contents of the input stream to the output stream */
	private static void pipe(InputStream in, OutputStream out) throws IOException
	{
		byte[] buf = new byte[500000];
		int nread;
		synchronized (in) {
			while((nread = in.read(buf, 0, buf.length)) >= 0) {
				out.write(buf, 0, nread);
			}
		}
		out.flush();
		buf = null;
	}

	/**
	 * adds a file parameter to the request
	 * @param inName parameter name
	 * @param inFilename the name of the file
	 * @param inStream input stream to read the contents of the file from
	 * @throws IOException
	 */
	public void setParameter(String inName, String inFilename, InputStream inStream) throws IOException
	{
		boundary();
		writeName(inName);
		write("; filename=\"");
		write(inFilename);
		write('"');
		newline();
		write("Content-Type: ");
		String type = URLConnection.guessContentTypeFromName(inFilename);
		if (type == null) {type = "application/octet-stream";}
		writeln(type);
		newline();
		pipe(inStream, _os);
		newline();
	}

	/**
	 * posts the requests to the server
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post() throws IOException
	{
		boundary();
		writeln("--");
		_os.close();
		return _connection.getInputStream();
	}

	/**
	 * @return the HTTP response code, 200 for success or -1 if not available
	 */
	public int getResponseCode() throws IOException
	{
		if (_connection != null && _connection instanceof HttpURLConnection) {
			return ((HttpURLConnection) _connection).getResponseCode();
		}
		return -1;
	}
}
