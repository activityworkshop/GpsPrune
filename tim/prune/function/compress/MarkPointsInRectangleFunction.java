package tim.prune.function.compress;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;

/**
 * Function to mark all the points in the selected rectangle
 */
public class MarkPointsInRectangleFunction extends GenericFunction
{
	/** Minimum and maximum latitude values of rectangle */
	private double _minLat = 0.0, _maxLat = 0.0;
	/** Minimum and maximum longitude values of rectangle */
	private double _minLon = 0.0, _maxLon = 0.0;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public MarkPointsInRectangleFunction(App inApp)
	{
		super(inApp);
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
			if (inLon2 > inLon1) {
				_minLon = inLon1; _maxLon = inLon2;
			}
			else {
				_minLon = inLon2; _maxLon = inLon1;
			}
			if (inLat2 > inLat1) {
				_minLat = inLat1; _maxLat = inLat2;
			}
			else {
				_minLat = inLat2; _maxLat = inLat1;
			}
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
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			// For each point, see if it's within the rectangle
			final double pointLon = point.getLongitude().getDouble();
			final double pointLat = point.getLatitude().getDouble();
			final boolean insideRect = (pointLon >= _minLon && pointLon <= _maxLon
				&& pointLat >= _minLat && pointLat <= _maxLat);
			// If so, then mark it
			point.setMarkedForDeletion(insideRect);
			if (insideRect) {
				numMarked++;
			}
		}

		// Inform subscribers to update display
		UpdateMessageBroker.informSubscribers();
		// Confirm message showing how many marked
		if (numMarked > 0) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.compress.confirm1")
				+ " " + numMarked + " " + I18nManager.getText("dialog.compress.confirm2"),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/** @return name key */
	public String getNameKey() {
		return "menu.track.markrectangle";
	}
}
