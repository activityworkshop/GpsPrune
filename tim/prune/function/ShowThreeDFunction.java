package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.threedee.ThreeDException;
import tim.prune.threedee.ThreeDWindow;
import tim.prune.threedee.WindowFactory;

/**
 * Class to show the 3d window
 */
public class ShowThreeDFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public ShowThreeDFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.show3d";
	}

	/**
	 * Show the help screen
	 */
	public void begin()
	{
		ThreeDWindow window = WindowFactory.getWindow(_parentFrame);
		if (window == null)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.function.nojava3d"),
				I18nManager.getText("error.function.notavailable.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			try
			{
				// Pass the track object and show the window
				window.setTrack(_app.getTrackInfo().getTrack());
				window.show();
			}
			catch (ThreeDException e)
			{
				_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getText("error.3d") + ": " + e.getMessage());
			}
		}
	}
}
