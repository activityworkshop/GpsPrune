package tim.prune.jpeg.drew;

/**
 * Immutable class for holding a rational number without loss of precision.
 * Based on Drew Noakes' Metadata extractor at https://drewnoakes.com
 */
public class Rational
{
	/** Holds the numerator */
	private final long _numerator;

	/** Holds the denominator */
	private final long _denominator;

	/**
	 * Creates a new (immutable) instance of Rational.
	 */
	public Rational(long numerator, long denominator)
	{
		_numerator = numerator;
		_denominator = denominator;
	}

	/**
	 * @return the value of the specified number as a <code>double</code>.
	 * This may involve rounding.
	 */
	public double doubleValue()
	{
		if (_denominator == 0L) return 0.0;
		return (double)_numerator / (double)_denominator;
	}

	/**
	 * Returns the value of the specified number as an <code>int</code>.
	 */
	public final int intValue()
	{
		return (int) longValue();
	}

	/**
	 * Returns the value of the specified number as a <code>long</code>.
	 * This may involve rounding or truncation.
	 * If the denominator is 0, returns 0 to avoid dividing by 0.
	 */
	public final long longValue()
	{
		if (_denominator == 0L) return 0L;
		return _numerator / _denominator;
	}

	/** Returns the denominator */
	public final long getDenominator()
	{
		return _denominator;
	}

	/** Returns the numerator */
	public final long getNumerator()
	{
		return _numerator;
	}

	/**
	 * @return the value of the specified number as a positive <code>double</code>.
	 * Prevents interpretation of 32 bit numbers as negative, and forces a positive answer.
	 * Needed?
	 */
	public double convertToPositiveValue()
	{
		if (_denominator == 0L) return 0.0;
		double numeratorDbl = _numerator;
		double denomDbl = _denominator;
		if (_numerator >= 0L) {
			// Numerator is positive (but maybe denominator isn't?)
			return numeratorDbl / denomDbl;
		}
		// Add 2^32 to negative doubles to make them positive
		final double correction = Math.pow(2.0, 32);
		numeratorDbl += correction;
		if (_denominator < 0L) {
			denomDbl += correction;
		}
		return numeratorDbl / denomDbl;
	}

	/**
	 * Returns a string representation of the object of form <code>numerator/denominator</code>.
	 */
	@Override
	public String toString()
	{
		return "" + _numerator + "/" + _denominator;
	}

	/**
	 * Compares two {@link Rational} instances, returning true if they are mathematically
	 * equivalent.
	 *
	 * @param obj the {@link Rational} to compare this instance to.
	 * @return true if instances are mathematically equivalent, otherwise false.  Will also
	 *         give false if <code>obj</code> is not an instance of {@link Rational}.
	 */
	@Override
	public boolean equals( Object obj)
	{
		if (obj==null || !(obj instanceof Rational))
			return false;
		Rational that = (Rational) obj;
		return this.doubleValue() == that.doubleValue();
	}
}
