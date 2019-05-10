package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Photo;
import tim.prune.undo.UndoDeletePhoto;

/**
 * Function to remove the currently selected photo
 */
public class RemovePhotoFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public RemovePhotoFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.removephoto";
	}

	/**
	 * Perform the function
	 */
	public void begin()
	{
		// Delete the current photo, and optionally its point too, keeping undo information
		Photo currentPhoto = _app.getTrackInfo().getCurrentPhoto();
		if (currentPhoto != null)
		{
			// Photo is selected, see if it has a point or not
			boolean photoDeleted = false;
			UndoDeletePhoto undoAction = null;
			if (currentPhoto.getDataPoint() == null)
			{
				// no point attached, so just delete photo
				undoAction = new UndoDeletePhoto(currentPhoto, _app.getTrackInfo().getSelection().getCurrentPhotoIndex(),
					null, -1);
				photoDeleted = _app.getTrackInfo().deleteCurrentPhoto(false);
			}
			else
			{
				// point is attached, so need to confirm point deletion
				final int pointIndex = _app.getTrackInfo().getTrack().getPointIndex(currentPhoto.getDataPoint());
				undoAction = new UndoDeletePhoto(currentPhoto, _app.getTrackInfo().getSelection().getCurrentPhotoIndex(),
					currentPhoto.getDataPoint(), pointIndex);
				undoAction.setAtBoundaryOfSelectedRange(pointIndex == _app.getTrackInfo().getSelection().getStart() ||
					pointIndex == _app.getTrackInfo().getSelection().getEnd());
				int response = JOptionPane.showConfirmDialog(_app.getFrame(),
					I18nManager.getText("dialog.deletephoto.deletepoint"),
					I18nManager.getText("dialog.deletephoto.title"),
					JOptionPane.YES_NO_CANCEL_OPTION);
				boolean deletePointToo = (response == JOptionPane.YES_OPTION);
				// Cancel delete if cancel pressed or dialog closed
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.NO_OPTION) {
					photoDeleted = _app.getTrackInfo().deleteCurrentPhoto(deletePointToo);
				}
			}
			// Add undo information to stack if necessary
			if (photoDeleted) {
				_app.completeFunction(undoAction, currentPhoto.getName() + " " + I18nManager.getText("confirm.media.removed"));
			}
		}
	}
}
