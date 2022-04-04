package tim.prune.gui.map.tile;

/**
 * Interface used by the TileWorker objects to communicate
 * back to their Coordinator, getting tile definitions to
 * process and returning the results
 */
public interface Coordinator
{
	TileDef getNextDefinition();

	void finishedTile(TileDef inDef, TileBytes inResult);

	void threadFinished();
}
