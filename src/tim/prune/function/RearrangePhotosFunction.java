package tim.prune.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.PointReference;
import tim.prune.cmd.RearrangePointsCmd;
import tim.prune.cmd.SetSegmentsCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.SortMode;
import tim.prune.data.Track;


/**
 * Class to provide the function for rearranging photo points
 */
public class RearrangePhotosFunction extends RearrangeFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangePhotosFunction(App inApp) {
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
		Rearrange rearrangeOption = getRearrangeOption();
		SortMode sortOption = getSortMode();

		// Collect the waypoints to the start or end of the track
		final List<PointReference> result = collectPhotoPoints(rearrangeOption, sortOption);

		if (isResultANop(result))
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			// Add all the photo points to the command
			SetSegmentsCmd segmentCommand = new SetSegmentsCmd();
			Track track = _app.getTrackInfo().getTrack();
			final int numPoints = track.getNumPoints();
			for (int i=0; i<numPoints; i++)
			{
				DataPoint point = track.getPoint(i);
				if (point.getPhoto() != null) {
					segmentCommand.addSegmentFlag(point);
				}
			}
			CompoundCommand command = new CompoundCommand()
				.addCommand(RearrangePointsCmd.from(result))
				.addCommand(segmentCommand);
			command.setDescription(getName());
			command.setConfirmText("confirm.rearrangephotos");
			_app.execute(command);
		}
	}


	/**
	 * Do the collection and sorting of the points
	 * @param inRearrangeOption beginning or end
	 * @param inSortOption optional sort criterion
	 * @return list of point references
	 */
	private List<PointReference> collectPhotoPoints(Rearrange inRearrangeOption, SortMode inSortOption)
	{
		Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		ArrayList<PointReference> photoPoints = new ArrayList<>();
		ArrayList<PointReference> nonPhotoPoints = new ArrayList<>();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.getPhoto() != null)
			{
				PointReference pointReference = new PointReference(i,
					inSortOption == SortMode.SORTBY_NAME ? point.getPhoto().getName() : null,
					inSortOption == SortMode.SORTBY_TIME ? point.getPhoto().getTimestamp() : null);
				photoPoints.add(pointReference);
			}
			else {
				nonPhotoPoints.add(new PointReference(i, null, null));
			}
		}

		Collections.sort(photoPoints);

		// Combine the two lists into a single one
		List<PointReference> result = new ArrayList<>();
		if (inRearrangeOption == Rearrange.TO_START)
		{
			result.addAll(photoPoints);
			result.addAll(nonPhotoPoints);
		}
		else
		{
			result.addAll(nonPhotoPoints);
			result.addAll(photoPoints);
		}
		return result;
	}
}
