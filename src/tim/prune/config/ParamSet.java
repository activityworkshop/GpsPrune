package tim.prune.config;

/**
 * Split a string into tokens using semicolons as delimiters.
 * Unlike StringTokenizer and Scanner this also allows empty tokens.
 */
public class ParamSet
{
	private final String _source;
	private int _currPos = 0;

	/**
	 * Constructor
	 * @param inSource source string to parse
	 */
	public ParamSet(String inSource) {
		_source = (inSource == null ? "" : inSource);
	}

	/**
	 * @return the next token
	 */
	public String getNext()
	{
		StringBuilder builder = new StringBuilder();
		while (_currPos < _source.length())
		{
			char c = _source.charAt(_currPos);
			_currPos++;
			if (c == ';') {
				break;
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
