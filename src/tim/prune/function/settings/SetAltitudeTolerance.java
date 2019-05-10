package tim.prune.function.settings;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Unit;
import tim.prune.function.SingleNumericParameterFunction;

/**
 * Function to set the tolerance for the altitude range calculations
 */
public class SetAltitudeTolerance extends SingleNumericParameterFunction
{

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SetAltitudeTolerance(App inApp) {
		super(inApp, 0, 100);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.setaltitudetolerance";
	}

	/**
	 * @return description key
	 */
	public String getDescriptionKey()
	{
		// Two different keys for feet and metres
		final boolean isMetres = Config.getUnitSet().getAltitudeUnit().isStandard();
		return "dialog.setaltitudetolerance.text." + (isMetres ? "metres" : "feet");
	}

	/**
	 * @return the current value to display
	 */
	public int getCurrentParamValue()
	{
		int configVal = Config.getConfigInt(Config.KEY_ALTITUDE_TOLERANCE);
		// Convert this to feet if necessary
		Unit altUnit = Config.getUnitSet().getAltitudeUnit();
		if (altUnit.isStandard()) {
			return configVal / 100;
		}
		return (int) (configVal * altUnit.getMultFactorFromStd() / 100.0);
	}

	/**
	 * Run function
	 */
	public void begin()
	{
		// Not required, because this function is started from a ChooseSingleParameter function
		// and goes directly to the completeFunction method.
	}

	/**
	 * Complete the function using the given tolerance parameter
	 */
	public void completeFunction(int inTolerance)
	{
		// Convert back from feet into metres again
		Unit altUnit = Config.getUnitSet().getAltitudeUnit();
		int configVal = inTolerance * 100;
		if (!altUnit.isStandard()) {
			configVal = (int) (inTolerance * 100.0 / altUnit.getMultFactorFromStd());
		}
		Config.setConfigInt(Config.KEY_ALTITUDE_TOLERANCE, configVal);
		UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
	}
}
