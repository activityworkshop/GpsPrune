package tim.prune;

import java.io.File;
import java.io.FileInputStream;
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


	/**
	 * Add a language file
	 * @param inFilename filename of file
	 */
	public static void addLanguageFile(String inFilename)
	{
		try
		{
			File file = new File(inFilename);
			ExternalPropsFile = new Properties();
			ExternalPropsFile.load(new FileInputStream(file));
		}
		catch (IOException ioe) {}
	}


	/**
	 * Lookup the given key and return the associated text
	 * @param inKey key to lookup
	 * @return associated text, or the key if not found
	 */
	public static String getText(String inKey)
	{
		String value = null;
		// look in external props file if available
		if (ExternalPropsFile != null)
		{
			value = ExternalPropsFile.getProperty(inKey);
			if (value != null && !value.equals(""))
				return value;
		}
		// look in extra texts if available
		if (LocalTexts != null)
		{
			try
			{
				value = LocalTexts.getString(inKey);
				if (value != null && !value.equals(""))
					return value;
			}
			catch (MissingResourceException mre) {}
		}
		// look in english texts
		if (EnglishTexts != null)
		{
			try
			{
				value = EnglishTexts.getString(inKey);
				if (value != null && !value.equals(""))
					return value;
			}
			catch (MissingResourceException mre) {}
		}
		// return the key itself
		return inKey;
	}
}
