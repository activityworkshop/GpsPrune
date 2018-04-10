package tim.prune.threedee;

import tim.prune.DataStatus;
import tim.prune.data.Track;

/**
 * Interface to decouple from Java3D classes
 */
public interface ThreeDWindow
{

	/**
	 * Set the Track data
	 * @param inTrack Track object
	 */
	public void setTrack(Track inTrack);

	/**
	 * @param inFactor altitude factor to use
	 */
	public void setAltitudeFactor(double inFactor);

	/**
	 * @param inSphereSize	sphere size to use
	 */
	public void setSphereSize(float inSphereSize);

	/**
	 * @param inRodSize	rod size to use
	 */
	public void setRodSize(float inRodSize);

	/**
	 * @param inStyle	<code>true</code> to show rods and spheres,
	 * 					<code>false</code> to show only spheres
	 */
	public void setStyle(boolean inStyle);

	/**
	 * @param inProjection <code>true</code> for orthographic projection
	 */
	public void setProjection(boolean inProjection);

	/**
	 * @param inShowScale	<code>true</code> to show scaling
	 */
	public void setShowScale(boolean inShowScale);

	/**
	 * @param inLighting	<code>true</code> for cartographic lighting
	 */
	public void setCartographic(boolean inCartographic);

	/**
	 * @param inDefinition image definition (image or not, source, zoom)
	 */
	public void setBaseImageParameters(ImageDefinition inDefinition);

	/**
	 * @param inDefinition terrain definition (terrain or not, resolution)
	 */
	public void setTerrainParameters(TerrainDefinition inDefinition);

	/**
	 * @param inStatus current data status for caching
	 */
	public void setDataStatus(DataStatus inStatus);

	/**
	 * Show the window
	 * @throws ThreeDException when 3d classes not found
	 */
	public void show() throws ThreeDException;
}
