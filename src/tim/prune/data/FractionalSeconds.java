package tim.prune.data;

public class FractionalSeconds
{
	private final long _numerator;
	private final byte _divisorDigits;


	public FractionalSeconds(long inDegrees, long inDegFrac, int inDigits)
	{
		// Limit digits to maximum 13 to stop longs from overflowing
		int digits = inDigits;
		long degFrac = inDegFrac;
		while (digits > 13)
		{
			digits--;
			degFrac = degFrac / 10L;
		}
		long degDenom = getMultiplier(digits);
		_numerator = (inDegrees * degDenom + degFrac) * 60L * 60L;
		_divisorDigits = (byte) digits;
	}

	public FractionalSeconds(long inDegrees, long inMinutes, long inMinFrac, int inDigits)
	{
		long minDenom = getMultiplier(inDigits);
		_numerator = ((inDegrees * 60L + inMinutes) * minDenom + inMinFrac)  * 60L;
		_divisorDigits = (byte) inDigits;
	}

	public FractionalSeconds(long inDegrees, long inMinutes, long inSeconds, long inSecFrac, int inDigits)
	{
		long secDenom = getMultiplier(inDigits);
		_numerator = ((inDegrees * 60L + inMinutes) * 60L + inSeconds) * secDenom + inSecFrac;
		_divisorDigits = (byte) inDigits;
	}

	public static FractionalSeconds fromDouble(double inDegrees, int inNumDigits)
	{
		double value = Math.abs(inDegrees) * getMultiplier(inNumDigits);
		long numerator = (long) (value * 60.0 * 60.0);
		return new FractionalSeconds(numerator, (byte) inNumDigits);
	}

	private FractionalSeconds(long inNumerator, byte inNumDigits)
	{
		_numerator = inNumerator;
		_divisorDigits = inNumDigits;
	}

	public int getTotalSeconds()
	{
		long seconds = _numerator / getMultiplier(_divisorDigits);
		return (int) seconds;
	}

	public int getWholeDegrees() {
		return getTotalSeconds() / 60 / 60;
	}

	public int getWholeMinutes() {
		return (getTotalSeconds() / 60) % 60;
	}

	public int getWholeSeconds() {
		return getTotalSeconds() % 60;
	}

	public String getFractionDegrees() {
		return getFractionString(_numerator / 60L / 60L, _divisorDigits);
	}

	public String getFractionDegrees(int inNumDigits)
	{
		if (inNumDigits != _divisorDigits) {
			return roundToSeconds(inNumDigits).divideBy(3600L).getFractionSeconds();
		}
		return getFractionDegrees();
	}

	public String getFractionMinutes() {
		return getFractionString(_numerator / 60L, _divisorDigits);
	}

	public String getFractionMinutes(int inNumDigits)
	{
		if (inNumDigits != _divisorDigits) {
			return roundToSeconds(inNumDigits).divideBy(60L).getFractionSeconds();
		}
		return getFractionMinutes();
	}

	public String getFractionSeconds() {
		return getFractionString(_numerator, _divisorDigits);
	}

	public String getFractionSeconds(int inNumDigits) {
		return roundToSeconds(inNumDigits).getFractionSeconds();
	}

	public double getDouble() {
		return _numerator * 1.0 / getMultiplier(_divisorDigits) / 3600.0;
	}

	public FractionalSeconds roundToDegrees(int inNumDigits) {
		return roundTo(60L * 60L, inNumDigits);
	}

	public FractionalSeconds roundToMinutes(int inNumDigits) {
		return roundTo(60L, inNumDigits);
	}

	public FractionalSeconds roundToSeconds(int inNumDigits) {
		return roundTo(1L, inNumDigits);
	}

	private FractionalSeconds roundTo(long inMultFactor, int inNumDigits)
	{
		if (inNumDigits > _divisorDigits)
		{
			long num = _numerator * getMultiplier(inNumDigits - _divisorDigits);
			return new FractionalSeconds(num, (byte) inNumDigits);
		}
		else if (inNumDigits < _divisorDigits)
		{
			long factor = getMultiplier(_divisorDigits - inNumDigits);
			long num = _numerator + factor / 2L * inMultFactor;
			return new FractionalSeconds(num / factor, (byte) inNumDigits);
		}
		return this;
	}

	private FractionalSeconds divideBy(long divisor) {
		return new FractionalSeconds(_numerator / divisor, _divisorDigits);
	}

	private static String getFractionString(long inNumerator, byte inDivisorDigits)
	{
		if (inDivisorDigits == 0) {
			return "";
		}
		long factor = getMultiplier(inDivisorDigits);
		long remainder = inNumerator % factor;
		return String.format("%0" + inDivisorDigits + "d", remainder);
	}

	/** Return the multiplier given the number of digits */
	private static long getMultiplier(int inNumDigits)
	{
		int numDigits = inNumDigits;
		long value = 1L;
		while (numDigits > 0) {
			numDigits--;
			value = value * 10L;
		}
		return value;
	}

	public boolean isWithinOneEightyDegrees()
	{
		final long oneEighty = 180L * 60L * 60L * getMultiplier(_divisorDigits);
		return _numerator <= oneEighty;
	}

	public FractionalSeconds wrapToThreeSixtyDegrees() {
		return new FractionalSeconds(_numerator % getThreeSixtyDegrees(), _divisorDigits);
	}

	private long getThreeSixtyDegrees() {
		return 360L * 60L * 60L * getMultiplier(_divisorDigits);
	}

	public FractionalSeconds invert() {
		return new FractionalSeconds(getThreeSixtyDegrees() - _numerator, _divisorDigits);
	}
}
