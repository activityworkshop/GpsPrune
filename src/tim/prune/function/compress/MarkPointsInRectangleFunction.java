package tim.prune.function.compress;

import tim.prune.App;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;

/**
 * Function to mark all the points inside or outside the selected rectangle
 */
public class MarkPointsInRectangleFunction extends MarkAndDeleteFunction
{
	/** Minimum and maximum latitude values of rectangle */
	private double _minLat = 0.0, _maxLat = 0.0;
	/** Minimum and maximum longitude values of rectangle */
	private double _minLon = 0.0, _maxLon = 0.0;
	/** Mark the ones inside or mark the ones outside? */
	private final boolean _markInside;

	/**
	 * Constructor
	 * @param inApp App object
	 * @param inInside true for marking points inside, false for outside
	 */
	public MarkPointsInRectangleFunction(App inApp, boolean inInside)
	{
		super(inApp);
		_markInside = inInside;
	}

	/** @return name key */
	public String getNameKey() {
		return _markInside ? "menu.track.markinsiderectangle" : "menu.track.markoutsiderectangle";
	}

	/**
	 * Set the coordinates of the rectangle
	 * @param inLon1 first longitude value
	 * @param inLat1 first latitude value
	 * @param inLon2 second longitude value
	 * @param inLat2 second latitude value
	 */
	public void setRectCoords(double inLon1, double inLat1, double inLon2, double inLat2)
	{
		if (inLon1 == inLon2 || inLat1 == inLat2)
		{
			// Coordinates not valid
			_minLat = _maxLat = _minLon = _maxLon = 0.0;
		}
		else
		{
			_minLon = Math.min(inLon1, inLon2);
			_maxLon = Math.max(inLon1, inLon2);
			_minLat = Math.min(inLat1, inLat2);
			_maxLat = Math.max(inLat1, inLat2);
		}
	}

	/**
	 * Begin the function using the set parameters
	 */
	public void begin()
	{
		if (_maxLon == _minLon || _maxLat == _minLat) {
			return;
		}

		// Loop over all points in track
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		int numMarked = 0;
		int numNotMarked = 0;
		_app.getTrackInfo().clearAllMarkers();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			// For each point, see if it's within the rectangle
			final double pointLon = point.getLongitude().getDouble();
			final double pointLat = point.getLatitude().getDouble();
			final boolean insideRect = (pointLon >= _minLon && pointLon <= _maxLon
				&& pointLat >= _minLat && pointLat <= _maxLat);
			if (insideRect == _markInside) {
				_app.getTrackInfo().markPointForDeletion(i);
				numMarked++;
			}
			else {
				numNotMarked++;
			}
		}

		// Inform subscribers to update display
		UpdateMessageBroker.informSubscribers();
		// Confirm message showing how many marked
		if (numMarked > 0 && numNotMarked > 0) {
			optionallyDeleteMarkedPoints(numMarked);
		}
	}

	/** for this function, segments should be split at deleted points */
	protected boolean getShouldSplitSegments() {
		return true;
	}
}
