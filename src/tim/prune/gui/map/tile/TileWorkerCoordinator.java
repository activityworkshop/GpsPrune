package tim.prune.gui.map.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class TileWorkerCoordinator implements Coordinator
{
	private final TileManager _parent;
	private final TileWorkerBuilder _builder;
	private final LinkedList<TileDef> _waitingDefs = new LinkedList<>();
	private final HashSet<TileDef> _processingDefs = new HashSet<>();
	private final ArrayList<Thread> _threads = new ArrayList<>();
	private int _previousZoom = -1;
	private final int _maxNumThreads;
	private static final int DEFAULT_MAX_THREADS = 20;


	/**
	 * Regular constructor
	 * @param inParent manager parent to be notified of results
	 * @param inBuilder builder which can create the workers
	 */
	public TileWorkerCoordinator(TileManager inParent, TileWorkerBuilder inBuilder) {
		this(inParent, inBuilder, DEFAULT_MAX_THREADS);
	}

	/**
	 * Constructor used by unit tests
	 * @param inParent manager parent to be notified of results
	 * @param inBuilder builder which can create the workers
	 * @param inMaxThreads maximum number of threads to allow instead of the default
	 */
	public TileWorkerCoordinator(TileManager inParent, TileWorkerBuilder inBuilder, int inMaxThreads) {
		_parent = inParent;
		_builder = inBuilder;
		_maxNumThreads = inMaxThreads;
	}

	/**
	 * Entry method to trigger a tile download
	 * @param inDef definition of requested tile
	 */
	public synchronized void triggerDownload(TileDef inDef)
	{
		for (TileDef def : _waitingDefs) {
			if (def.equals(inDef)) {
				return; // already queued
			}
		}
		if (_processingDefs.contains(inDef)) {
			return; // already being processed
		}
		// New, so add to queue
		_waitingDefs.add(inDef);
		// Maybe add another worker
		if (_threads.size() < _maxNumThreads)
		{
			Thread thread = new Thread(_builder.createWorker(this));
			_threads.add(thread);
			thread.start();
		}
	}

	@Override
	public synchronized TileDef getNextDefinition()
	{
		TileDef def = _waitingDefs.isEmpty() ? null : _waitingDefs.pop();
		if (def == null) {
			return null;
		}
		int zoom = def._zoom;
		if (zoom != _previousZoom)
		{
			_processingDefs.clear();
			_previousZoom = zoom;
		}
		_processingDefs.add(def);
		return def;
	}

	@Override
	public synchronized void finishedTile(TileDef inDef, TileBytes inResult)
	{
		if (_parent != null) {
			_parent.returnTile(inDef, inResult);
		}
		_processingDefs.remove(inDef);
	}

	@Override
	public void threadFinished()
	{
		// Go through _threads and remove the finished ones
		synchronized(this)
		{
			ArrayList<Thread> livingThreads = new ArrayList<>();
			for (Thread t : _threads)
			{
				if (t.isAlive() && t != Thread.currentThread()) {
					livingThreads.add(t);
				}
			}
			_threads.clear();
			_threads.addAll(livingThreads);
		}
	}
}
