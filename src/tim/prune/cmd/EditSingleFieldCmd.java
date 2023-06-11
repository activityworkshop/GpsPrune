package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.edit.PointEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to edit a single field applied to one or more points
 */
public class EditSingleFieldCmd extends Command
{
	private final Field _field;
	private final List<PointEdit> _editList;

	public EditSingleFieldCmd(Field inField, List<PointEdit> inEditList) {
		this(null, inField, inEditList);
	}

	protected EditSingleFieldCmd(EditSingleFieldCmd inParent, Field inField, List<PointEdit> inEditList)
	{
		super(inParent);
		_field = inField;
		_editList = inEditList;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_field == null) {
			return false;
		}
		inInfo.getTrack().getFieldList().extendList(_field);
		for (PointEdit edit : _editList) {
			inInfo.getTrack().getPoint(edit.getPointIndex()).setFieldValue(_field, edit.getValue(), isUndo());
		}
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new EditSingleFieldCmd(this, _field, makeOppositeEdits(inInfo.getTrack()));
	}

	private List<PointEdit> makeOppositeEdits(Track inTrack)
	{
		List<PointEdit> opposite = new ArrayList<>();
		for (PointEdit edit : _editList) {
			final String currValue = inTrack.getPoint(edit.getPointIndex()).getFieldValue(_field);
			opposite.add(new PointEdit(edit.getPointIndex(), currValue));
		}
		return opposite;
	}
}
