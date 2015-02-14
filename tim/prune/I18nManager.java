package tim.prune;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private static final String BUNDLE_NAME = "tim.prune.lang.prune-texts";
	private static final Locale BACKUP_LOCALE = new Locale("en", "GB");

	private static ResourceBundle EnglishTexts = null;
	private static ResourceBundle LocalTexts = null;

	/** External properties file for developer testing */
	private static Properties ExternalPropsFile = null;


	/**
	 * Initialize the library using the (optional) locale
	 * @param inLocale locale to use, or null for default
	 */
	public static void init(Locale inLocale)
	{
		// Load English texts first to use as defaults
		EnglishTexts = ResourceBundle.getBundle(BUNDLE_NAME, BACKUP_LOCALE);

		// Get bundle for selected locale, if any
		try
		{
			if (inLocale != null)
			{
				LocalTexts = ResourceBundle.getBundle(BUNDLE_NAME, inLocale);
			}
			else
			{
				// locale is null so just use the system default
				LocalTexts = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
			}
		}
		catch (MissingResourceException mre) { // ignore error, default to english
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
		// look in extra texts if available
		if (LocalTexts != null)
		{
			try
			{
				String localText = LocalTexts.getString(inKey);
				if (localText != null) return localText;
			}
			catch (MissingResourceException mre) {}
		}
		// look in english texts
		if (EnglishTexts != null)
		{
			try
			{
				String engText = EnglishTexts.getString(inKey);
				if (engText != null) return engText;
			}
			catch (MissingResourceException mre) {}
		}
		// return the key itself
		return inKey;
	}
}
