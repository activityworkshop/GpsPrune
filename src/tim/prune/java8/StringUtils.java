package tim.prune.java8;

public abstract class StringUtils
{
	/** Equivalent of String.repeat for java8 */
	public static String repeat(String inString, int inNumRepeats)
	{
		StringBuilder result = new StringBuilder();
		for (int i=0; i<inNumRepeats; i++) {
			result.append(inString);
		}
		return result.toString();
	}
}
