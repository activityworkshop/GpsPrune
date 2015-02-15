package tim.prune.save.xml;

/**
 * Collection of utility functions for handling XML
 */
public abstract class XmlUtils
{
	/** Start of Cdata sequence */
	private static final String CDATA_START = "<![CDATA[";
	/** End of Cdata sequence */
	private static final String CDATA_END = "]]>";

	/**
	 * Fix the CDATA blocks in the given String to give valid xml
	 * @param inString String to modify
	 * @return fixed String
	 */
	public static String fixCdata(String inString)
	{
		if (inString == null) return "";
		if (inString.indexOf('<') < 0 && inString.indexOf('>') < 0) {
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
		if (result.indexOf('<') < 0 && result.indexOf('>') < 0) {
			return result;
		}
		return CDATA_START + result + CDATA_END;
	}
}
