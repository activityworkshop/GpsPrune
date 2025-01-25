package tim.prune.cmd;

import java.util.List;

import tim.prune.data.Field;
import tim.prune.function.edit.PointEdit;

/** Compound command to set latitude and longitude of a set of points */
public class EditPositionsCmd extends CompoundCommand
{
	public EditPositionsCmd(List<PointEdit> inLatEditList, List<PointEdit> inLonEditList)
	{
		addCommand(new EditSingleFieldCmd(Field.LATITUDE, inLatEditList, null));
		addCommand(new EditSingleFieldCmd(Field.LONGITUDE, inLonEditList, null));
	}
}
