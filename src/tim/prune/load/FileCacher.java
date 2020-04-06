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
public class FileCacher extends ContentCacher
{
	/**
	 * Constructor
	 * @param inFile File object to cache
	 */
	public FileCacher(File inFile)
	{
		loadFile(inFile);
	}


	/**
	 * Load the specified file into memory
	 */
	private void loadFile(File inFile)
	{
		ArrayList<String> contentList = new ArrayList<String>();
		if (inFile != null && inFile.exists() && inFile.canRead())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new FileReader(inFile));
				String currLine = reader.readLine();
				if (currLine != null && currLine.startsWith("<?xml")) {
					return; // it's an xml file, it shouldn't use this cacher
				}
				while (currLine != null)
				{
					if (currLine.indexOf('\0') >= 0)
					{
						reader.close();
						return; // it's a binary file, shouldn't use this cacher
					}
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
		setContents(contentList);
	}
}
