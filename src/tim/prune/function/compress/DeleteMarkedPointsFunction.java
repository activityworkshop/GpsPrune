package tim.prune.function.compress;

import java.util.ArrayList;
import java.util.List;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.cmd.Command;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;


/**
 * Function to delete the marked points in the track
 */
public class DeleteMarkedPointsFunction extends GenericFunction
{
	private boolean _splitSegments = false;
	private String  _parentFunctionKey = null;

	/** Constructor */
	public DeleteMarkedPointsFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.deletemarked";
	}

	/**
	 * Get notification about parent function
	 * @param inKey parent function name key
	 * @param inSplitSegments true to split segment, false to not
	 */
	public void setParentFunction(String inKey, boolean inSplitSegments)
	{
		_parentFunctionKey = inKey;
		_splitSegments = inSplitSegments;
	}

	@Override
	public void begin()
	{
		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		Track track = _app.getTrackInfo().getTrack();
		for (int i=0; i<track.getNumPoints(); i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.getDeleteFlag()) {
				indexesToDelete.add(i);
			}
			else {
				indexesToKeep.add(i);
			}
		}

		if (indexesToDelete.isEmpty())
		{
			String titleKey = (_parentFunctionKey == null ? getNameKey() : _parentFunctionKey);
			_app.showErrorMessage(titleKey, "dialog.deletemarked.nonefound");
		}
		else
		{
			Command command = new ShuffleAndCropCmd(indexesToKeep, indexesToDelete,
				makeSegmentFlags(track, indexesToDelete));
			_app.execute(command);
		}
	}

	private List<PointFlag> makeSegmentFlags(Track inTrack, ArrayList<Integer> inIndexesToDelete)
	{
		ArrayList<PointFlag> flags = new ArrayList<>();
		DataPoint prevPoint = null;
		for (int i : inIndexesToDelete)
		{
			if (inTrack.getPoint(i).getSegmentStart() || _splitSegments)
			{
				DataPoint nextPoint = inTrack.getNextTrackPoint(i+1);
				if (nextPoint == null || nextPoint == prevPoint) {
					continue;
				}
				// Check it's not one of the indexes to delete
				boolean willBeDeleted = false;
				for (int j : inIndexesToDelete)
				{
					if (j > i && inTrack.getPoint(j) == nextPoint) {
						willBeDeleted = true;
					}
				}
				if (!willBeDeleted) {
					prevPoint = nextPoint;
					flags.add(new PointFlag(nextPoint, true));
				}
			}
		}
		return flags;
	}
}
