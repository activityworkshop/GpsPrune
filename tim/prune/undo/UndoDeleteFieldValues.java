package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the deletion of field values
 */
public class UndoDeleteFieldValues implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** Field to be deleted */
	private Field _field = null;
	/** Field values before operation */
	private String[] _fieldValues = null;

	/**
	 * Constructor
	 * @param inTrackInfo track info object to copy values from
	 * @param inField field to delete
	 */
	public UndoDeleteFieldValues(TrackInfo inTrackInfo, Field inField)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		_endIndex = inTrackInfo.getSelection().getEnd();
		final int numPoints = _endIndex - _startIndex + 1;
		_fieldValues = new String[numPoints];
		_field = inField;
		// Loop over points in selection, and copy field values
		for (int i=_startIndex; i<=_endIndex; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(i);
			_fieldValues[i-_startIndex] = point.getFieldValue(inField);
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.deletefieldvalues");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Sanity check
		Track track = inTrackInfo.getTrack();
		if (track.getNumPoints() <= _endIndex || _endIndex <= _startIndex) {
			throw new UndoException("Cannot undo conversion, track length doesn't match");
		}
		// Loop over points in selection and replace field values
		for (int i=_startIndex; i<=_endIndex; i++)
		{
			String storedValue = _fieldValues[i-_startIndex];
			if (storedValue != null) {
				track.getPoint(i).setFieldValue(_field, storedValue, true);
			}
		}
		track.requestRescale();
		inTrackInfo.getSelection().markInvalid();
		UpdateMessageBroker.informSubscribers();
	}
}
