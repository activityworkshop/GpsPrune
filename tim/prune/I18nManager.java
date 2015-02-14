package tim.prune;

import java.util.Locale;
import java.util.MissingResourceException;
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
	private static ResourceBundle ExtraTexts = null;


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
			ExtraTexts = ResourceBundle.getBundle(BUNDLE_NAME, inLocale);
		}
		else
		{
			// locale is null so just use the system default
			ExtraTexts = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
		}
	}


	/**
	 * Lookup the given key and return the associated text
	 * @param inKey key to lookup
	 * @return associated text, or the key if not found
	 */
	public static String getText(String inKey)
	{
		String value = null;
		// look in extra texts if available
		if (ExtraTexts != null)
		{
			try
			{
				value = ExtraTexts.getString(inKey);
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
