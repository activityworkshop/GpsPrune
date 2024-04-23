package tim.prune.function.filesleuth.data;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.DoubleRange;
import tim.prune.data.UnitSetLibrary;

/** Holds the bounding area(s) of a track */
public class TrackBounds
{
	private final DoubleRange _latitudeRange = new DoubleRange();
	private final DoubleRange[] _longitudeRanges;

	public TrackBounds()
	{
		_longitudeRanges = new DoubleRange[36];
		for (int i=0; i<_longitudeRanges.length; i++) {
			_longitudeRanges[i] = new DoubleRange();
		}
	}

	public void addPoint(DataPoint inPoint) {
		addCoordinates(inPoint.getLatitude().getDouble(),
			inPoint.getLongitude().getDouble());
	}

	public void addCoordinates(double inLatitude, double inLongitude)
	{
		_latitudeRange.addValue(inLatitude);
		final int pocket = getPocket(inLongitude);
		_longitudeRanges[pocket].addValue(inLongitude);
	}

	/** @return which pocket the given longitude belongs to */
	private static int getPocket(double inLongitude) {
		return ((int) (inLongitude + 180.0)) / 10;
	}

	public boolean includesPoint(DataPoint inPoint) {
		return includesPoint(inPoint, 0.0);
	}

	public boolean includesPoint(DataPoint inPoint, double inDistInMetres)
	{
		final double latitude = inPoint.getLatitude().getDouble();
		return isLatitudeInRange(latitude, inDistInMetres)
			&& isLongitudeInRange(inPoint.getLongitude().getDouble(), latitude, inDistInMetres);
	}

	boolean isLatitudeInRange(double inLatitude, double inDistLimitInMetres)
	{
		if (_latitudeRange.includes(inLatitude)) {
			return true;
		}
		final double radiansFromEdge;
		if (inLatitude < _latitudeRange.getMinimum()) {
			radiansFromEdge = Distance.calculateRadiansBetween(inLatitude, 0.0, _latitudeRange.getMinimum(), 0.0);
		}
		else {
			radiansFromEdge = Distance.calculateRadiansBetween(inLatitude, 0.0, _latitudeRange.getMaximum(), 0.0);
		}
		final double distanceFromEdgeMetres = Distance.convertRadiansToDistance(radiansFromEdge, UnitSetLibrary.UNITS_METRES);
		return distanceFromEdgeMetres < inDistLimitInMetres;
	}

	boolean isLongitudeInRange(double inLongitude, double inLatitude, double inDistLimitInMetres)
	{
		for (DoubleRange lonRange : _longitudeRanges)
		{
			if (lonRange.hasData())
			{
				if (lonRange.includes(inLongitude)) {
					return true;
				}
				// maybe our longitude is just beyond the edge of this range
				double radiansFromWesternEdge = Distance.calculateRadiansBetween(inLatitude,
					inLongitude, inLatitude, lonRange.getMinimum());
				double radiansFromEasternEdge = Distance.calculateRadiansBetween(inLatitude,
					inLongitude, inLatitude, lonRange.getMaximum());
				double minRadiansFromEdge = Math.min(radiansFromWesternEdge, radiansFromEasternEdge);
				double distanceFromEdgeMetres = Distance.convertRadiansToDistance(
					minRadiansFromEdge, UnitSetLibrary.UNITS_METRES);
				if (distanceFromEdgeMetres < inDistLimitInMetres) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean overlaps(LocationFilter inFilter)
	{
		return inFilter == null
			|| inFilter.getPoint() == null
			|| includesPoint(inFilter.getPoint(), inFilter.getDistanceInMetres());
	}
}
