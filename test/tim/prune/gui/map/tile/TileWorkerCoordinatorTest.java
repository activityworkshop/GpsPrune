package tim.prune.gui.map.tile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.OsmMapSource;

/**
 * Unit test for the coordination of tile processing
 */
class TileWorkerCoordinatorTest
{

	/**
	 * Request the same tile three times, and ensure only one worker is created and only one tile processed
	 */
	@Test
	void testOneTileRequestedThreeTimes()
	{
		FakeWorker.resetAll();
		TileWorkerCoordinator coordinator = new TileWorkerCoordinator(null, FakeWorker::new);
		TileDef tile1 = new TileDef(getMapSource(), 0, 1, 2, 3);
		coordinator.triggerDownload(tile1);
		coordinator.triggerDownload(tile1);
		waitABit();
		coordinator.triggerDownload(tile1);
		FakeWorker.finish(tile1);
		waitABit();

		System.out.println("one worker but the same tile requested multiple times:");
		checkExpectedResults(1, 1); // expect only one worker, and only one tile
	}

	private void checkExpectedResults(int inExpectedWorkers, int inExpectedTiles)
	{
		assertEquals(inExpectedWorkers, FakeWorker.getCounter()); // correct number of workers created
		int numTilesDownloaded = 0;
		for (FakeWorker.WorkLog l : FakeWorker.logs) {
			System.out.println("W" + l.ctr + " action " + l.action + (l.tileDef == null ? "" : (" " + l.tileDef)));
			if (l.action == FakeWorker.WorkLog.Action.COMPLETED) {numTilesDownloaded++;}
		}
		assertEquals(inExpectedTiles, numTilesDownloaded); // correct number of tiles processed
		System.out.println("---");
	}

	@Test
	void testTwoTilesTwoWorkers()
	{
		FakeWorker.resetAll();
		TileWorkerCoordinator coordinator = new TileWorkerCoordinator(null, FakeWorker::new);
		TileDef tile1 = new TileDef(getMapSource(), 0, 1, 2, 3);
		TileDef tile2 = new TileDef(getMapSource(), 1, 2, 3, 4);
		coordinator.triggerDownload(tile1);
		waitABit();
		coordinator.triggerDownload(tile2);
		waitABit();
		FakeWorker.finish(tile1);
		waitABit();
		FakeWorker.finish(tile2);
		waitABit();
		System.out.println("two workers:");
		checkExpectedResults(2, 2);
	}

	@Test
	void testFourTilesTwoWorkers()
	{
		FakeWorker.resetAll();
		TileWorkerCoordinator coordinator = new TileWorkerCoordinator(null, FakeWorker::new, 2); // limit to max 2 workers
		TileDef tile1 = new TileDef(getMapSource(), 0, 1, 2, 3);
		TileDef tile2 = new TileDef(getMapSource(), 1, 2, 3, 4);
		TileDef tile3 = new TileDef(getMapSource(), 1, -1, 13, 11);
		TileDef tile4 = new TileDef(getMapSource(), 0, -2, 12, 11);
		coordinator.triggerDownload(tile1);
		waitABit();
		coordinator.triggerDownload(tile2);
		coordinator.triggerDownload(tile3);
		waitABit();
		FakeWorker.finish(tile1);
		waitABit();
		FakeWorker.finish(tile2);
		waitABit();
		FakeWorker.finish(tile3);
		waitABit();
		coordinator.triggerDownload(tile4);	// results in a third worker being created, the other two have died
		waitABit();
		FakeWorker.finish(tile4);
		waitABit();
		System.out.println("max two workers but now four tiles:");
		checkExpectedResults(3, 4);
	}

	private static void waitABit() {
		try {Thread.sleep(200L);} catch(InterruptedException ignored) {}
	}

	private static MapSource getMapSource() {
		return new OsmMapSource("exampleName", "https://blah.org/");
	}
}
