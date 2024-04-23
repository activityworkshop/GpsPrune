package tim.prune.lang;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;

import org.junit.jupiter.api.Test;


/**
 * Tests for checking the consistency of the translations.
 * This means ensuring that translations don't have elements which aren't in en,
 * and that all the %d and %s placeholders are consistent for all languages.
 */
public class TranslationsTest
{
	@Test
	public void testConsistency()
	{
		File enFile = new File("src/tim/prune/lang/prune-texts_en.properties");
		Properties enTexts = loadLanguage(enFile);
		for (File file : new File("src/tim/prune/lang/").listFiles())
		{
			if (file.getName().contains("texts") && !file.getName().equals(enFile.getName())) {
				Properties texts = loadLanguage(file);
				for (Object key : texts.keySet()) {
					String translation = texts.getProperty(key.toString());
					String enText = enTexts.getProperty(key.toString());
					assertNotNull(enText, "en missing for key '" + key + "' for file '" + file.getName() + "'");

					// Make sure that placeholders %s and %d are consistent everywhere
					int enCountPercentS = countOccurrences(enText, "%s");
					int translationCountPercentS = countOccurrences(translation, "%s");
					assertEquals(enCountPercentS, translationCountPercentS, "%s for key '" + key + "', " + file.getName());

					boolean enHasPercentD = enText.contains("%d");
					boolean translationHasPercentD = translation.contains("%d");
					assertEquals(enHasPercentD, translationHasPercentD, "%d for key '" + key + "', " + file.getName());
				}
			}
		}
	}

	/** Count the number of times the given placeholder occurs within the translation */
	private int countOccurrences(String translation, String placeholder)
	{
		int count = 0;
		int pos = translation.indexOf(placeholder);
		while (pos >= 0) {
			count++;
			pos = translation.indexOf(placeholder, pos + 1);
		}
		return count;
	}

	private Properties loadLanguage(File inFile)
	{
		Properties texts = new Properties();
		try {
			texts.load(new FileInputStream(inFile));
		} catch (IOException ignored) {}
		return texts;
	}

	/** Make sure the texts don't contain duplicate keys */
	@Test
	public void testDuplicates() throws IOException
	{
		for (File file : new File("src/tim/prune/lang/").listFiles())
		{
			if (file.getName().contains("texts")) {
				try (InputStream in = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(in)))
				{
					HashSet<String> keys = new HashSet<>();
					String strLine;
					while ((strLine = br.readLine()) != null)
					{
						if (!strLine.startsWith("#") && !strLine.isEmpty())
						{
							int eqPos = strLine.indexOf('=');
							assertTrue(eqPos > 0);
							String key = strLine.substring(0, eqPos);
							assertFalse(keys.contains(key), "File '" + file.getName() + "' has duplicate key '" + key + "'");
							keys.add(key);
						}
					}
				}
			}
		}
	}
}
