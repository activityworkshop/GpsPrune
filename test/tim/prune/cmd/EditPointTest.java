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
		Track track = new Track();
		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"),
			new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);

		// Edit several fields
		List<FieldEdit> edits = List.of(new FieldEdit(Field.WAYPT_NAME, "some name"),
			new FieldEdit(Field.DESCRIPTION, "description"),
			new FieldEdit(Field.ALTITUDE, "1001"),
			new FieldEdit(Field.LONGITUDE, "W 2.381"));
		Command command = new EditPointCmd(0, edits);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals("some name", point.getWaypointName());
		assertEquals("description", point.getFieldValue(Field.DESCRIPTION));
		assertEquals(1001, point.getAltitude().getValue());
		assertEquals("W002°22'51.5\"", point.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
		// undo
		command.getInverse().executeCommand(info);
		assertEquals(1, track.getNumPoints());
		assertNull(point.getWaypointName());
		assertNull(point.getFieldValue(Field.DESCRIPTION));
		assertEquals(515, point.getAltitude().getValue());
		assertEquals("E002°20'23.9\"", point.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
	}
}
