package tim.prune.data;

/**
 * Validation of symbol scale factor to restrict to certain range
 */
public abstract class SymbolScaleFactor
{
	private static final double MIN_FACTOR = 0.1;
	private static final double MAX_FACTOR = 2.0;

	/**
	 * @param inFactor value entered by user
	 * @return validated value within range
	 */
	public static double validateFactor(double inFactor) {
		return Math.min(MAX_FACTOR, Math.max(MIN_FACTOR, inFactor));
	}
}
