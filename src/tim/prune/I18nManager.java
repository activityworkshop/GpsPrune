package tim.prune;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Manager for all internationalization
 * Responsible for loading property files
 * and delivering language-specific texts
 */
public abstract class I18nManager
{
	/** Properties object into which all the texts are copied */
	private static Properties LocalTexts = null;

	/** External properties file for developer testing */
	private static Properties ExternalPropsFile = null;


	/**
	 * Initialize the library using the (optional) locale
	 * @param inLocale locale to use, or null for default
	 */
	public static void init(Locale inLocale)
	{
		final String BUNDLE_NAME = "tim.prune.lang.prune-texts";
		final Locale BACKUP_LOCALE = new Locale("en", "GB");

		LocalTexts = new Properties();
		// Load English texts first to use as defaults
		loadFromBundle(ResourceBundle.getBundle(BUNDLE_NAME, BACKUP_LOCALE));

		// Get bundle for selected locale, if any
		try
		{
			if (inLocale != null)
			{
				loadFromBundle(ResourceBundle.getBundle(BUNDLE_NAME, inLocale));
			}
			else
			{
				// locale is null so just use the system default
				loadFromBundle(ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()));
			}
		}
		catch (MissingResourceException mre) { // ignore error, default to english
		}
	}

	/**
	 * Copy all the translations from the given bundle and store in the Properties object
	 * overwriting the existing translations if necessary
	 * @param inBundle bundle object loaded from file
	 */
	private static void loadFromBundle(ResourceBundle inBundle)
	{
		Enumeration<String> e = inBundle.getKeys();
		while (e.hasMoreElements())
		{
			String key = e.nextElement();
			LocalTexts.setProperty(key, inBundle.getString(key));
		}
	}

	/**
	 * Add a language file
	 * @param inFilename filename of file
	 * @throws FileNotFoundException if load failed
	 */
	public static void addLanguageFile(String inFilename) throws FileNotFoundException
	{
		FileInputStream fis = null;
		boolean fileLoaded = false;
		try
		{
			File file = new File(inFilename);
			ExternalPropsFile = new Properties();
			fis = new FileInputStream(file);
			ExternalPropsFile.load(fis);
			fileLoaded = true; // everything worked
		}
		catch (IOException ioe) {}
		finally { try { fis.close();
			} catch (Exception e) {}
		}
		// complain if file wasn't loaded, by throwing a filenotfound exception
		if (!fileLoaded) throw new FileNotFoundException();
	}


	/**
	 * Lookup the given key and return the associated text
	 * @param inKey key to lookup
	 * @return associated text, or the key if not found
	 */
	public static String getText(String inKey)
	{
		// look in external props file if available
		if (ExternalPropsFile != null)
		{
			String extText = ExternalPropsFile.getProperty(inKey);
			if (extText != null) return extText;
		}
		// look in texts if available
		if (LocalTexts != null)
		{
			try
			{
				String localText = LocalTexts.getProperty(inKey);
				if (localText != null) return localText;
			}
			catch (MissingResourceException mre) {}
		}
		// return the key itself
		return inKey;
	}

	/**
	 * Lookup the given key and return the associated text, formatting with the number
	 * @param inKey key to lookup (text should contain a %d)
	 * @param inNumber number to substitute into the %d
	 * @return associated text, or the key if not found
	 */
	public static String getTextWithNumber(String inKey, int inNumber)
	{
		String localText = getText(inKey);
		try
		{
			localText = String.format(localText, inNumber);
		}
		catch (Exception e)
		{} // printf formatting didn't work, maybe the placeholders are wrong?
		return localText;
	}
}
