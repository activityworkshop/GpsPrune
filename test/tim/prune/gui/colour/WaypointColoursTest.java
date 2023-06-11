package tim.prune.gui.colour;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

/**
 * Testing of colour generation for waypoint symbols according to waypoint type
 */
class WaypointColoursTest
{

	@Test
	void testGetColourForNoType()
	{
		WaypointColours wpc = new WaypointColours();
		wpc.setSalt(0);
		assertNull(wpc.getColourForType(null));
		assertNull(wpc.getColourForType(""));
		assertNull(wpc.getColourForType(" "));
	}

	@Test
	void testGetColourWhenDeactivated()
	{
		WaypointColours wpc = new WaypointColours();
		assertNull(wpc.getColourForType(null));
		assertNull(wpc.getColourForType(""));
		assertNull(wpc.getColourForType(" "));
		assertNull(wpc.getColourForType("geocache"));
		wpc.setSalt(30);	// activate
		assertNotNull(wpc.getColourForType("geocache"));
		wpc.setSalt(-2);	// deactivate again
		assertNull(wpc.getColourForType("geocache"));
	}

	@Test
	void testGetColourForType()
	{
		WaypointColours wpc = new WaypointColours();
		wpc.setSalt(0);
		Color c1 = wpc.getColourForType("waypoint");
		assertNotNull(c1);
		Color c2 = wpc.getColourForType("WayPoint");
		assertEquals(c1, c2); // case insensitive

		Color c3 = wpc.getColourForType("campsite");
		assertNotEquals(c1, c3); // very unlikely!
	}

	@Test
	void testGetColourWithSalt()
	{
		WaypointColours wpc = new WaypointColours();
		wpc.setSalt(7);
		final String wptType = "bus stop";
		Color c1 = wpc.getColourForType(wptType);
		wpc.setSalt(8);
		Color c2 = wpc.getColourForType(wptType);
		assertNotEquals(c1, c2); // different salt
		wpc.setSalt(7);
		Color c3 = wpc.getColourForType(wptType);
		assertEquals(c1, c3); // back to first salt

		// Cycle through all salt values for a single type
		HashSet<String> allColours = new HashSet<>();
		for (int i=0; i<30; i++)
		{
			wpc.setSalt(i);
			Color col = wpc.getColourForType("RESTAURANT");
			allColours.add(col.toString());
		}
		assertEquals(10, allColours.size()); // in general some number <= 10 because of limited colour range
	}
}
