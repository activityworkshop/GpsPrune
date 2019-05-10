package tim.prune.threedee;

/**
 * Holds the definition of the terrain to use
 * (whether or not to use a terrain, and the resolution)
 */
public class TerrainDefinition
{
	private boolean _useTerrain = false;
	private int     _gridSize   = 0;

	/**
	 * Empty constructor specifying no terrain
	 */
	public TerrainDefinition()
	{
		this(false, 0);
	}

	/**
	 * Constructor
	 * @param inUse true to use a terrain
	 * @param inGridSize size of grid
	 */
	public TerrainDefinition(boolean inUse, int inGridSize)
	{
		setUseTerrain(inUse, inGridSize);
	}

	/**
	 * Set the parameters
	 * @param inUse true to use a terrain
	 * @param inGridSize size of grid
	 */
	public void setUseTerrain(boolean inUse, int inGridSize)
	{
		_useTerrain = inUse;
		_gridSize   = inGridSize;
	}

	/**
	 * @return true if terrain should be used, false otherwise
	 */
	public boolean getUseTerrain() {
		return _useTerrain && _gridSize > 2;
	}

	/**
	 * @return grid size
	 */
	public int getGridSize() {
		return _gridSize;
	}

	@Override
	/**
	 * Compare two TerrainDefinitions to see if they're equal
	 */
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof TerrainDefinition)) {
			return false;
		}
		TerrainDefinition other = (TerrainDefinition) obj;
		return _useTerrain == other._useTerrain
			&& _gridSize == other._gridSize;
	}
}
