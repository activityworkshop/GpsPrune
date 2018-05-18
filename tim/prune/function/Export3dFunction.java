package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.threedee.ImageDefinition;
import tim.prune.threedee.TerrainDefinition;

/**
 * Abstract superclass of any 3d export function, currently only the PovExporter
 */
public abstract class Export3dFunction extends GenericFunction
{
	/** altitude exaggeration factor */
	protected double _altFactor = 5.0;
	/** definition of terrain */
	protected TerrainDefinition _terrainDef = null;
	/** definition of base image */
	protected ImageDefinition _imageDef = null;

	/**
	 * Required constructor
	 * @param inApp App object
	 */
	public Export3dFunction(App inApp) {
		super(inApp);
	}

	/**
	 * Set the coordinates for the camera
	 * @param inX X coordinate of camera
	 * @param inY Y coordinate of camera
	 * @param inZ Z coordinate of camera
	 */
	public abstract void setCameraCoordinates(double inX, double inY, double inZ);

	/**
	 * @param inFactor exaggeration factor
	 */
	public void setAltitudeExaggeration(double inFactor)
	{
		if (inFactor >= 1.0) {
			_altFactor = inFactor;
		}
	}

	/**
	 * @param inDefinition terrain definition, or null
	 */
	public void setTerrainDefinition(TerrainDefinition inDefinition)
	{
		_terrainDef = inDefinition;
	}

	/**
	 * @param inDefinition image definition, or null
	 */
	public void setImageDefinition(ImageDefinition inDefinition)
	{
		_imageDef = inDefinition;
	}
}
