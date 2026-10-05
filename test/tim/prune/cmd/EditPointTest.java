package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;
import tim.prune.function.edit.FieldEdit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to edit a single point
 */
class EditPointTest
{
	@Test
	public void testEditFields()
	{
		final Track track = new Track();
		final DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
			new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);

		// Edit several fields
		final List<FieldEdit> edits = List.of(new FieldEdit(Field.WAYPT_NAME, "some name"),
			new FieldEdit(Field.DESCRIPTION, "description"),
			new FieldEdit(Field.ALTITUDE, "1001"),
			new FieldEdit(Field.LONGITUDE, "W 2.381"));
		final Command command = new EditPointCmd(0, edits);
		final TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals("some name", point.getWaypointName());
		assertEquals("description", point.getFieldValue(Field.DESCRIPTION));
		assertEquals(1001, point.getAltitude().getValue());
		assertEquals("W002°22'51.600\"", point.getLongitude().output(Coordinate.Format.DEG_MIN_SEC));
		// undo
		command.getInverse().executeCommand(info);
		assertEquals(1, track.getNumPoints());
		assertNull(point.getWaypointName());
		assertNull(point.getFieldValue(Field.DESCRIPTION));
		assertEquals(515, point.getAltitude().getValue());
		assertEquals("E002°20'24.000\"", point.getLongitude().output(Coordinate.Format.DEG_MIN_SEC));
	}

	@Test
	public void testEdit_invalidLongitude()
	{
		final Track track = new Track();
		final DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("E2.50"),
			new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		// Longitude is 2.5 degrees east
		assertEquals("E002°30.0000'", track.getPoint(0).getLongitude().output(Coordinate.Format.DEG_MIN, 4));

		// Edit longitude
		final List<FieldEdit> edits = List.of(new FieldEdit(Field.LONGITUDE, "a"));
		final Command command = new EditPointCmd(0, edits);
		final TrackInfo info = new TrackInfo(track);
		assertFalse(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertTrue(track.getPoint(0).isValid());
		// Longitude is still 2.5 degrees east (not a), because edit failed
		assertEquals("E002°30.0000'", track.getPoint(0).getLongitude().output(Coordinate.Format.DEG_MIN, 4));
	}
}
