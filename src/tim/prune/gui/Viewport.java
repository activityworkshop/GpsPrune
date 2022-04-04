package tim.prune.gui;

import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.MapUtils;

/**
 * Class to provide access to current viewport
 * The point of this class is to decouple the view from the MapCanvas object
 * so that when a search function needs to know the area currently viewed, it doesn't
 * need to have a direct connection to the MapCanvas.  Instead it asks the App for the viewport,
 * which is then able to get the map position from the MapCanvas.
 * I'm still not sure whether this is ugly or not, but it's more efficient than constantly listening.
 */
public class Viewport
{
	/** Map canvas object */
	private MapCanvas _mapCanvas = null;

	/**
	 * Constructor
	 * @param inCanvas map canvas object
	 */
	public Viewport(MapCanvas inCanvas)
	{
		_mapCanvas = inCanvas;
	}

	/**
	 * @return latitude at centre of viewport
	 */
	public double getCentreLatitude()
	{
		final double minLat = MapUtils.getLatitudeFromY(_mapCanvas.getMinYValue());
		final double maxLat = MapUtils.getLatitudeFromY(_mapCanvas.getMaxYValue());
		return (minLat + maxLat) / 2.0;
	}

	/**
	 * @return longitude at centre of viewport
	 */
	public double getCentreLongitude()
	{
		final double minLon = MapUtils.getLongitudeFromX(_mapCanvas.getMinXValue());
		double maxLon = MapUtils.getLongitudeFromX(_mapCanvas.getMaxXValue());
		if (maxLon < minLon) {
			maxLon += 360.0;
		}
		double result = (minLon + maxLon) / 2.0;
		if (result > 360.0) {
			result -= 360.0;
		}
		return result;
	}
}
