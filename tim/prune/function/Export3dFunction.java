package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;

/**
 * Abstract superclass for pov and svg export functions
 */
public abstract class Export3dFunction extends GenericFunction
{
	/** altitude exaggeration factor */
	protected double _altFactor = 50.0;

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
}
