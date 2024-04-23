package tim.prune.function.filesleuth.data;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;

public class LocationFilter
{
	private final DataPoint _point;
	private final String _pointDescription;
	private final int _distanceValue;
	private final Unit _distanceUnit;
	private final double _distanceInMetres;
	private final double _distanceInRadians;

	public LocationFilter(DataPoint inPoint, String inDescription,
			int inDistanceValue, Unit inDistanceUnit)
	{
		_point = inPoint;
		_pointDescription = inDescription;
		_distanceValue = inDistanceValue;
		_distanceUnit = inDistanceUnit;
		_distanceInMetres = Distance.convertBetweenUnits(inDistanceValue,
			inDistanceUnit, UnitSetLibrary.UNITS_METRES);
		_distanceInRadians = Distance.convertDistanceToRadians(
			inDistanceValue, inDistanceUnit);
	}

	public DataPoint getPoint() {
		return _point;
	}

	public String getPointDescription() {
		return _pointDescription;
	}

	public int getDistanceValue() {
		return _distanceValue;
	}

	public Unit getDistanceUnit() {
		return _distanceUnit;
	}

	public double getDistanceInMetres() {
		return _distanceInMetres;
	}

	public double getDistanceInRadians() {
		return _distanceInRadians;
	}

	/** Check if the given point coordinates lies within this filter */
	public boolean doesLocationMatch(double inLatitude, double inLongitude)
	{
		final double filterLatitude = _point.getLatitude().getDouble();
		final double filterLongitude = _point.getLongitude().getDouble();
		final double calculatedRadians = Distance.calculateRadiansBetween(filterLatitude,
			filterLongitude, inLatitude, inLongitude);
		return calculatedRadians <= _distanceInRadians;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LocationFilter other = (LocationFilter) obj;
		return _point == other._point
			&& _distanceUnit == other._distanceUnit
			&& _distanceValue == other._distanceValue;
	}
}
