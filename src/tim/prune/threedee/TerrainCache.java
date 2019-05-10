package tim.prune.threedee;

import tim.prune.DataStatus;
import tim.prune.data.Track;

/**
 * This abstract class acts as a singleton to store a single
 * terrain model (as a Track) for a given data status and terrain definition.
 * When the data or the definition changes, this track becomes invalid.
 */
public abstract class TerrainCache
{
	/** The data status at the time this terrain was generated */
	private static DataStatus _dataStatus = null;
	/** The definition (grid size) for this terrain */
	private static TerrainDefinition _terrainDef = null;
	/** The generated grid of points with altitudes */
	private static Track _terrainTrack = null;


	/**
	 * Get the stored terrain track if it's still valid
	 * @param inCurrStatus current data status
	 * @param inTerrainDef currently selected terrain definition
	 * @return stored terrain track if it's valid, null otherwise
	 */
	public static Track getTerrainTrack(DataStatus inCurrStatus, TerrainDefinition inTerrainDef)
	{
		if (_dataStatus == null || _terrainDef == null || _terrainTrack == null)
		{
			return null; // nothing stored
		}
		if (inCurrStatus == null || inTerrainDef == null || !inTerrainDef.getUseTerrain())
		{
			return null; // nonsense requested
		}
		if (inCurrStatus.hasDataChanged(_dataStatus) || !inTerrainDef.equals(_terrainDef))
		{
			return null; // stored track is out of date
		}
		// we have a match
		return _terrainTrack;
	}

	/**
	 * Now that a terrain track has been generated, store it for possible reuse
	 * @param inTrack terrain track to store
	 * @param inCurrStatus current data status
	 * @param inTerrainDef terrain definition
	 */
	public static void storeTerrainTrack(Track inTrack, DataStatus inCurrStatus, TerrainDefinition inTerrainDef)
	{
		_terrainTrack = inTrack;
		_dataStatus = inCurrStatus;
		_terrainDef = inTerrainDef;
	}
}
