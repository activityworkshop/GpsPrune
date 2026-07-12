package tim.prune.function.compress;

/**
 * Basic record to hold x and y coordinates
 * for a point or a vector
 */
public record XYpoint(double x, double y)
{
	/**
	 * @param inOther other vector
	 * @return scalar dot product
	 */
	public double dot(XYpoint inOther) {
		return x * inOther.x + y * inOther.y;
	}

	/** @return length of vector */
	public double len() {
		return Math.sqrt(len2());
	}

	/** @return squared length of vector */
	public double len2() {
		return x * x + y * y;
	}

	/**
	 * @param inOther other point object
	 * @return vector from this one to the other one
	 */
	public XYpoint vectorTo(XYpoint inOther) {
		return new XYpoint(inOther.x - x, inOther.y - y);
	}
}
