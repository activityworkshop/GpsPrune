package tim.prune.gui;

import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.map.MapPosition;
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
	 * @return coordinate bounds of current viewport
	 */
	public double[] getBounds()
	{
		int width = _mapCanvas.getWidth();
		int height = _mapCanvas.getHeight();
		MapPosition mapPosition = _mapCanvas.getMapPosition();
		double minLat = MapUtils.getLatitudeFromY(mapPosition.getYFromPixels(height, height));
		double maxLat = MapUtils.getLatitudeFromY(mapPosition.getYFromPixels(0, height));
		double minLon = MapUtils.getLongitudeFromX(mapPosition.getXFromPixels(0, width));
		double maxLon = MapUtils.getLongitudeFromX(mapPosition.getXFromPixels(width, width));
		return new double[] {minLat, minLon, maxLat, maxLon};
	}
}
