package tim.prune.threedee;

import tim.prune.data.Track;

/**
 * Interface to decouple from Java3D classes
 */
public interface ThreeDWindow
{

	/**
	 * Set the Track data
	 * @param inTrack Track object
	 */
	public void setTrack(Track inTrack);


	/**
	 * Show the window
	 */
	public void show() throws ThreeDException;
}
