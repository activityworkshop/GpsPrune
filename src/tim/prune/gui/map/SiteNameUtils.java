package tim.prune.gui.map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper functions for manipulating tile site names
 */
public abstract class SiteNameUtils
{
	/** Regular expression for catching server wildcards */
	private static final Pattern WILD_PATTERN = Pattern.compile("^(.*)\\[(.*)\\](.*)$");


	/**
	 * If the base url contains something like [1234], then pick a server
	 * @param inBaseUrl base url
	 * @return modified base url
	 */
	public static String pickServerUrl(String inBaseUrl)
	{
		if (inBaseUrl == null || inBaseUrl.indexOf('[') < 0) {
			return inBaseUrl;
		}
		// Check for [.*] (once only)
		// Only need to support one, make things a bit easier
		final Matcher matcher = WILD_PATTERN.matcher(inBaseUrl);
		// if not, return base url unchanged
		if (!matcher.matches()) {
			return inBaseUrl;
		}
		// if so, pick one at random and replace in the String
		final String match = matcher.group(2);
		final int numMatches = match.length();
		String server = null;
		if (numMatches > 0)
		{
			int matchNum = (int) Math.floor(Math.random() * numMatches);
			server = "" + match.charAt(matchNum);
		}
		return matcher.group(1) + (server==null?"":server) + matcher.group(3);
	}


	/**
	 * Fix the site name by stripping off protocol and www.
	 * This is used to create the file path for disk caching
	 * @param inUrl url to strip
	 * @return stripped url
	 */
	public static String convertUrlToDirectory(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String url = inUrl.toLowerCase();
		int idx = url.indexOf("://");
		if (idx >= 0) {url = url.substring(idx + 3);}
		if (url.startsWith("www.")) {url = url.substring(4);}
		// Strip out any "[.*]" as well
		if (url.indexOf('[') >= 0)
		{
			Matcher matcher = WILD_PATTERN.matcher(url);
			if (matcher.matches())
			{
				url = matcher.group(1) + matcher.group(3);
				if (url.length() > 1 && url.charAt(0) == '.') {
					url = url.substring(1);
				}
			}
		}
		if (url.indexOf('{') > 0) {
			url = removePlaceholders(url);
		}
		return url;
	}


	private static String removePlaceholders(String url) {
		final int lastSlashPos = url.lastIndexOf('/');
		if (lastSlashPos >= 0) {
			url = url.substring(0, lastSlashPos+1);
		}
		return url.replace("{x}", "x").replace("{y}", "y").replace("{z}", "z");
	}
}
