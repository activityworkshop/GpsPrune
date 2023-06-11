package tim.prune.lang;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
					boolean enHasPercentS = enText.contains("%s");
					boolean translationHasPercentS = translation.contains("%s");
					assertEquals(enHasPercentS, translationHasPercentS, "%s for key '" + key + "', " + file.getName());

					boolean enHasPercentD = enText.contains("%d");
					boolean translationHasPercentD = translation.contains("%d");
					assertEquals(enHasPercentD, translationHasPercentD, "%d for key '" + key + "', " + file.getName());
				}
			}
		}
	}

	private Properties loadLanguage(File inFile)
	{
		Properties texts = new Properties();
		try {
			texts.load(new FileInputStream(inFile));
		} catch (IOException ignored) {}
		return texts;
	}
}
