package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;
import tim.prune.undo.UndoDeleteRange;


/**
 * Abstract class to hold general deletion routines to delete
 * either one or two track sections.  Forms parent class to
 * the DeleteSelection and CropToSelection functions
 */
public abstract class DeleteBitOfTrackFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DeleteBitOfTrackFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * @return key of undo text
	 */
	protected abstract String getUndoNameKey();

	/**
	 * Delete a single section
	 * @param inStart1 start index of section
	 * @param inEnd1 end index of section
	 */
	protected void deleteSection(int inStart1, int inEnd1)
	{
		deleteTwoSections(inStart1, inEnd1, -1, -1);
	}

	/**
	 * Delete the two specified sections
	 * @param inStart1 start index of first section to delete
	 * @param inEnd1 end index of first section
	 * @param inStart2 start index of second section to delete
	 * @param inEnd2 end index of second section
	 */
	protected void deleteTwoSections(int inStart1, int inEnd1, int inStart2, int inEnd2)
	{
		boolean[] deleteAllOrNone = {false, false};
		// TODO: Check for range overlap?  And test!
		if (inStart1 < 0 || inEnd1 < 0 || inEnd1 < inStart1) {
			inStart1 = inEnd1 = -1;
		}
		if (inStart2 < 0 || inEnd2 < 0 || inEnd2 < inStart2) {
			inStart2 = inEnd2 = -1;
		}
		if ((inStart1 >= 0 && inEnd1 < inStart1)
			|| (inStart2 >= 0 && (inStart2 < inEnd1 || inEnd2 <= inStart2)))
		{
			System.err.println("Invalid ranges: (" + inStart1 + " - " + inEnd1 + "), (" + inStart2 + " - " + inEnd2 + ")");
			return;
		}
		// First section (if any)
		int numPoints = inEnd1 - inStart1 + 1;
		boolean[] deleteMedia1 = new boolean[numPoints];
		int numDeleted1 = prepareDeleteMedia(inStart1, inEnd1, deleteAllOrNone, deleteMedia1);
		if (numDeleted1 < 0) return;

		// Second section (if any)
		numPoints = inEnd2 - inStart2 + 1;
		boolean[] deleteMedia2 = new boolean[numPoints];
		int numDeleted2 = prepareDeleteMedia(inStart2, inEnd2, deleteAllOrNone, deleteMedia2);
		if (numDeleted2 < 0) return;
		int numDeleted = numDeleted1 + numDeleted2;
		if (numDeleted <= 0) return;

		// create undo object
		UndoDeleteRange undo = new UndoDeleteRange(_app.getTrackInfo(), getUndoNameKey(),
			inStart1, deleteMedia1, inStart2, deleteMedia2);

		// Loop through media to remove or disconnect
		if (numDeleted1 > 0) {
			resolveMedia(_app.getTrackInfo(), inStart1, deleteMedia1);
		}
		if (numDeleted2 > 0) {
			resolveMedia(_app.getTrackInfo(), inStart2, deleteMedia2);
		}

		// Call track to delete ranges 1 and 2
		if (numDeleted2 > 0) { // delete range2 first
			_app.getTrackInfo().getTrack().deleteRange(inStart2, inEnd2);
		}
		if (numDeleted1 > 0) { // delete range1 first
			_app.getTrackInfo().getTrack().deleteRange(inStart1, inEnd1);
		}

		// clear selection and notify
		_app.getTrackInfo().getSelection().clearAll();
		UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_ADDED_OR_REMOVED);

		// pass back to _app
		_app.completeFunction(undo, "" + numDeleted + " "
			+ I18nManager.getText("confirm.deletepoint.multi"));
	}

	/**
	 * Prepare to delete the media in the given section, including prompting to delete or not
	 * @param inStart start index of the range to delete
	 * @param inEnd end index
	 * @param inDeleteAllOrNone boolean flags for delete all and delete none, held in an array
	 * @param inDeleteMedia boolean flag for each point, whether to delete media or not
	 * @return number of points to delete
	 */
	private int prepareDeleteMedia(int inStart, int inEnd, boolean[] inDeleteAllOrNone, boolean[] inDeleteMedia)
	{
		// Check sanity of inputs
		if (inStart < 0 || inEnd < 0 || inEnd < inStart) return 0;
		final int numPoints = inEnd - inStart + 1;
		if (inDeleteAllOrNone == null || inDeleteAllOrNone.length != 2
			|| inDeleteMedia == null || inDeleteMedia.length != numPoints) {
			return 0;
		}

		// define buttons on prompt
		String[] questionOptions = {I18nManager.getText("button.yes"), I18nManager.getText("button.no"),
			I18nManager.getText("button.yestoall"), I18nManager.getText("button.notoall"),
			I18nManager.getText("button.cancel")};

		// Loop over points to check for media
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(inStart + i);
			if (point.hasMedia())
			{
				// point has either photo or audio
				if (inDeleteAllOrNone[0]) // delete all has already been selected
				{
					inDeleteMedia[i] = true;
				}
				else if (inDeleteAllOrNone[1]) // delete none has already been selected
				{
					inDeleteMedia[i] = false;
				}
				else
				{
					int response = JOptionPane.showOptionDialog(_app.getFrame(),
						I18nManager.getText("dialog.deletepoint.deletephoto") + " " + point.getMediaName(),
						I18nManager.getText("dialog.deletepoint.title"),
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						questionOptions, questionOptions[1]);
					// check for cancel or close
					if (response == 4 || response == -1) {return -1;}
					// check for yes or yes to all
					if (response == 0 || response == 2)
					{
						inDeleteMedia[i] = true;
						if (response == 2) {inDeleteAllOrNone[0] = true;}
					}
					// check for no to all
					if (response == 3) {inDeleteAllOrNone[1] = true;}
				}
			}
		}
		return numPoints;
	}

	/**
	 * Resolve the media from the given points by either detaching or deleting
	 * @param inTrack track object
	 * @param inStart start index of range
	 * @param inDeleteFlags media deletion flags
	 */
	private static void resolveMedia(TrackInfo inTrackInfo, int inStart, boolean[] inDeleteFlags)
	{
		for (int i=0; i<inDeleteFlags.length; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(i + inStart);
			if (point != null && point.hasMedia())
			{
				if (inDeleteFlags[i])
				{
					// delete photo and/or audio from lists
					if (point.getPhoto() != null) {
						inTrackInfo.getPhotoList().deletePhoto(inTrackInfo.getPhotoList().getPhotoIndex(point.getPhoto()));
					}
					if (point.getAudio() != null) {
						inTrackInfo.getAudioList().deleteAudio(inTrackInfo.getAudioList().getAudioIndex(point.getAudio()));
					}
				}
				else
				{
					// decouple photo and/or audio from point
					if (point.getPhoto() != null) {
						point.getPhoto().setDataPoint(null);
					}
					if (point.getAudio() != null) {
						point.getAudio().setDataPoint(null);
					}
				}
			}
		}
	}
}
