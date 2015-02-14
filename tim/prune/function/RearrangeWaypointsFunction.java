package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Track;
import tim.prune.undo.UndoRearrangeWaypoints;

/**
 * Class to provide the function for rearranging waypoints
 */
public class RearrangeWaypointsFunction extends GenericFunction
{

	/** Enumeration for rearrange commands */
	public enum Rearrange
	{
		/** Rearrange all waypoints to start */
		TO_START,
		/** Rearrange all waypoints to end */
		TO_END,
		/** Rearrange each waypoint to nearest track point */
		TO_NEAREST
	}

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangeWaypointsFunction(App inApp)
	{
		super(inApp);
	}

	/** Begin the rearrange (not needed) */
	public void begin() {
	}

	/** Get the name key (not needed) */
	public String getNameKey() {
		return null;
	}

	/**
	 * Rearrange the waypoints into track order
	 * @param inFunction nearest point, all to end or all to start
	 */
	public void rearrangeWaypoints(Rearrange inFunction)
	{
		Track track = _app.getTrackInfo().getTrack();
		UndoRearrangeWaypoints undo = new UndoRearrangeWaypoints(track);
		boolean success = false;
		if (inFunction == Rearrange.TO_START || inFunction == Rearrange.TO_END)
		{
			// Collect the waypoints to the start or end of the track
			success = track.collectWaypoints(inFunction == Rearrange.TO_START);
		}
		else
		{
			// Interleave the waypoints into track order
			success = track.interleaveWaypoints();
		}
		if (success)
		{
			_app.completeFunction(undo, I18nManager.getText("confirm.rearrangewaypoints"));
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}

}
