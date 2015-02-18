package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;

/**
 * Abstract superclass of Functions which just take a
 * single numeric parameter
 */
public abstract class SingleNumericParameterFunction extends GenericFunction
{
	/** Minimum and maximum allowed values */
	protected int _minAllowedValue, _maxAllowedValue;

	/** Constructor */
	public SingleNumericParameterFunction(App inApp, int inMinValue, int inMaxValue)
	{
		super(inApp);
		_minAllowedValue = inMinValue;
		_maxAllowedValue = inMaxValue;
	}

	/** Get the current value for display in the dialog */
	public abstract int getCurrentParamValue();

	/** Get the key for the description label */
	public abstract String getDescriptionKey();

	/** Callback to trigger the rest of the function once the parameter has been chosen */
	public abstract void completeFunction(int inParam);

	/** @return minimum allowed value */
	public int getMinAllowedValue() {return _minAllowedValue;}
	/** @return maximum allowed value */
	public int getMaxAllowedValue() {return _maxAllowedValue;}
}
