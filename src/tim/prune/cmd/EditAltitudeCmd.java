package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.Unit;
import tim.prune.function.edit.PointAltitudeEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to edit the altitude values of one or more points
 */
public class EditAltitudeCmd extends Command
{
	/** List of all the edits to be made */
	private final List<PointAltitudeEdit> _editList;

	/**
	 * Constructor
	 * @param inEditList list of edits
	 */
	public EditAltitudeCmd(List<PointAltitudeEdit> inEditList) {
		this(null, inEditList);
	}

	/**
	 * Constructor giving parent as well (to construct an Undo)
	 * @param inParent parent command
	 * @param inEditList edit list
	 */
	protected EditAltitudeCmd(EditAltitudeCmd inParent, List<PointAltitudeEdit> inEditList)
	{
		super(inParent);
		_editList = inEditList;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo) {
		return executeCommand(inInfo.getTrack());
	}

	/**
	 * Additional entry point when the App isn't involved
	 * @param inTrack track object (for terrain data)
	 * @return true on success
	 */
	public boolean executeCommand(Track inTrack)
	{
		for (PointAltitudeEdit edit : _editList)
		{
			DataPoint point = inTrack.getPoint(edit.getPointIndex());
			point.setAltitude(edit.getValue(), edit.getUnit(), isUndo());
		}
		inTrack.requestRescale();
		return !_editList.isEmpty();
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new EditAltitudeCmd(this, makeOppositeEdits(inInfo.getTrack()));
	}

	/**
	 * Copy the altitudes from the current track to make an Undo command
	 * @param inTrack track object
	 * @return reverse edit list
	 */
	private List<PointAltitudeEdit> makeOppositeEdits(Track inTrack)
	{
		List<PointAltitudeEdit> opposite = new ArrayList<>();
		for (PointAltitudeEdit edit : _editList)
		{
			DataPoint point = inTrack.getPoint(edit.getPointIndex());
			final String currValue;
			final Unit currUnit;
			if (point.hasAltitude()) {
				currValue = point.getFieldValue(Field.ALTITUDE);
				currUnit = point.getAltitude().getUnit();
			}
			else {
				currValue = null;
				currUnit = null;
			}
			opposite.add(new PointAltitudeEdit(edit.getPointIndex(), currValue, currUnit));
		}
		return opposite;
	}
}
