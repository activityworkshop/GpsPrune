package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.DataPoint;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.profile.SpeedData;

/**
 * Function to select an extreme point from the track
 */
public class SelectExtremePoint extends GenericFunction
{
	public enum Extreme {HIGHEST, LOWEST, FASTEST}

	private final Extreme _extreme;

	public SelectExtremePoint(App inApp, Extreme inExtreme) {
		super(inApp);
		_extreme = inExtreme;
	}

	@Override
	public void begin()
	{
		if (_extreme == Extreme.HIGHEST || _extreme == Extreme.LOWEST) {
			selectHighestOrLowest();
		}
		else if (_extreme == Extreme.FASTEST) {
			selectFastest();
		}
		else {
			throw new IllegalArgumentException("Unexpected extreme: " + _extreme);
		}
	}

	private void selectHighestOrLowest()
	{
		int bestPointIndex = -1;
		double bestValue = 0.0;
		for (int i=0; i<_app.getTrackInfo().getTrack().getNumPoints(); i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			double currValue = 0.0;
			if (!point.hasAltitude()) {
				continue;
			}
			currValue = point.getAltitude().getMetricValue();
			if (_extreme == Extreme.LOWEST) {
				currValue = -currValue;
			}
			if (bestPointIndex == -1 || currValue > bestValue)
			{
				bestValue = currValue;
				bestPointIndex = i;
			}
		}
		if (bestPointIndex >= 0) {
			_app.getTrackInfo().selectPoint(bestPointIndex);
		}
	}

	public void selectFastest()
	{
		SpeedData speeds = new SpeedData(_app.getTrackInfo().getTrack());
		speeds.init(UnitSetLibrary.getUnitSet(UnitSetLibrary.UNITSET_METRIC));
		int bestPointIndex = -1;
		double maxSpeed = 0.0;
		for (int i=0; i<_app.getTrackInfo().getTrack().getNumPoints(); i++)
		{
			double speed = 0.0;
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point.hasHSpeed()) {
				speed = point.getHSpeed().getValueInMetresPerSec();
			}
			else if (speeds.hasData(i)) {
				speed = speeds.getData(i);
			}
			else {
				continue;
			}
			if (bestPointIndex == -1 || speed > maxSpeed)
			{
				maxSpeed = speed;
				bestPointIndex = i;
			}
		}
		if (bestPointIndex > -1) {
			_app.getTrackInfo().selectPoint(bestPointIndex);
		}
	}

	@Override
	public String getNameKey() {
		return "menu.point.goto." + _extreme.toString().toLowerCase();
	}
}
