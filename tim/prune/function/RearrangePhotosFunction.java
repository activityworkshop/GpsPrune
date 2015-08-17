package tim.prune.function;

import java.util.Arrays;
import javax.swing.JOptionPane;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.sort.PhotoComparer;
import tim.prune.data.sort.SortMode;
import tim.prune.undo.UndoRearrangePhotos;

/**
 * Class to provide the function for rearranging photo points
 */
public class RearrangePhotosFunction extends RearrangeFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangePhotosFunction(App inApp)
	{
		super(inApp, false);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.rearrangephotos";
	}

	/** Get the description key */
	public String getDescriptionKey() {
		return "dialog.rearrangephotos.desc";
	}

	/** Sort by filename key */
	protected String getSortNameKey() {
		return "sortbyfilename";
	}

	/**
	 * Perform the rearrange
	 */
	protected void finish()
	{
		Track track = _app.getTrackInfo().getTrack();
		UndoRearrangePhotos undo = new UndoRearrangePhotos(track);
		// Loop through track collecting non-photo points and photo points
		final int numPoints = track.getNumPoints();
		DataPoint[] nonPhotos = new DataPoint[numPoints];
		DataPoint[] photos = new DataPoint[numPoints];
		int numNonPhotos = 0;
		int numPhotos = 0;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.getPhoto() != null)
			{
				photos[numPhotos] = point;
				numPhotos++;
			}
			else
			{
				nonPhotos[numNonPhotos] = point;
				numNonPhotos++;
			}
		}
		boolean pointsChanged = false;
		if (numPhotos > 0)
		{
			Rearrange rearrangeOption = getRearrangeOption();
			SortMode sortOption = getSortMode();
			// Sort photos if necessary
			if (sortOption != SortMode.DONT_SORT && numPhotos > 1) {
				sortPhotos(photos, sortOption);
			}
			// Put the non-photo points and photo points together
			DataPoint[] neworder = new DataPoint[numPoints];
			if (rearrangeOption == Rearrange.TO_START)
			{
				// photos at front
				System.arraycopy(photos, 0, neworder, 0, numPhotos);
				System.arraycopy(nonPhotos, 0, neworder, numPhotos, numNonPhotos);
			}
			else
			{
				// photos at end
				System.arraycopy(nonPhotos, 0, neworder, 0, numNonPhotos);
				System.arraycopy(photos, 0, neworder, numNonPhotos, numPhotos);
			}

			// Give track the new point order
			pointsChanged = track.replaceContents(neworder);
		}
		// did anything change?
		if (pointsChanged)
		{
			_app.getTrackInfo().getSelection().clearAll();
			_app.completeFunction(undo, I18nManager.getText("confirm.rearrangephotos"));
			// Note: subscribers are informed up to three times now
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Sort the given photo list either by filename or by time
	 * @param inPhotos array of DataPoint objects to sort
	 * @param inSortOrder sort order
	 * @return sorted array
	 */
	private static void sortPhotos(DataPoint[] inPhotos, SortMode inSortMode)
	{
		PhotoComparer comparer = new PhotoComparer(inSortMode);
		Arrays.sort(inPhotos, comparer);
	}
}
