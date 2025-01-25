package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSet;
import tim.prune.function.edit.FieldEdit;
import tim.prune.java8.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to edit a single point with one or more fields
 */
public class EditPointCmd extends Command
{
	private final int _pointIndex;
	private final List<FieldEdit> _editList;
	private final UnitSet _unitSet;


	public EditPointCmd(int inPointIndex, List<FieldEdit> inEditList) {
		this(inPointIndex, inEditList, null);
	}

	public EditPointCmd(int inPointIndex, FieldEdit inEdit) {
		this(null, inPointIndex, ListUtils.makeListOfEdit(inEdit), null);
	}

	public EditPointCmd(int inPointIndex, List<FieldEdit> inEditList, UnitSet inUnitSet) {
		this(null, inPointIndex, inEditList, inUnitSet);
	}

	protected EditPointCmd(EditPointCmd inParent, int inPointIndex, List<FieldEdit> inEditList, UnitSet inUnitSet)
	{
		super(inParent);
		_pointIndex = inPointIndex;
		_editList = inEditList;
		_unitSet = inUnitSet;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		DataPoint point = inInfo.getTrack().getPoint(_pointIndex);
		if (point == null) {
			return false;
		}
		boolean coordsChanged = false;
		for (FieldEdit edit : _editList)
		{
			Field field = edit.getField();
			point.setFieldValue(field, edit.getValue(), _unitSet, isUndo());
			inInfo.getTrack().getFieldList().addField(field);
			coordsChanged |= (field.equals(Field.LATITUDE)
				|| field.equals(Field.LONGITUDE) || field.equals(Field.ALTITUDE));
		}
		// set photo status if coordinates have changed
		if (coordsChanged)
		{
			if (point.getPhoto() != null) {
				point.getPhoto().setCurrentStatus(Photo.Status.CONNECTED);
			}
		}
		inInfo.getTrack().requestRescale();
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		DataPoint pointToEdit = inInfo.getTrack().getPoint(_pointIndex);
		return new EditPointCmd(this, _pointIndex, makeOppositeEdits(pointToEdit), _unitSet);
	}

	private List<FieldEdit> makeOppositeEdits(DataPoint pointToEdit)
	{
		List<FieldEdit> opposite = new ArrayList<>();
		for (FieldEdit fieldEdit : _editList) {
			final Field field = fieldEdit.getField();
			opposite.add(new FieldEdit(field, pointToEdit.getFieldValue(field)));
		}
		return opposite;
	}
}
