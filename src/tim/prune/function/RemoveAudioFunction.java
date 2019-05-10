package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.AudioClip;
import tim.prune.undo.UndoDeleteAudio;

/**
 * Function to remove the currently selected audio clip
 */
public class RemoveAudioFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public RemoveAudioFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.removeaudio";
	}

	/**
	 * Perform the function
	 */
	public void begin()
	{
		// Delete the current audio, and optionally its point too, keeping undo information
		AudioClip currentAudio = _app.getTrackInfo().getCurrentAudio();
		if (currentAudio != null)
		{
			// Audio is selected, see if it has a point or not
			boolean deleted = false;
			UndoDeleteAudio undoAction = null;
			if (currentAudio.getDataPoint() == null)
			{
				// no point attached, so just delete
				undoAction = new UndoDeleteAudio(currentAudio, _app.getTrackInfo().getSelection().getCurrentAudioIndex(),
					null, -1);
				deleted = _app.getTrackInfo().deleteCurrentAudio(false);
			}
			else
			{
				// point is attached, so need to confirm point deletion
				final int pointIndex = _app.getTrackInfo().getTrack().getPointIndex(currentAudio.getDataPoint());
				undoAction = new UndoDeleteAudio(currentAudio, _app.getTrackInfo().getSelection().getCurrentAudioIndex(),
					currentAudio.getDataPoint(), pointIndex);
				undoAction.setAtBoundaryOfSelectedRange(pointIndex == _app.getTrackInfo().getSelection().getStart() ||
					pointIndex == _app.getTrackInfo().getSelection().getEnd());
				int response = JOptionPane.showConfirmDialog(_app.getFrame(),
					I18nManager.getText("dialog.deleteaudio.deletepoint"),
					I18nManager.getText(getNameKey()), JOptionPane.YES_NO_CANCEL_OPTION);
				boolean deletePointToo = (response == JOptionPane.YES_OPTION);
				// Cancel delete if cancel pressed or dialog closed
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.NO_OPTION) {
					deleted = _app.getTrackInfo().deleteCurrentAudio(deletePointToo);
				}
			}
			// Add undo information to stack if necessary
			if (deleted) {
				_app.completeFunction(undoAction, currentAudio.getName() + " " + I18nManager.getText("confirm.media.removed"));
			}
		}
	}
}
