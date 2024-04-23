package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;
import tim.prune.function.edit.PointEdit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to set a single field on several points
 */
class EditSingleFieldTest
{
	@Test
	public void testEditSinglePoint()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);

		// Set
		PointEdit descEdit = new PointEdit(0, "Description");
		EditSingleFieldCmd command = new EditSingleFieldCmd(Field.DESCRIPTION, List.of(descEdit), null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals("Description", point.getFieldValue(Field.DESCRIPTION));
		// undo
		assertTrue(command.getInverse().execute(info));
		assertNull(point.getFieldValue(Field.DESCRIPTION));
	}

	@Test
	public void testEditSeveralPoints()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(6).setWaypointName("waypoint");
		for (int i=0; i<track.getNumPoints(); i++) {
			String expectedName = (i==6 ? "waypoint" : null);
			assertEquals(expectedName, track.getPoint(i).getWaypointName());
		}
		// Edit
		ArrayList<PointEdit> descEdits = new ArrayList<>();
		for (int i=4; i<10; i++) {
			descEdits.add(new PointEdit(i, "Point " + i));
		}
		EditSingleFieldCmd command = new EditSingleFieldCmd(Field.WAYPT_NAME, descEdits, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(12, track.getNumPoints());
		for (int i=0; i<track.getNumPoints(); i++) {
			String expectedName = ((i>=4 && i<10) ? ("Point " + i) : null);
			assertEquals(expectedName, track.getPoint(i).getWaypointName());
		}
		// Undo
		assertTrue(command.getInverse().execute(info));
		for (int i=0; i<track.getNumPoints(); i++) {
			String expectedName = (i==6 ? "waypoint" : null);
			assertEquals(expectedName, track.getPoint(i).getWaypointName());
		}
	}
}
