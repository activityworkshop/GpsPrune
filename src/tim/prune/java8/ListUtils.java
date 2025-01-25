package tim.prune.java8;

import java.util.ArrayList;
import java.util.List;

import tim.prune.cmd.PointFlag;
import tim.prune.function.edit.FieldEdit;

/** Provide java8 compatibility */
public abstract class ListUtils
{
	/** Equivalent of List.of for one or more integer values */
	public static List<Integer> makeListOfInts(int ... inValues)
	{
		ArrayList<Integer> result = new ArrayList<>();
		for (int value : inValues) {
			result.add(value);
		}
		return result;
	}

	/** Equivalent of List.of for a single String */
	public static List<String> makeListOfString(String inValue)
	{
		ArrayList<String> values = new ArrayList<>();
		values.add(inValue);
		return values;
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

	@SafeVarargs
	public static <T> List<T> makeList(T ... inObjects)
	{
		ArrayList<T> result = new ArrayList<>();
		for (T object : inObjects) {
			result.add(object);
		}
		return result;
	}
}
