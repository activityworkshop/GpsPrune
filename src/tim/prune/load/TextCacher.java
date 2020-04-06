package tim.prune.load;

import java.util.ArrayList;

/**
 * Class to split a pasted text
 * into an array for later retrieval
 */
public class TextCacher extends ContentCacher
{
	/**
	 * Constructor
	 * @param inText text to cache
	 */
	public TextCacher(String inText)
	{
		splitText(inText);
	}


	/**
	 * Load and split the specified text
	 */
	private void splitText(String inText)
	{
		ArrayList<String> contentList = new ArrayList<String>();
		if (inText != null)
		{
			for (String currLine : inText.split("\n"))
			{
				if (currLine != null)
				{
					currLine = currLine.trim();
					if (currLine.length() > 0) {
						contentList.add(currLine);
					}
				}
			}
		}
		setContents(contentList);
	}
}
