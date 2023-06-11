package tim.prune.function.estimate.jama;

/**
 * Static helper method, taken from public domain NIST code for JAMA
 */
public abstract class Maths
{
	/**
	 * Work out sqrt(a^2 + b^2)
	 */
	public static double pythag(double a, double b)
	{
		double r;

		if (Math.abs(a) > Math.abs(b))
		{
			r = b/a;
			return Math.abs(a)*Math.sqrt(1+r*r);
		}
		else if (b != 0)
		{
			r = a/b;
			return Math.abs(b)*Math.sqrt(1+r*r);
		}
		// b is zero and a isn't bigger than b
		return 0.0;
	}
}
