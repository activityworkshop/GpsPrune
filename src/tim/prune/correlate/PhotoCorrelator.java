package tim.prune.correlate;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.TimeDifference;
import tim.prune.undo.UndoCorrelatePhotos;

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


	/** Get the name key */
	public String getNameKey() {
		return "function.correlatephotos";
	}

	/** @return type key */
	protected String getMediaTypeKey() {
		return "photo";
	}

	/** @return photo list*/
	protected MediaList getMediaList() {
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
		PhotoList photos = _app.getTrackInfo().getPhotoList();
		// Loop through photos deciding whether to set correlate flag or not
		int numPhotos = photos.getNumPhotos();
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = photos.getPhoto(i);
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
				I18nManager.getText(getNameKey()), JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * Finish the correlation by modifying the track
	 * and passing the Undo information back to the App
	 */
	protected void finishCorrelation()
	{
		PointMediaPair[] pointPairs = getPointPairs();
		if (pointPairs == null || pointPairs.length <= 0) {return;}

		// begin to construct undo information
		UndoCorrelatePhotos undo = new UndoCorrelatePhotos(_app.getTrackInfo());
		// loop over Photos
		int arraySize = pointPairs.length;
		int i = 0, numPhotos = 0;
		int numPointsToCreate = 0;
		PointMediaPair pair = null;
		for (i=0; i<arraySize; i++)
		{
			pair = pointPairs[i];
			if (pair != null && pair.isValid())
			{
				if (pair.getMinSeconds() == 0L)
				{
					// exact match
					Photo pointPhoto = pair.getPointBefore().getPhoto();
					if (pointPhoto == null)
					{
						// photo coincides with photoless point so connect the two
						pair.getPointBefore().setPhoto((Photo) pair.getMedia());
						pair.getMedia().setDataPoint(pair.getPointBefore());
					}
					else if (pointPhoto.equals(pair.getMedia())) {
						// photo is already connected, nothing to do
					}
					else {
						// point is already connected to a different photo, so need to clone point
						numPointsToCreate++;
					}
				}
				else
				{
					// photo time falls between two points, so need to interpolate new one
					numPointsToCreate++;
				}
				numPhotos++;
			}
		}
		// Second loop, to create points if necessary
		if (numPointsToCreate > 0)
		{
			// make new array for added points
			DataPoint[] addedPoints = new DataPoint[numPointsToCreate];
			int pointNum = 0;
			DataPoint pointToAdd = null;
			for (i=0; i<arraySize; i++)
			{
				pair = pointPairs[i];
				if (pair != null && pair.isValid())
				{
					pointToAdd = null;
					if (pair.getMinSeconds() == 0L && pair.getPointBefore().getPhoto() != null
					 && !pair.getPointBefore().getPhoto().equals(pair.getMedia()))
					{
						// clone point
						pointToAdd = pair.getPointBefore().clonePoint();
					}
					else if (pair.getMinSeconds() > 0L)
					{
						// interpolate point
						pointToAdd = DataPoint.interpolate(pair.getPointBefore(), pair.getPointAfter(), pair.getFraction());
					}
					if (pointToAdd != null)
					{
						// link photo to point
						pointToAdd.setPhoto((Photo) pair.getMedia());
						pair.getMedia().setDataPoint(pointToAdd);
						// set to start of segment so not joined in track
						pointToAdd.setSegmentStart(true);
						// add to point array
						addedPoints[pointNum] = pointToAdd;
						pointNum++;
					}
				}
			}
			// expand track
			_app.getTrackInfo().getTrack().appendPoints(addedPoints);
		}

		// send undo information back to controlling app
		undo.setNumPhotosCorrelated(numPhotos);
		_app.completeFunction(undo, ("" + numPhotos + " "
			 + (numPhotos==1?I18nManager.getText("confirm.correlatephotos.single"):I18nManager.getText("confirm.correlatephotos.multi"))));
		// observers already informed by track update if new points created
		if (numPointsToCreate == 0) {
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		}
	}
}
