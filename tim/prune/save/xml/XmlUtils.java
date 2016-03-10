package tim.prune.save.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Collection of utility functions for handling XML
 */
public abstract class XmlUtils
{
	/** Start of Cdata sequence */
	private static final String CDATA_START = "<![CDATA[";
	/** End of Cdata sequence */
	private static final String CDATA_END = "]]>";
	/** Cached copy of system encoding string */
	private static String _systemEncoding = null;

	/**
	 * Fix the CDATA blocks in the given String to give valid xml
	 * @param inString String to modify
	 * @return fixed String
	 */
	public static String fixCdata(String inString)
	{
		if (inString == null || inString.isEmpty()) return "";
		if (!hasIllegalCharacter(inString)) {
			return inString;
		}
		String result = inString;
		// Remove cdata block at start if present
		if (result.startsWith(CDATA_START)) {
			result = result.substring(CDATA_START.length());
		}
		// Remove all instances of end block
		result = result.replaceAll(CDATA_END, "");
		// Now check whether cdata block is required
		if (!hasIllegalCharacter(result)) {
			return result;
		}
		return CDATA_START + result + CDATA_END;
	}

	/**
	 * Checks the input string for the three illegal characters,
	 * but only looping through the string once instead of three times
	 * @param inValue string to check
	 * @return true if at least one of the illegal characters is found
	 */
	public static boolean hasIllegalCharacter(String inValue)
	{
		if (inValue == null) return false;
		final int numChars = inValue.length();
		for (int i=0; i<numChars; i++)
		{
			final char c = inValue.charAt(i);
			if (c == '<' || c == '>' || c == '&')
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if system uses UTF-8 by default
	 */
	public static boolean isSystemUtf8()
	{
		String systemEncoding = getSystemEncoding();
		return (systemEncoding != null && systemEncoding.toUpperCase().equals("UTF-8"));
	}

	/**
	 * @return name of the system's character encoding
	 */
	public static String getSystemEncoding()
	{
		if (_systemEncoding == null) {
			_systemEncoding = determineSystemEncoding();
		}
		return _systemEncoding;
	}

	/**
	 * Use a temporary file to obtain the name of the default system encoding
	 * @return name of default system encoding, or null if write failed
	 */
	private static String determineSystemEncoding()
	{
		File tempFile = null;
		String encoding = null;
		try
		{
			tempFile = File.createTempFile("gpsprune", null);
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile));
			encoding = getEncoding(writer);
			writer.close();
		}
		catch (IOException e) {} // value stays null
		// Delete temp file
		if (tempFile != null && tempFile.exists()) {
			if (!tempFile.delete()) {
				System.err.println("Cannot delete temp file: " + tempFile.getAbsolutePath());
			}
		}
		// If writing failed (eg permissions) then just ask system for default
		if (encoding == null) encoding = Charset.defaultCharset().name();
		return encoding;
	}


	/**
	 * Get the default system encoding using a writer
	 * @param inWriter writer object
	 * @return string defining encoding
	 */
	public static String getEncoding(OutputStreamWriter inWriter)
	{
		String encoding = inWriter.getEncoding();
		try {
			encoding =  Charset.forName(encoding).name();
		}
		catch (Exception e) {} // ignore failure to find encoding
		return encoding;
	}
}
