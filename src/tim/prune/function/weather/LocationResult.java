package tim.prune.function.weather;

public class LocationResult
{
	private final String _name;
	private final String _errorMessage;

	private LocationResult(String inResult, String inError)
	{
		_name = inResult;
		_errorMessage = inError;
	}

	String getName() {
		return _name;
	}

	boolean isError() {
		return _errorMessage != null;
	}

	String getErrorMessage() {
		return _errorMessage;
	}

	static class IntermediateResult
	{
		private String _name = null, _country = null, _message = null;

		void addPair(String inKey, String inValue)
		{
			if (inKey == null || inValue == null) {
				return;
			}
			if (inKey.equals("name") && isBlank(_name)) {
				_name = inValue;
			}
			else if (inKey.equals("country") && isBlank(_country)) {
				_country = inValue;
			}
			else if (inKey.equals("message")) {
				_message = inValue;
			}
		}

		String getLocation()
		{
			if (isBlank(_name)) {
				return null;
			}
			if (isBlank(_country)) {
				return _name;
			}
			return _name + "," + _country;
		}

		String getErrorMessage()
		{
			if (isBlank(_name) && _message == null) {
				_message = "";
			}
			return _message;
		}

		static boolean isBlank(String inValue) {
			return inValue == null || inValue.isEmpty();
		}
	}

	static LocationResult fromString(String inResponse)
	{
		if (inResponse == null || inResponse.length() < 7) {
			return new LocationResult(null, ""); // no result, empty error
		}
		IntermediateResult result = new IntermediateResult();
		boolean inQuotes = false, afterSlash = false;
		String key = null;
		StringBuilder current = new StringBuilder();
		for (int i=0; i<inResponse.length(); i++)
		{
			char c = inResponse.charAt(i);
			if (inQuotes)
			{
				if (c == '"' && !afterSlash) {
					inQuotes = false;
				}
				else {
					current.append(c);
				}
			}
			else if (c == ':')
			{
				key = current.toString();
				current = new StringBuilder();
			}
			else if (c == ',')
			{
				result.addPair(key, current.toString());
				key = null;
				current = new StringBuilder();
			}
			else if (c == '"') {
				inQuotes = true;
			}
			else if ("[{}] ".indexOf(c) < 0) {
				current.append(c);
			}
			else if (key != null)
			{
				result.addPair(key, current.toString());
				key = null;
				current = new StringBuilder();
			}
			afterSlash = (c == '\\');
		}
		if (key != null) {
			result.addPair(key, current.toString());
		}
		return new LocationResult(result.getLocation(), result.getErrorMessage());
	}
}
