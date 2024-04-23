package tim.prune.threedee;

/**
 * Holds the definition of the terrain to use
 * (whether or not to use a terrain, and the resolution)
 */
public class TerrainDefinition
{
	private final boolean _useTerrain;
	private final int _gridSize;

	/**
	 * Constructor
	 * @param inUse true to use a terrain
	 * @param inGridSize size of grid
	 */
	public TerrainDefinition(boolean inUse, int inGridSize)
	{
		_useTerrain = inUse && inGridSize > 2;
		_gridSize = inGridSize;
	}

	/**
	 * @return true if terrain should be used, false otherwise
	 */
	public boolean getUseTerrain() {
		return _useTerrain;
	}

	/**
	 * @return grid size
	 */
	public int getGridSize() {
		return _gridSize;
	}

	/**
	 * Compare two TerrainDefinitions to see if they're equal
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof TerrainDefinition)) {
			return false;
		}
		TerrainDefinition other = (TerrainDefinition) obj;
		return _useTerrain == other._useTerrain
			&& _gridSize == other._gridSize;
	}
}
