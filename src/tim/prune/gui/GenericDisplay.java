package tim.prune.gui;

import javax.swing.JPanel;

import tim.prune.DataSubscriber;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Superclass of all display components
 */
public abstract class GenericDisplay extends JPanel implements DataSubscriber
{
	protected TrackInfo _trackInfo = null;
	protected Track _track = null;

	/**
	 * Constructor
	 * @param inTrackInfo trackInfo object
	 */
	public GenericDisplay(TrackInfo inTrackInfo)
	{
		_trackInfo = inTrackInfo;
		_track = _trackInfo.getTrack();
	}

	/**
	 * Ignore action completed signals
	 */
	public void actionCompleted(String inMessage)
	{
	}
}
