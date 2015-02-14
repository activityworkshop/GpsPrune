package tim.prune.jpeg.drew;

/**
 * Immutable class for holding a rational number without loss of precision.
 * Based on Drew Noakes' Metadata extractor at http://drewnoakes.com
 */
public class Rational
{
	/** Holds the numerator */
	private final int _numerator;

	/** Holds the denominator */
	private final int _denominator;

	/**
	 * Constructor
	 * @param inNumerator numerator of fraction (upper number)
	 * @param inDenominator denominator of fraction (lower number)
	 */
	public Rational(int inNumerator, int inDenominator)
	{
		// Could throw exception if denominator is zero
		_numerator = inNumerator;
		_denominator = inDenominator;
	}


	/**
	 * @return the value of the specified number as a <code>double</code>.
	 * This may involve rounding.
	 */
	public double doubleValue()
	{
		if (_denominator == 0) return 0.0;
		return (double)_numerator / (double)_denominator;
	}

	/**
	 * @return the value of the specified number as an <code>int</code>.
	 * This may involve rounding or truncation.
	 */
	public final int intValue()
	{
		if (_denominator == 0) return 0;
		return _numerator / _denominator;
	}

	/**
	 * @return the denominator.
	 */
	public final int getDenominator()
	{
		return _denominator;
	}

	/**
	 * @return the numerator.
	 */
	public final int getNumerator()
	{
		return _numerator;
	}

	/**
	 * Checks if this rational number is an Integer, either positive or negative
	 * @return true if an integer
	 */
	public boolean isInteger()
	{
		// number is integer if the denominator is 1, or if the remainder is zero
		return (_denominator == 1
			|| (_denominator != 0 && (_numerator % _denominator == 0)));
	}


	/**
	 * @return a string representation of the object of form <code>numerator/denominator</code>.
	 */
	public String toString()
	{
		return "" + _numerator + "/" + _denominator;
	}


	/**
	 * Compares two <code>Rational</code> instances, returning true if they are equal
	 * @param inOther the Rational to compare this instance to.
	 * @return true if instances are equal, otherwise false.
	 */
	public boolean equals(Rational inOther)
	{
		// Could also attempt to simplify fractions to lowest common denominator before compare
		return _numerator == inOther._numerator && _denominator == inOther._denominator;
	}
}