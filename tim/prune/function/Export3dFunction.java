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

	protected String _sphereSize = null;
	protected static String _rodSize = null;
	protected String _projection = null;
	protected String _lighting = null;
	protected String _style = null;
	protected String _scales = null;

	/** definition of terrain */
	protected TerrainDefinition _terrainDef = null;
	/** definition of base image */
	protected ImageDefinition _imageDef = null;

	/**
	 * Required constructor
	 * @param inApp App object
	 */
	public Export3dFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * Set the coordinates for the camera.
	 *
	 * @param inX X coordinate of camera
	 * @param inY Y coordinate of camera
	 * @param inZ Z coordinate of camera
	 */
	public abstract void setCameraCoordinates(
		double inX, double inY, double inZ);

	/**
	 * @param inFactor exaggeration factor
	 */
	public void setAltitudeExaggeration(double inFactor)
	{
		if (inFactor >= 1.0)
		{
			_altFactor = inFactor;
		}
	}

	public void setSphereSize(String inSphereSize)
	{
		_sphereSize = inSphereSize;
	}

	public void setRodSize(String inRodSize)
	{
		_rodSize = inRodSize;
	}

	public void setProjection(String inProjection)
	{
		_projection = inProjection;
	}

	public void setLighting(String inLighting)
	{
		_lighting = inLighting;
	}

	public void setStyle(String inStyle)
	{
		_style = inStyle;
	}

	public void setScales(String inScales)
	{
		_scales = inScales;
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
