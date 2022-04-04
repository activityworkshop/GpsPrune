package tim.prune.gui.map.tile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.OsmMapSource;

class TileDefTest
{
	@Test
	void testEquality()
	{
		MapSource source = new OsmMapSource("exampleName", "https://blah.org/");
		TileDef tile1 = new TileDef(source, 0, 1, 2, 3);
		TileDef tile2 = new TileDef(source, 0, 1, 2, 3);
		TileDef tile3 = new TileDef(source, 0, 1, 4, 3);

		assertFalse(tile1 == tile2); // two different objects
		assertEquals(tile1, tile2); // but they're equal
		assertNotEquals(tile1, tile3); // third tile has different y

		HashSet<TileDef> tileSet = new HashSet<>();
		tileSet.add(tile1);
		tileSet.add(tile3);
		tileSet.add(tile2);
		assertEquals(2, tileSet.size());
	}

	@Test
	void testZoomOut()
	{
		MapSource source = new OsmMapSource("exampleName", "https://blah.org/");
		TileDef tile1 = new TileDef(source, 0, 100, 200, 9);
		TileDef tile2 = tile1.zoomOut();
		assertNotEquals(tile1, tile2);
		assertEquals(tile1._mapSource, tile2._mapSource);
		assertEquals(tile1._layerIdx, tile2._layerIdx);
		assertEquals(tile1._x / 2, tile2._x);
		assertEquals(tile1._y / 2, tile2._y);
		assertEquals(tile1._zoom - 1, tile2._zoom);

		// Tile next to tile1 zooms out to the same tile on the next zoom level
		TileDef tile3 = new TileDef(source, tile1._layerIdx, tile1._x+1, tile1._y+1, tile1._zoom);
		TileDef tile4 = tile3.zoomOut();
		assertFalse(tile2 == tile4);
		assertEquals(tile2, tile4);
	}

	@Test
	void testZoomIn()
	{
		MapSource source = new OsmMapSource("exampleName", "https://blah.org/");
		int x = 1001, y = 2002;
		TileDef tile1 = new TileDef(source, 1, x, y, 10);
		TileDef tile2 = tile1.zoomOut();
		for (int i=0; i<4; i++) {
			TileDef quarter = tile2.zoomIn(i);
			if (i == 1) {
				assertEquals(tile1, quarter);
			} else {
				assertNotEquals(tile1, quarter);
			}
		}
	}
}
