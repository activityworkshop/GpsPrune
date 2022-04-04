package tim.prune.gui.map.tile;

/**
 * Can be used to create a new worker for the coordinator
 */
public interface TileWorkerBuilder {
	TileWorker createWorker(Coordinator inCoordinator);
}
