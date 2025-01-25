package tim.prune.data;

public abstract class GeocacheCode
{
	/** Supported providers of cache codes */
	private enum Provider
	{
		GEOCACHING_COM("GC", "https://coord.info/"),
		OPENCACHING_DE("OC", "https://www.opencaching.de/");

		private final String _prefix;
		private final String _url;

		Provider(String inPrefix, String inUrl) {
			_prefix = inPrefix;
			_url = inUrl;
		}

		static Provider getProvider(String inCode)
		{
			if (inCode == null || inCode.length() < 2) {
				return null;
			}
			for (Provider provider : values())
			{
				// Assume that all prefixes are the first two characters
				if (provider._prefix.charAt(0) == inCode.charAt(0)
						&& provider._prefix.charAt(1) == inCode.charAt(1)) {
					return provider;
				}
			}
			return null;
		}
	}

	/** @return true if the given code is recognised and valid */
	public static boolean isValidCode(String inCode)
	{
		if (inCode == null || !isLengthValid(inCode.length())) {
			return false;
		}
		Provider provider = Provider.getProvider(inCode);
		return provider != null && verifyCharactersAreValid(inCode);
	}

	/** @return true if the length of the code is acceptable */
	private static boolean isLengthValid(int inLength) {
		return inLength > 4 && inLength <= 8;
	}

	/**
	 * @return true if the code only contains valid letters and numbers,
	 * but this doesn't mean that it starts with a recognised prefix
	 */
	private static boolean verifyCharactersAreValid(String inCode)
	{
		// Note, only upper case ASCII letters and numbers are allowed
		final String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		for (int i=0; i<inCode.length(); i++)
		{
			if (allowedChars.indexOf(inCode.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	/** @return a suitable URL for the given code, or null if not available */
	public static String getUrl(String inCode)
	{
		Provider provider = Provider.getProvider(inCode);
		if (provider != null
			&& inCode != null
			&& isLengthValid(inCode.length())
			&& verifyCharactersAreValid(inCode))
		{
			return provider._url + inCode;
		}
		return null;
	}
}
