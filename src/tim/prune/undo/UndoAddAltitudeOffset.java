package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Undo addition/subtraction of an altitude offset
 */
public class UndoAddAltitudeOffset implements UndoOperation
{
	/** Start index of section */
	private int _startIndex;
	/** altitude values before operation */
	private Altitude[] _altitudes;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoAddAltitudeOffset(TrackInfo inTrackInfo)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		final int endIndex = inTrackInfo.getSelection().getEnd();
		final int numPoints = endIndex - _startIndex + 1;
		// Make array of cloned altitude objects
		_altitudes = new Altitude[numPoints];
		for (int i=0; i<numPoints; i++) {
			Altitude a = inTrackInfo.getTrack().getPoint(_startIndex+i).getAltitude();
			if (a != null && a.isValid()) {
				_altitudes[i] = a.clone();
			}
		}
	}


	/**
	 * @return description of operation including number of points adjusted
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.addaltitudeoffset") + " (" + (_altitudes.length) + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Perform the inverse operation
		final int numPoints = _altitudes.length;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(i+_startIndex);
			point.resetAltitude(_altitudes[i]);
		}
		_altitudes = null;
		inTrackInfo.getSelection().markInvalid();
		UpdateMessageBroker.informSubscribers();
	}
}
