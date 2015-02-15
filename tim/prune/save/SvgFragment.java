package tim.prune.save;

/**
 * Class to enable the sorting of Svg fragments
 */
public class SvgFragment implements Comparable<SvgFragment>
{
	private String _fragment = null;
	private int _yCoord = 0;

	/**
	 * Constructor
	 * @param inFragment fragment of svg source
	 * @param inYCoord y coordinate of point, for sorting
	 */
	public SvgFragment(String inFragment, int inYCoord)
	{
		_fragment = inFragment;
		_yCoord = inYCoord;
	}

	/**
	 * @return svg fragment
	 */
	public String getFragment()
	{
		return _fragment;
	}

	/**
	 * Compare method
	 */
	public int compareTo(SvgFragment inOther)
	{
		int ycompare = _yCoord - inOther._yCoord;
		if (ycompare != 0) {return ycompare;}
		return _fragment.compareTo(inOther._fragment);
	}

	/**
	 * @param inOther other fragment to compare this one with
	 * @return true if the fragments are equal
	 */
	public boolean equals(SvgFragment inOther)
	{
		return _fragment.equals(inOther._fragment);
	}

	/**
	 * @param inOther other object to compare this one with
	 * @return true if the objects are equal
	 */
	public boolean equals(Object inOther) {
		return (inOther instanceof SvgFragment?equals((SvgFragment) inOther):false);
	}
}
