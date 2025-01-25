package tim.prune.function.distance;

import tim.prune.I18nManager;

/** Class to convert a bearing into a language-dependent description like "N" or "SW" */
public class BearingDescriber
{
	/** Concatenated cardinals like "NESW" */
	private final String _singleCardinals;
	/** Concatenated two-letter cardinals like "NESESWNW" */
	private final String _doubleCardinals;

	/** Constructor */
	BearingDescriber()
	{
		final String cardinalNorth = I18nManager.getText("cardinal.n"),
				cardinalEast = I18nManager.getText("cardinal.e"),
				cardinalSouth = I18nManager.getText("cardinal.s"),
				cardinalWest = I18nManager.getText("cardinal.w");
		_singleCardinals = cardinalNorth + cardinalEast + cardinalSouth + cardinalWest;
		_doubleCardinals = cardinalNorth + cardinalEast
				+ cardinalSouth + cardinalEast
				+ cardinalSouth + cardinalWest
				+ cardinalNorth + cardinalWest;
	}


	/**
	 * @param bearing angle in degrees clockwise from north
	 * @return description using cardinals, like N, NE, E
	 */
	String describeBearing(double bearing)
	{
		final int index = (int) Math.floor((bearing + 22.5 + 360.0) / 45) % 8;
		if ((index % 2) == 0) {
			return _singleCardinals.substring(index / 2, index / 2 + 1);
		}
		return _doubleCardinals.substring(index - 1, index + 1);
	}
}
