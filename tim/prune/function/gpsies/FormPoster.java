package tim.prune.function.gpsies;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.io.OutputStream;
import java.io.FileInputStream;

/**
 * Taken from Client HTTP Request class
 * @author Vlad Patryshev
 */
public class FormPoster
{
	private URLConnection _connection;
	private OutputStream _os = null;
	private static final Random random = new Random();
	private static final String boundary = "---------------------------"
		+ randomString() + randomString() + randomString();


	protected void connect() throws IOException {
		if (_os == null) _os = _connection.getOutputStream();
	}

	protected void write(char c) throws IOException {
		connect();
		_os.write(c);
	}

	protected void write(String s) throws IOException {
		connect();
		_os.write(s.getBytes());
	}

	protected void newline() throws IOException {
		connect();
		write("\r\n");
	}

	protected void writeln(String s) throws IOException {
		connect();
		write(s);
		newline();
	}

	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	private void boundary() throws IOException {
		write("--");
		write(boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request on a freshly opened URLConnection
	 *
	 * @param inConnection an already open URL connection
	 * @throws IOException
	 */
	public FormPoster(URLConnection inConnection) throws IOException
	{
		_connection = inConnection;
		_connection.setDoOutput(true);
		_connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL
	 *
	 * @param url the URL to send request to
	 * @throws IOException
	 */
	public FormPoster(URL url) throws IOException {
		this(url.openConnection());
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL string
	 *
	 * @param urlString the string representation of the URL to send request to
	 * @throws IOException
	 */
	public FormPoster(String urlString) throws IOException {
		this(new URL(urlString));
	}


	private void writeName(String name) throws IOException
	{
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}

	/**
	 * adds a string parameter to the request
	 * @param name parameter name
	 * @param value parameter value
	 * @throws IOException
	 */
	public void setParameter(String name, String value) throws IOException
	{
		boundary();
		writeName(name);
		newline(); newline();
		writeln(value);
	}

	private static void pipe(InputStream in, OutputStream out) throws IOException
	{
		byte[] buf = new byte[500000];
		int nread;
		int total = 0;
		synchronized (in) {
			while((nread = in.read(buf, 0, buf.length)) >= 0) {
				out.write(buf, 0, nread);
				total += nread;
			}
		}
		out.flush();
		buf = null;
	}

	/**
	 * adds a file parameter to the request
	 * @param name parameter name
	 * @param filename the name of the file
	 * @param is input stream to read the contents of the file from
	 * @throws IOException
	 */
	public void setParameter(String name, String filename, InputStream is) throws IOException
	{
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		String type = URLConnection.guessContentTypeFromName(filename);
		if (type == null) type = "application/octet-stream";
		writeln(type);
		newline();
		pipe(is, _os);
		newline();
	}

	/**
	 * adds a file parameter to the request
	 * @param name parameter name
	 * @param file the file to upload
	 * @throws IOException
	 */
	public void setParameter(String name, File file) throws IOException {
		setParameter(name, file.getPath(), new FileInputStream(file));
	}

	/**
	 * adds a parameter to the request; if the parameter is a File, the file is uploaded,
	 * otherwise the string value of the parameter is passed in the request
	 * @param name parameter name
	 * @param object parameter value, a File or anything else that can be stringified
	 * @throws IOException
	 */
	public void setParameter(String name, Object object) throws IOException
	{
		if (object instanceof File) {
			setParameter(name, (File) object);
		} else {
			setParameter(name, object.toString());
		}
	}

	/**
	 * posts the requests to the server
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post() throws IOException {
		boundary();
		writeln("--");
		_os.close();
		return _connection.getInputStream();
	}

	/**
	 * post the POST request to the server, with the specified parameter
	 * @param name parameter name
	 * @param value parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name, Object value) throws IOException {
		setParameter(name, value);
		return post();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2) throws IOException {
		setParameter(name1, value1);
		return post(name2, value2);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2,
		String name3, Object value3) throws IOException
	{
		setParameter(name1, value1);
		return post(name2, value2, name3, value3);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @param name4 fourth parameter name
	 * @param value4 fourth parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public InputStream post(String name1, Object value1, String name2, Object value2,
		String name3, Object value3, String name4, Object value4) throws IOException
	{
		setParameter(name1, value1);
		return post(name2, value2, name3, value3, name4, value4);
	}

	/**
	 * post the POST request specified URL, with the specified parameter
	 * @param name parameter name
	 * @param value parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public static InputStream post(URL url, String name1, Object value1) throws IOException {
		return new FormPoster(url).post(name1, value1);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public static InputStream post(URL url, String name1, Object value1,
		String name2, Object value2) throws IOException
	{
		return new FormPoster(url).post(name1, value1, name2, value2);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public static InputStream post(URL url, String name1, Object value1, String name2, Object value2,
		String name3, Object value3) throws IOException
	{
		return new FormPoster(url).post(name1, value1, name2, value2, name3, value3);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * @param name1 first parameter name
	 * @param value1 first parameter value
	 * @param name2 second parameter name
	 * @param value2 second parameter value
	 * @param name3 third parameter name
	 * @param value3 third parameter value
	 * @param name4 fourth parameter name
	 * @param value4 fourth parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setParameter
	 */
	public static InputStream post(URL url, String name1, Object value1, String name2, Object value2,
		String name3, Object value3, String name4, Object value4) throws IOException
	{
		return new FormPoster(url).post(name1, value1, name2, value2, name3, value3, name4, value4);
	}
}
