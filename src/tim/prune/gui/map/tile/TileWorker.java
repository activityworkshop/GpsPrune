package tim.prune.gui.map.tile;

/**
 * Abstract worker object which just does _something_
 * to process each given Tile Definition and return
 * the result in a TileBytes object
 */
public abstract class TileWorker implements Runnable
{
	protected final Coordinator _parent;

	public TileWorker(Coordinator inParent) {
		_parent = inParent;
	}

	@Override
	public void run()
	{
		TileDef tileDef = _parent.getNextDefinition();
		while (tileDef != null)
		{
			TileBytes result = processTile(tileDef);
			_parent.finishedTile(tileDef, result);
			tileDef = _parent.getNextDefinition();
		}
		_parent.threadFinished();
	}

	protected abstract TileBytes processTile(TileDef def);
}
