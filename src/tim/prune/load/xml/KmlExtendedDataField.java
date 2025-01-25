package tim.prune.load.xml;

import java.util.ArrayList;
import java.util.List;

import tim.prune.data.Field;

/** Holds the values of an ExtendedData section of kml */
public class KmlExtendedDataField
{
	private final Field _field;
	private final ArrayList<String> _values;

	public KmlExtendedDataField(Field inField, List<String> inValues)
	{
		_field = inField;
		_values = new ArrayList<>();
		_values.addAll(inValues);
	}

	Field getField() {
		return _field;
	}

	List<String> getValues() {
		return _values;
	}
}
