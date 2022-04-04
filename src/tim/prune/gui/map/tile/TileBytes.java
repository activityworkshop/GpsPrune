package tim.prune.gui.map.tile;

/**
 * Container for the results of a TileWorker
 * having processed a tile definition
 */
public class TileBytes
{
	public final byte[] data;

	public TileBytes(int inSize) {
		data = new byte[inSize];
	}
}
