package tim.prune.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to load the contents of a file
 * into an array for later retrieval
 */
public class FileCacher
{
	/** File to cache */
	private File _file = null;
	/** Array to hold lines of file */
	private String[] _contentArray = null;


	/**
	 * Constructor
	 * @param inFile File object to cache
	 */
	public FileCacher(File inFile)
	{
		_file = inFile;
		loadFile();
	}


	/**
	 * Load the specified file into memory
	 */
	private void loadFile()
	{
		ArrayList<String> contentList = new ArrayList<String>();
		if (_file != null && _file.exists() && _file.canRead())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new FileReader(_file));
				String currLine = reader.readLine();
				while (currLine != null)
				{
					if (currLine.trim().length() > 0)
						contentList.add(currLine);
					currLine = reader.readLine();
				}
			}
			catch (IOException ioe) {}
			finally
			{
				// close file ignoring errors
				try
				{
					if (reader != null) reader.close();
				}
				catch (Exception e) {}
			}
		}
		// Convert into String array for keeps
		int numLines = contentList.size();
		_contentArray = new String[numLines];
		for (int i=0; i<numLines; i++)
			_contentArray[i] = contentList.get(i);
	}


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
		_file = null;
		_contentArray = null;
	}
}
