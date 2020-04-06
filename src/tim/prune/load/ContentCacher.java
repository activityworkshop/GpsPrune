package tim.prune.load;

import java.util.ArrayList;

/**
 * General point data cacher
 */
public abstract class ContentCacher
{
	/** Array to hold lines of file */
	private String[] _contentArray = null;


	/**
	 * @return Contents of the file as array of non-blank Strings
	 */
	public String[] getContents()
	{
		return _contentArray;
	}


	/**
	 * Get the top section of the file for preview
	 * @param inNumRows number of lines to extract
	 * @param inMaxWidth max length of Strings (longer ones will be chopped)
	 * @return String array containing non-blank lines from the file
	 */
	public String[] getSnippet(int inNumRows, int inMaxWidth)
	{
		final int MIN_SNIPPET_SIZE = 3;
		// Check size is within sensible limits
		int numToCopy = inNumRows;
		if (numToCopy > getNumLines()) numToCopy = getNumLines();
		int size = numToCopy;
		if (size < MIN_SNIPPET_SIZE) size = MIN_SNIPPET_SIZE;
		String[] result = new String[size];
		// Copy Strings across
		System.arraycopy(_contentArray, 0, result, 0, numToCopy);
		// Chop Strings to max width if necessary
		if (inMaxWidth > 10)
		{
			for (int i=0; i<size; i++)
			{
				if (result[i] == null)
					result[i] = "";
				else
				{
					if (result[i].length() > inMaxWidth)
						result[i] = result[i].trim();
					if (result[i].length() > inMaxWidth)
						result[i] = result[i].substring(0, inMaxWidth);
				}
			}
		}
		return result;
	}

	/**
	 * @return the number of non-blank lines in the file
	 */
	public int getNumLines()
	{
		return _contentArray.length;
	}


	/**
	 * Clear the memory
	 */
	public void clear()
	{
		_contentArray = null;
	}

	/**
	 * Populate the string array
	 * @param inList list of lines
	 */
	protected void setContents(ArrayList<String> inList)
	{
		// Convert into String array for keeps
		int numLines = inList.size();
		_contentArray = new String[numLines];
		for (int i=0; i<numLines; i++)
		{
			_contentArray[i] = inList.get(i);
		}
	}
}
