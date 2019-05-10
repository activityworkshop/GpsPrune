package tim.prune.function.compress;

/**
 * Basic class to hold x and y coordinates
 * for a point or a vector
 */
public class XYpoint
{
	// x and y coordinates
	public double x = 0.0, y = 0.0;

	/**
	 * Empty constructor
	 */
	public XYpoint() {
		this(0.0, 0.0);
	}

	/**
	 * Constructor
	 * @param inX x value
	 * @param inY y value
	 */
	public XYpoint(double inX, double inY) {
		x = inX; y = inY;
	}

	/**
	 * @param inOther other vector
	 * @return scalar dot product
	 */
	public double dot(XYpoint inOther) {
		return (x * inOther.x + y * inOther.y);
	}

	/** @return length of vector */
	public double len() {return Math.sqrt(len2());}

	/** @return squared length of vector */
	public double len2() {return (x*x + y*y);}

	/**
	 * @param inOther other point object
	 * @return vector from this one to the other one
	 */
	public XYpoint vectorTo(XYpoint inOther) {
		return new XYpoint(inOther.x - x, inOther.y - y);
	}
}
