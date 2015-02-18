package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;

/**
 * Function to set the width with which lines are drawn
 */
public class SetLineWidth extends SingleNumericParameterFunction
{

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SetLineWidth(App inApp) {
		super(inApp, 1, 4);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.setlinewidth";
	}

	/** @return description key */
	public String getDescriptionKey() {
		return "dialog.setlinewidth.text";
	}

	/** @return the current value to display */
	public int getCurrentParamValue() {
		return Config.getConfigInt(Config.KEY_LINE_WIDTH);
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
	 * Complete the function using the given line width parameter
	 */
	public void completeFunction(int inLineWidth)
	{
		final int currLineWidth = Config.getConfigInt(Config.KEY_LINE_WIDTH);
		if (inLineWidth >= 1 && inLineWidth <= 4 && inLineWidth != currLineWidth)
		{
			Config.setConfigInt(Config.KEY_LINE_WIDTH, inLineWidth);
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		}
	}
}
