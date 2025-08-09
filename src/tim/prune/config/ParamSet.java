package tim.prune.config;

import java.util.ArrayList;

/**
 * Split a string into tokens using semicolons as delimiters.
 * Unlike StringTokenizer and Scanner this also allows empty tokens.
 */
public class ParamSet
{
	private final ArrayList<String> _params = new ArrayList<>();

	/**
	 * Constructor
	 * @param inSource source string to parse
	 */
	public ParamSet(String inSource) {
		getParams(inSource == null ? "" : inSource);
	}

	/** Extract the parameters into the _params list */
	private void getParams(String inSource)
	{
		StringBuilder builder = new StringBuilder();
		int currPos = 0;
		while (currPos < inSource.length())
		{
			char c = inSource.charAt(currPos);
			currPos++;
			if (c == ';')
			{
				_params.add(builder.toString());
				builder = new StringBuilder();
			}
			else {
				builder.append(c);
			}
		}
		_params.add(builder.toString());
	}

	/** @return the number of parameters read */
	public int getNumParams() {
		return _params.size();
	}

	/** @return the parameter at the given index, or an empty string */
	public String getParam(int inIndex)
	{
		if (inIndex < 0 || inIndex >= _params.size()) {
			return "";
		}
		return _params.get(inIndex);
	}
}
