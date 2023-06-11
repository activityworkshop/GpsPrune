package tim.prune.data;

import java.util.ArrayList;
import java.util.List;

import tim.prune.cmd.PointFlag;
import tim.prune.function.edit.FieldEdit;

/** Provide java8 compatibility */
public abstract class ListUtils
{
	/** Equivalent of List.of for a single integer value */
	public static List<Integer> makeListOfInteger(int inValue)
	{
		ArrayList<Integer> result = new ArrayList<>();
		result.add(inValue);
		return result;
	}

	/** Equivalent of List.of for a single FieldEdit value */
	public static List<FieldEdit> makeListOfEdit(FieldEdit inEdit)
	{
		ArrayList<FieldEdit> edits = new ArrayList<>();
		edits.add(inEdit);
		return edits;
	}

	/** Equivalent of List.of for a single PointFlag value */
	public static List<PointFlag> makeListOfFlag(PointFlag inFlag)
	{
		ArrayList<PointFlag> flags = new ArrayList<>();
		flags.add(inFlag);
		return flags;
	}

	/** Equivalent of List.of for a single MediaObject */
	public static List<MediaObject> makeListOfMedia(MediaObject inObject)
	{
		ArrayList<MediaObject> media = new ArrayList<>();
		media.add(inObject);
		return media;
	}
}
