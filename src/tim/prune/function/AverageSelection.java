package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.InsertPointCmd;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.Unit;

/**
 * Function to create an average point from a selection
 */
public class AverageSelection extends GenericFunction
{
	public AverageSelection(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "menu.range.average";
	}

	@Override
	public void begin()
	{
		Selection selection = _app.getTrackInfo().getSelection();
		DataPoint averagePoint = average(_app.getTrackInfo().getTrack(), selection.getStart(), selection.getEnd());
		if (averagePoint != null)
		{
			InsertPointCmd command = new InsertPointCmd(averagePoint, -1);
			command.setDescription(I18nManager.getText("undo.createpoint"));
			command.setConfirmText(I18nManager.getText("confirm.createpoint"));
			_app.execute(command);
		}
	}

	/**
	 * Average selected points
	 * @param inStartIndex start index of selection
	 * @param inEndIndex end index of selection
	 * @return true if successful
	 */
	private DataPoint average(Track inTrack, int inStartIndex, int inEndIndex)
	{
		// check parameters
		if (inStartIndex < 0 || inStartIndex >= inTrack.getNumPoints() || inEndIndex <= inStartIndex) {
			return null;
		}

		DataPoint startPoint = inTrack.getPoint(inStartIndex);
		double firstLatitude = startPoint.getLatitude().getDouble();
		double firstLongitude = startPoint.getLongitude().getDouble();
		double latitudeDiff = 0.0, longitudeDiff = 0.0;
		double totalAltitude = 0;
		int numAltitudes = 0;
		Unit altUnit = null;
		// loop between start and end points
		for (int i=inStartIndex; i<= inEndIndex; i++)
		{
			DataPoint currPoint = inTrack.getPoint(i);
			if (currPoint == null) {
				break;
			}
			latitudeDiff += (currPoint.getLatitude().getDouble() - firstLatitude);
			longitudeDiff += (currPoint.getLongitude().getDouble() - firstLongitude);
			if (currPoint.hasAltitude())
			{
				totalAltitude += currPoint.getAltitude().getValue(altUnit);
				// Use altitude format of first valid altitude
				if (altUnit == null)
					altUnit = currPoint.getAltitude().getUnit();
				numAltitudes++;
			}
		}
		int numPoints = inEndIndex - inStartIndex + 1;
		double meanLatitude = firstLatitude + (latitudeDiff / numPoints);
		double meanLongitude = firstLongitude + (longitudeDiff / numPoints);
		Altitude meanAltitude = null;
		if (numAltitudes > 0) {
			meanAltitude = new Altitude((int) (totalAltitude / numAltitudes), altUnit);
		}

		DataPoint averagePoint = new DataPoint(new Latitude(meanLatitude, Coordinate.FORMAT_DECIMAL_FORCE_POINT),
			new Longitude(meanLongitude, Coordinate.FORMAT_DECIMAL_FORCE_POINT), meanAltitude);
		averagePoint.setSegmentStart(true);
		return averagePoint;
	}
}
