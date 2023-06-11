package tim.prune.function;

import tim.prune.I18nManager;

/**
 * Class to build descriptions using singular and plural versions of a token
 */
public class Describer
{
	private final String _singularToken;
	private final String _pluralToken;

	public Describer(String inSingularToken, String inPluralToken)
	{
		_singularToken = inSingularToken;
		_pluralToken = inPluralToken;
	}

	public String getDescriptionWithNameOrCount(String inName, int inCount)
	{
		if (inCount == 1) {
			return I18nManager.getText(_singularToken, inName == null ? "" : inName);
		}
		else if (inCount > 1) {
			return I18nManager.getTextWithNumber(_pluralToken, inCount);
		}
		throw new IllegalArgumentException("Count should not be <= 0");
	}

	public String getDescriptionWithCount(int inCount)
	{
		if (inCount == 1) {
			return I18nManager.getText(_singularToken);
		}
		else if (inCount > 1) {
			return I18nManager.getTextWithNumber(_pluralToken, inCount);
		}
		throw new IllegalArgumentException("Count should not be <= 0");
	}

	public String getDescriptionWithNameOrNot(String inName)
	{
		// We use singular and plural here, but it means without name and with
		if (inName == null || inName.isEmpty()) {
			return I18nManager.getText(_singularToken);
		}
		return I18nManager.getText(_pluralToken, inName);
	}
}
