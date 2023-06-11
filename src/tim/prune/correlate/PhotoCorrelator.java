package tim.prune.correlate;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CorrelateMediaCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.Photo;
import tim.prune.data.TimeDifference;


/**
 * Class to manage the automatic correlation of photos to points
 * including the GUI stuff to control the correlation options
 */
public class PhotoCorrelator extends Correlator
{
	/**
	 * Constructor
	 * @param inApp App object to report actions to
	 */
	public PhotoCorrelator(App inApp) {
		super(inApp);
	}


	/** @return the name key */
	public String getNameKey() {
		return "function.correlatephotos";
	}

	/** @return type key */
	protected String getMediaTypeKey() {
		return "photo";
	}

	/** @return photo list*/
	protected MediaList<?> getMediaList() {
		return _app.getTrackInfo().getPhotoList();
	}

	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inTimeDiff TimeDifference to use for preview
	 * @param inShowWarning true to show warning if all points out of range
	 */
	protected void createPreview(TimeDifference inTimeDiff, boolean inShowWarning)
	{
		TimeDifference timeLimit = parseTimeLimit();
		double angDistLimit = parseDistanceLimit();
		MediaPreviewTableModel model = new MediaPreviewTableModel("dialog.correlate.select.photoname");
		MediaList<Photo> photos = _app.getTrackInfo().getPhotoList();
		// Loop through photos deciding whether to set correlate flag or not
		int numPhotos = photos.getCount();
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = photos.get(i);
			PointMediaPair pair = getPointPairForMedia(_app.getTrackInfo().getTrack(), photo, inTimeDiff);
			MediaPreviewTableRow row = new MediaPreviewTableRow(pair);
			// Don't try to correlate photos which don't have points either side
			boolean correlatePhoto = pair.isValid();
			// Don't select photos which already have a point
			if (photo.getCurrentStatus() != Photo.Status.NOT_CONNECTED) {correlatePhoto = false;}
			// Check time limits, distance limits
			if (timeLimit != null && correlatePhoto) {
				long numSecs = pair.getMinSeconds();
				correlatePhoto = (numSecs <= timeLimit.getTotalSeconds());
			}
			if (angDistLimit > 0.0 && correlatePhoto)
			{
				final double angDistPair = DataPoint.calculateRadiansBetween(pair.getPointBefore(), pair.getPointAfter());
				double frac = pair.getFraction();
				if (frac > 0.5) {frac = 1 - frac;}
				final double angDistPhoto = angDistPair * frac;
				correlatePhoto = (angDistPhoto < angDistLimit);
			}
			// Don't select photos which are already correlated to the same point
			if (pair.getSecondsBefore() == 0L && pair.getPointBefore().isDuplicate(photo.getDataPoint())) {
				correlatePhoto = false;
			}
			row.setCorrelateFlag(correlatePhoto);
			model.addRow(row);
		}
		_previewTable.setModel(model);
		// Set distance units
		model.setDistanceUnits(getSelectedDistanceUnits());
		// Set column widths
		_previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		final int[] colWidths = {150, 160, 100, 100, 50};
		for (int i=0; i<model.getColumnCount(); i++) {
			_previewTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
		}
		// check if any photos found
		_okButton.setEnabled(model.hasAnySelected());
		if (inShowWarning && !model.hasAnySelected())
		{
			JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.correlate.alloutsiderange"),
				getName(), JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * Finish the correlation by creating the appropriate command
	 * and passing it back to the App
	 */
	protected void finishCorrelation()
	{
		PointMediaPair[] pointPairs = getPointPairs();
		if (pointPairs == null || pointPairs.length <= 0) {
			return;
		}

		ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
		ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
		for (PointMediaPair pair : pointPairs)
		{
			if (pair != null && pair.isValid())
			{
				Photo photoToLink = (Photo) pair.getMedia();
				if (pair.getMinSeconds() == 0L)
				{
					// exact match
					Photo pointPhoto = pair.getPointBefore().getPhoto();
					if (pointPhoto == null)
					{
						// photo coincides with photoless point so connect the two
						DataPoint point = pair.getPointBefore();
						pointPhotoPairs.add(new PointAndMedia(point, photoToLink, null));
					}
					else if (pointPhoto.equals(pair.getMedia())) {
						// photo is already connected, nothing to do
					}
					else
					{
						// point is already connected to a different photo, so need to clone point
						DataPoint pointToAdd = pair.getPointBefore().clonePoint();
						pointsToCreate.add(pointToAdd);
						pointPhotoPairs.add(new PointAndMedia(pointToAdd, photoToLink, null));
					}
				}
				else
				{
					// photo time falls between two points, so need to interpolate new one
					DataPoint pointToAdd = DataPoint.interpolate(pair.getPointBefore(), pair.getPointAfter(), pair.getFraction());
					pointToAdd.setSegmentStart(true);
					pointsToCreate.add(pointToAdd);
					pointPhotoPairs.add(new PointAndMedia(pointToAdd, photoToLink, null));
				}
			}
		}

		Command command = new CorrelateMediaCmd(MediaLinkType.LINK_PHOTOS, pointsToCreate, pointPhotoPairs);
		command.setDescription(makeUndoText(pointPhotoPairs.size()));
		command.setConfirmText(makeConfirmText(pointPhotoPairs.size()));
		_app.execute(command);
	}
}
