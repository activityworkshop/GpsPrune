package tim.prune.gui.profile;

public class ChartScale
{
	private double _previousMin = 0.0;
	private double _previousMax = 0.0;
	private int _previousMinLines = 0;
	private int _previousScale = 0;

	/**
	 * Work out the scale for the horizontal lines
	 * @param inMin min value of data
	 * @param inMax max value of data
	 * @param inMinLines minimum number of lines to draw, depending on space
	 * @return scale separation, or -1 for no scale
	 */
	public int getLineScale(double inMin, double inMax, int inMinLines)
	{
		if ((inMax - inMin) < 2.0 || (inMax - inMin) > Integer.MAX_VALUE) {
			return -1;
		}
		if (inMin == _previousMin && inMax == _previousMax
				&& inMinLines == _previousMinLines && _previousScale != 0)
		{
			return _previousScale;
		}
		_previousMin = inMin;
		_previousMax = inMax;
		_previousMinLines = inMinLines;
		int powerOfTen = (int) Math.pow(10.0, (int) Math.log10(inMax - inMin));
		int[] factors = new int[] {5, 2, 1};
		while (powerOfTen >= 1)
		{
			for (int f : factors)
			{
				int scale = f * powerOfTen;
				int numLines = (int) (inMax / scale) - (int) (inMin / scale);
				// If enough lines are produced then use this scale
				if (numLines >= inMinLines) {
					_previousScale = scale;
					return scale;
				}
			}
			powerOfTen = powerOfTen / 10;
		}
		// not enough lines, just try 1
		return 1;
	}
}
