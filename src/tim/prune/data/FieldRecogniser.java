package tim.prune.data;

import tim.prune.I18nManager;

import java.util.HashMap;

public class FieldRecogniser
{
	static final HashMap<String, String> _lookupMap = new HashMap<>();

	/** Called only once to populate the static lookup map */
	private static synchronized void fillMap()
	{
		if (!_lookupMap.isEmpty()) {
			return; // already done
		}
		_lookupMap.put("hr", "heartrate");
		_lookupMap.put("heart", "heartrate");
		_lookupMap.put("cad", "cadence");
		String[] knowns = {"heartrate", "cadence", "power", "speed", "course"};
		for (String known : knowns) {
			_lookupMap.put(known, known);
		}
	}

	/** @return a displayable label for the given xml tag */
	public static String getLabel(String inTagName)
	{
		fillMap();
		int colonPos = inTagName.lastIndexOf(':');
		if (colonPos < 0) {
			return translate(inTagName);
		}
		String suffix = inTagName.substring(colonPos + 1);
		return translate(suffix);
	}

	/** @return the name of the field if a mapping is known, otherwise the tag name itself */
	private static String translate(String inTagName)
	{
		String token = _lookupMap.getOrDefault(inTagName.toLowerCase(), null);
		if (token != null) {
			return I18nManager.getText("fieldname." + token);
		}
		return inTagName;
	}

	/** Just for testing! */
	protected static int getMappingSize() {
		return _lookupMap == null ? 0 : _lookupMap.size();
	}

	/** Just for testing! */
	protected static void clearMap() {
		_lookupMap.clear();
	}
}
