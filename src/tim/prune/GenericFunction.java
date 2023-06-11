package tim.prune;

import javax.swing.JFrame;

/**
 * Generic function class for launching from the app
 */
public abstract class GenericFunction
{
	/** Reference to app object */
	protected final App _app;
	/** Reference to parent frame */
	protected final JFrame _parentFrame;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GenericFunction(App inApp)
	{
		_app = inApp;
		_parentFrame = inApp.getFrame();
	}

	/**
	 * Begin the function
	 */
	public abstract void begin();

	/**
	 * @return the function name
	 */
	public String getName() {
		return I18nManager.getText(getNameKey());
	}

	/**
	 * @return the key for the function name
	 */
	public abstract String getNameKey();
}
