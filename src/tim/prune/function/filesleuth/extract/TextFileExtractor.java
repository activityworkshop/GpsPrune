package tim.prune.function.filesleuth.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TimeZone;

import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackContents;


/** Responsible for extracting text from text/csv files */
public class TextFileExtractor implements ContentExtractor
{
	private final File _file;

	public TextFileExtractor(File inFile) {
		_file = inFile;
	}

	@Override
	public TrackContents getContents(TimeZone inTimezone)
	{
		TrackContents contents = new TrackContents(inTimezone);
		try (BufferedReader reader = new BufferedReader(new FileReader(_file)))
		{
			reader.lines().forEach((s) -> processLine(s, contents));
		} catch (IOException ignored) {}
		return contents;
	}

	/** Split the given line into bits and add fields to the contents object */
	private void processLine(String inLine, TrackContents inContents)
	{
		if (inLine == null) {
			return;
		}
		String line = inLine.trim();
		if (line.equals("")) {
			return;
		}
		for (String field : line.split("[,;\\t]+")) {
			if (looksLikeWord(field)) {
				inContents.addString(field);
			}
		}
	}

	/** @return true if the text contains at least two letters (not just numbers) */
	private static boolean looksLikeWord(String inText)
	{
		boolean foundLetter = false;
		for (int i=0; i<inText.length(); i++)
		{
			if (Character.isAlphabetic(inText.charAt(i)))
			{
				if (foundLetter) {
					return true;
				}
				foundLetter = true;
			}
		}
		return false;
	}

	@Override
	public boolean matchesFilter(LocationFilter inFilter)
	{
		// Not possible because we don't get coordinates from text files, only xml
		return false;
	}
}
