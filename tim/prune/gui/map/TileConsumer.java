package tim.prune.gui.map;

/**
 * Interface used by the MapTileManager to communicate back to its consumers
 */
public interface TileConsumer
{
	/** Let the consumer know that the tiles have been updated */
	public void tilesUpdated(boolean inIsOk);
}
