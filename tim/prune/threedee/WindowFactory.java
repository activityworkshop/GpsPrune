package tim.prune.threedee;

import javax.swing.JFrame;

import tim.prune.App;

/**
 * Factory class for getting a Window
 */
public abstract class WindowFactory
{
	private static Java3DWindow _window = null;

	/**
	 * Get a Window object
	 * @param inApp App object
	 * @param inFrame parent frame
	 * @return object if available, otherwise null
	 */
	public static ThreeDWindow getWindow(App inApp, JFrame inFrame)
	{
		if (isJava3dEnabled())
		{
			if (_window == null) {
				_window = new Java3DWindow(inApp, inFrame);
			}
			else {
				_window.dispose();
			}
			return _window;
		}
		return null;
	}


	/**
	 * @return true if 3d capability is installed
	 */
	public static boolean isJava3dEnabled()
	{
		boolean has3d = false;
		try
		{
			Class universeClass = Class.forName("com.sun.j3d.utils.universe.SimpleUniverse");
			has3d = (universeClass != null);
		}
		catch (ClassNotFoundException e)
		{
			// no java3d classes available
		}
		return has3d;
	}

}
