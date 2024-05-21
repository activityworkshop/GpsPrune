package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;
import tim.prune.function.edit.PointAltitudeEdit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to set the altitude of several points
 */
class EditAltitudesTest
{
	@Test
	public void testEditSinglePoint()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		assertEquals(515.0, point.getAltitude().getMetricValue());

		// Set
		PointAltitudeEdit altitudeEdit = new PointAltitudeEdit(0, "3210", UnitSetLibrary.UNITS_METRES);
		EditAltitudeCmd command = new EditAltitudeCmd(List.of(altitudeEdit));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals(3210.0, point.getAltitude().getMetricValue());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(515.0, point.getAltitude().getMetricValue());
	}

	@Test
	public void testEditSeveralPoints()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		for (int i=0; i<track.getNumPoints(); i++) {
			track.getPoint(i).setFieldValue(Field.ALTITUDE, "" + i*5, UnitSetLibrary.getMetricUnitSet(), false);
		}
		// Check
		for (int i=0; i<track.getNumPoints(); i++) {
			double expectedAltitude = i * 5;
			assertEquals(expectedAltitude, track.getPoint(i).getAltitude().getMetricValue());
		}
		// Edit
		ArrayList<PointAltitudeEdit> altEdits = new ArrayList<>();
		for (int i=4; i<10; i++) {
			altEdits.add(new PointAltitudeEdit(i, "" + i*10, UnitSetLibrary.UNITS_METRES));
		}
		EditAltitudeCmd command = new EditAltitudeCmd(altEdits);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(12, track.getNumPoints());
		for (int i=0; i<track.getNumPoints(); i++) {
			double expectedAltitude = (i >= 4 && i < 10 ? i * 10 :  i * 5);
			assertEquals(expectedAltitude, track.getPoint(i).getAltitude().getMetricValue());
		}
		// Undo
		assertTrue(command.getInverse().execute(info));
		for (int i=0; i<track.getNumPoints(); i++) {
			double expectedAltitude = i * 5;
			assertEquals(expectedAltitude, track.getPoint(i).getAltitude().getMetricValue());
		}
	}

	@Test
	public void testChangeToMetres()
	{
		// A point originally has an altitude in units of feet, then it is given a new altitude
		// in metres (for example, from SRTM).  The undo should replace the original value and units.
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("3155", UnitSetLibrary.UNITS_FEET));
		track.appendPoint(point);
		assertEquals(3155, point.getAltitude().getValue(UnitSetLibrary.UNITS_FEET));
		assertEquals(962, point.getAltitude().getIntValue(UnitSetLibrary.UNITS_METRES));
		assertEquals(UnitSetLibrary.UNITS_FEET, point.getAltitude().getUnit());

		// Set using metres
		PointAltitudeEdit altitudeEdit = new PointAltitudeEdit(0, "876", UnitSetLibrary.UNITS_METRES);
		EditAltitudeCmd command = new EditAltitudeCmd(List.of(altitudeEdit));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals(2874, point.getAltitude().getIntValue(UnitSetLibrary.UNITS_FEET));
		assertEquals(876, point.getAltitude().getValue(UnitSetLibrary.UNITS_METRES));
		assertEquals(UnitSetLibrary.UNITS_METRES, point.getAltitude().getUnit());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(3155, point.getAltitude().getValue(UnitSetLibrary.UNITS_FEET));
		assertEquals(962, point.getAltitude().getIntValue(UnitSetLibrary.UNITS_METRES));
		assertEquals(UnitSetLibrary.UNITS_FEET, point.getAltitude().getUnit());
	}
}
