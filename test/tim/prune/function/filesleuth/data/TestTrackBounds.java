package tim.prune.function.filesleuth.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;

public class TestTrackBounds
{
	@Test
	public void testEmptyBounds()
	{
		TrackBounds bounds = new TrackBounds();
		Assertions.assertFalse(bounds.isLatitudeInRange(0.0, 0.0));
		Assertions.assertFalse(bounds.isLongitudeInRange(0.0, 0.0, 0.0));
	}

	@Test
	public void testRectangularBounds()
	{
		TrackBounds bounds = new TrackBounds();
		DataPoint point1 = new DataPoint(Latitude.make(4.9), Longitude.make(94.95));
		DataPoint point2 = new DataPoint(Latitude.make(5.01), Longitude.make(95.5));
		bounds.addPoint(point1);
		bounds.addPoint(point2);
		Assertions.assertFalse(bounds.isLatitudeInRange(0.0, 0.0));
		Assertions.assertFalse(bounds.isLongitudeInRange(0.0, 0.0, 0.0));
		Assertions.assertTrue(bounds.includesPoint(point1));
		Assertions.assertTrue(bounds.includesPoint(point2));
		DataPoint point3 = new DataPoint(Latitude.make(5.0), Longitude.make(95.0));
		Assertions.assertTrue(bounds.includesPoint(point3));
		DataPoint point4 = new DataPoint(Latitude.make(5.02), Longitude.make(95.0));
		Assertions.assertFalse(bounds.includesPoint(point4));
		Assertions.assertTrue(bounds.isLatitudeInRange(5.0, 0.0));
		Assertions.assertTrue(bounds.isLongitudeInRange(95.0, 0.0, 0.0));

		// out of rectangle but close enough
		Assertions.assertFalse(bounds.isLongitudeInRange(95.501, 5.0, 0.0));
		Assertions.assertTrue(bounds.isLongitudeInRange(95.501, 5.0, 120.0));
	}

	@Test
	public void testOtherSideOfDateline()
	{
		TrackBounds bounds = new TrackBounds();
		DataPoint point1 = new DataPoint(Latitude.make(70.0), Longitude.make(179.995));
		bounds.addPoint(point1);
		Assertions.assertFalse(bounds.isLatitudeInRange(0.0, 0.0));
		Assertions.assertFalse(bounds.isLongitudeInRange(0.0, 0.0, 0.0));
		Assertions.assertTrue(bounds.includesPoint(point1));

		// Probe point is -179 degrees so definitely outside the rectangle of +179
		DataPoint point2 = new DataPoint(Latitude.make(70.0), Longitude.make(-179.995));
		Assertions.assertFalse(bounds.includesPoint(point2));
		// but it's close
		Assertions.assertTrue(bounds.includesPoint(point2, 400.0));
	}

	@Test
	public void testBothSidesOfDateline()
	{
		TrackBounds bounds = new TrackBounds();
		// Make lots of points between -180 and -179 and between 179 and 180
		for (int i=0; i<1000; i++) {
			bounds.addPoint(new DataPoint(Latitude.make(70.0), Longitude.make(179.0 + i/1000.0)));
			bounds.addPoint(new DataPoint(Latitude.make(70.1), Longitude.make(-179.0 - i/1000.0)));
		}
		Assertions.assertFalse(bounds.isLatitudeInRange(0.0, 180.0));
		Assertions.assertTrue(bounds.isLatitudeInRange(70.05, 0.0));
		Assertions.assertFalse(bounds.isLatitudeInRange(70.101, 0.0));
		Assertions.assertTrue(bounds.isLatitudeInRange(70.101, 120.0));
		Assertions.assertFalse(bounds.isLongitudeInRange(0.0, 0.0, 0.0));

		// Between these points is "inside"
		final double validLatitude = 70.06;
		DataPoint point3 = new DataPoint(Latitude.make(validLatitude), Longitude.make(-179.900001));
		Assertions.assertTrue(bounds.includesPoint(point3));
		DataPoint point4 = new DataPoint(Latitude.make(validLatitude), Longitude.make(179.900001));
		Assertions.assertTrue(bounds.includesPoint(point4));

		// But -170 and +170 are both "outside" (even though they're both between -179 and 179)
		DataPoint point5 = new DataPoint(Latitude.make(validLatitude), Longitude.make(-170.0));
		Assertions.assertFalse(bounds.includesPoint(point5));
		DataPoint point6 = new DataPoint(Latitude.make(validLatitude), Longitude.make(170.0));
		Assertions.assertFalse(bounds.includesPoint(point6));
	}

	@Test
	public void testGroupsAtSameLongitude()
	{
		TrackBounds bounds = new TrackBounds();
		final double longitude = -44.5;
		bounds.addPoint(new DataPoint(Latitude.make(70.0), Longitude.make(longitude)));
		bounds.addPoint(new DataPoint(Latitude.make(-12.6), Longitude.make(longitude)));

		// Any points within this large latitude range are then included
		DataPoint point1 = new DataPoint(Latitude.make(0.0), Longitude.make(longitude));
		Assertions.assertTrue(bounds.includesPoint(point1));
		DataPoint point2 = new DataPoint(Latitude.make(13.0), Longitude.make(longitude));
		Assertions.assertTrue(bounds.includesPoint(point2));
	}

	@Test
	public void testGroupsAtSameLatitude()
	{
		TrackBounds bounds = new TrackBounds();
		final double latitude = -44.5;
		bounds.addPoint(new DataPoint(Latitude.make(latitude), Longitude.make(1.3)));
		bounds.addPoint(new DataPoint(Latitude.make(latitude), Longitude.make(9.3)));
		bounds.addPoint(new DataPoint(Latitude.make(latitude), Longitude.make(111.3)));
		bounds.addPoint(new DataPoint(Latitude.make(latitude), Longitude.make(116.3)));

		// Any points within each group are included
		DataPoint point1 = new DataPoint(Latitude.make(latitude), Longitude.make(5.0));
		Assertions.assertTrue(bounds.includesPoint(point1));
		DataPoint point2 = new DataPoint(Latitude.make(latitude), Longitude.make(6.0));
		Assertions.assertTrue(bounds.includesPoint(point2));
		DataPoint point3 = new DataPoint(Latitude.make(latitude), Longitude.make(115.1));
		Assertions.assertTrue(bounds.includesPoint(point3));
		// but points between the groups are not
		DataPoint point4 = new DataPoint(Latitude.make(latitude), Longitude.make(30.2));
		Assertions.assertFalse(bounds.includesPoint(point4));
		DataPoint point5 = new DataPoint(Latitude.make(latitude), Longitude.make(104.2));
		Assertions.assertFalse(bounds.includesPoint(point5));
		DataPoint point6 = new DataPoint(Latitude.make(latitude), Longitude.make(174.2));
		Assertions.assertFalse(bounds.includesPoint(point6));
		DataPoint point7 = new DataPoint(Latitude.make(latitude), Longitude.make(-4.2));
		Assertions.assertFalse(bounds.includesPoint(point7));
	}
}
