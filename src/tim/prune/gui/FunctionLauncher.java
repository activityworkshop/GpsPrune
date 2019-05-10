package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import tim.prune.GenericFunction;

/**
 * Class to launch a function triggered by an action
 */
public class FunctionLauncher implements ActionListener
{
	/** Function to launch */
	private GenericFunction _function = null;

	/**
	 * Constructor
	 * @param inFunction function to launch
	 */
	public FunctionLauncher(GenericFunction inFunction)
	{
		_function = inFunction;
	}

	/**
	 * React to action
	 * @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		_function.begin();
	}

}