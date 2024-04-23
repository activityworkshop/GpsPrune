package tim.prune.load;

import tim.prune.I18nManager;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.TimestampUtc;

/**
 * Class to try to match data with field names,
 * using a variety of guessing techniques
 */
public abstract class FieldGuesser
{
	/**
	 * Try to guess whether the given line is a header line or data
	 * @param inValues array of values from first non-blank line of file
	 * @return true if it looks like a header row, false if it looks like data
	 */
	private static boolean isHeaderRow(String[] inValues)
	{
		// Loop over values seeing if any are mostly numeric
		if (inValues != null)
		{
			for (String value : inValues)
			{
				if (fieldLooksNumeric(value)) {return false;}
			}
		}
		// No (mostly) numeric values found so presume header
		return true;
	}


	/**
	 * See if a field looks numeric or not by comparing the number of numeric vs non-numeric characters
	 * @param inValue field value to check
	 * @return true if there are more numeric characters than not
	 */
	private static boolean fieldLooksNumeric(String inValue)
	{
		if (inValue == null) {
			return false;
		}
		final int numChars = inValue.length();
		if (numChars < 3) {return false;} // Don't care about one or two character values
		// Loop through characters seeing which ones are numeric and which not
		int numNums = 0;
		for (int i=0; i<numChars; i++)
		{
			char currChar = inValue.charAt(i);
			if (currChar >= '0' && currChar <= '9') {numNums++;}
		}
		// Return true if more than half the characters are numeric
		return numNums > (numChars/2);
	}

	/**
	 * Try to guess the fields for the given values from the file
	 * @param inValues array of values from first non-blank line of file
	 * @return array of fields which hopefully match
	 */
	public static Field[] guessFields(String[] inValues)
	{
		// Guess whether it's a header line or not
		boolean isHeader = isHeaderRow(inValues);
		// make array of Fields
		int numFields = inValues.length;
		Field[] fields = new Field[numFields];
		// Loop over fields to try to guess the main ones
		for (int f=0; f<numFields; f++)
		{
			if (inValues[f] != null) {
				String value = inValues[f].trim();
				// check for latitude
				if (!checkArrayHasField(fields, Field.LATITUDE) && fieldLooksLikeLatitude(value, isHeader))
				{
					fields[f] = Field.LATITUDE;
				}
				// check for longitude
				else if (!checkArrayHasField(fields, Field.LONGITUDE) && fieldLooksLikeLongitude(value, isHeader))
				{
					fields[f] = Field.LONGITUDE;
				}
				// check for altitude
				else if (!checkArrayHasField(fields, Field.ALTITUDE) && fieldLooksLikeAltitude(value, isHeader))
				{
					fields[f] = Field.ALTITUDE;
				}
				// check for waypoint name
				else if (!checkArrayHasField(fields, Field.WAYPT_NAME) && fieldLooksLikeName(value, isHeader))
				{
					fields[f] = Field.WAYPT_NAME;
				}
				// check for timestamp
				else if (!checkArrayHasField(fields, Field.TIMESTAMP) && fieldLooksLikeTimestamp(value, isHeader))
				{
					fields[f] = Field.TIMESTAMP;
				}
				// check for tracksegment
				else if (!checkArrayHasField(fields, Field.NEW_SEGMENT) && fieldLooksLikeSegment(value, isHeader))
				{
					fields[f] = Field.NEW_SEGMENT;
				}
				// check for waypoint type
				else if (!checkArrayHasField(fields, Field.WAYPT_TYPE) && fieldLooksLikeWaypointType(value, isHeader))
				{
					fields[f] = Field.WAYPT_TYPE;
				}
			}
		}
		// Fill in the rest of the fields using just custom fields
		// Could try to guess other fields (waypoint type, segment) or unguessed altitude, name, but keep simple for now
		String customPrefix = I18nManager.getText("fieldname.prefix") + " ";
		int customFieldNum = 0;
		for (int f=0; f<numFields; f++) {
			if (fields[f] == null)
			{
				// Make sure lat and long are filled in if not already
				if (!checkArrayHasField(fields, Field.LATITUDE)) {
					fields[f] = Field.LATITUDE;
				}
				else if (!checkArrayHasField(fields, Field.LONGITUDE)) {
					fields[f] = Field.LONGITUDE;
				}
				else
				{
					// Can we use the field name given?
					Field customField = null;
					if (isHeader && inValues[f] != null && inValues[f].length() > 0) {
						customField = new Field(inValues[f]);
					}
					// Find an unused field number
					while (customField == null || checkArrayHasField(fields, customField))
					{
						customFieldNum++;
						customField = new Field(customPrefix + (customFieldNum));
					}
					fields[f] = customField;
				}
			}
		}

		// Do a final check to make sure lat and long are in there
		if (!checkArrayHasField(fields, Field.LATITUDE)) {
			fields[0] = Field.LATITUDE;
		}
		else if (!checkArrayHasField(fields, Field.LONGITUDE)) {
			fields[1] = Field.LONGITUDE;
		}
		// Longitude _could_ have overwritten latitude in position 1
		if (!checkArrayHasField(fields, Field.LATITUDE)) {
			fields[0] = Field.LATITUDE;
		}
		return fields;
	}


	/**
	 * Check whether the given field array has the specified field
	 * @param inFields field array to look through
	 * @param inCheckField field to look for
	 * @return true if Field is contained within the array
	 */
	private static boolean checkArrayHasField(Field[] inFields, Field inCheckField)
	{
		for (Field field : inFields)
		{
			if (field != null && field.equals(inCheckField)) {
				return true;
			}
		}
		// not found
		return false;
	}


	/**
	 * Check whether the given String looks like a Latitude value
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be latitude
	 */
	public static boolean fieldLooksLikeLatitude(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			// This is a header line so look for english or local text
			String upperValue = inValue.toUpperCase();
			return (upperValue.equals("LATITUDE")
				|| upperValue.equals(I18nManager.getText("fieldname.latitude").toUpperCase()));
		}
		else
		{
			// Note this will also catch longitudes too if they're within range
			return couldBeCoordinateString(inValue) && Latitude.make(inValue) != null;
		}
	}

	/**
	 * Check whether the given String looks like a Longitude value
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be longitude
	 */
	public static boolean fieldLooksLikeLongitude(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			// This is a header line so look for english or local text
			String upperValue = inValue.toUpperCase();
			return (upperValue.equals("LONGITUDE")
				|| upperValue.equals(I18nManager.getText("fieldname.longitude").toUpperCase()));
		}
		else
		{
			// Note this will also catch latitudes too
			return couldBeCoordinateString(inValue) && Longitude.make(inValue) != null;
		}
	}

	/**
	 * @return true if this string could represent a lat/long coordinate
	 */
	public static boolean couldBeCoordinateString(String inValue)
	{
		final String value = inValue == null ? "" : inValue.trim().toUpperCase();
		if (value.length() < 2) {
			return false;
		}
		int totalLetters = 0, totalNumbers = 0;
		boolean prevCharWasLetter = false;
		for (int i=0; i<value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isAlphabetic(c)) {
				if (prevCharWasLetter) {return false;} // no consecutive letters allowed
				totalLetters++;
				prevCharWasLetter = true;
			}
			else {
				prevCharWasLetter = false;
			}
			if (Character.isDigit(c)) {
				totalNumbers++;
			}
		}
		return totalLetters < 3 && totalNumbers > 1;
	}

	/**
	 * Check whether the given String looks like an Altitude value
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be altitude
	 */
	private static boolean fieldLooksLikeAltitude(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			// This is a header line so look for english or local text
			String upperValue = inValue.toUpperCase();
			return (upperValue.equals("ALTITUDE")
				|| upperValue.equals("ALT")
				|| upperValue.equals("HMSL") // height above mean sea level
				|| upperValue.equals(I18nManager.getText("fieldname.altitude").toUpperCase()));
		}
		else
		{
			// Look for a number less than 100000
			try
			{
				int intValue = Integer.parseInt(inValue);
				return (intValue > 0 && intValue < 100000);
			}
			catch (NumberFormatException nfe) {}
			return false;
		}
	}


	/**
	 * Check whether the given String looks like a waypoint name
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be a name
	 */
	private static boolean fieldLooksLikeName(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			// This is a header line so look for english or local text
			String upperValue = inValue.toUpperCase();
			return (upperValue.equals("NAME")
				|| upperValue.equals("LABEL")
				|| upperValue.equals(I18nManager.getText("fieldname.waypointname").toUpperCase()));
		}
		else
		{
			// Look for at least two letters in it
			int numLetters = 0;
			for (int i=0; i<inValue.length(); i++)
			{
				char currChar = inValue.charAt(i);
				if (Character.isLetter(currChar)) {
					numLetters++;
				}
				// Not interested if it contains ":" or "."
				if (currChar == ':' || currChar == '.') {return false;}
			}
			return numLetters >= 2;
		}
	}

	/**
	 * Check whether the given String looks like a timestamp
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be a timestamp
	 */
	private static boolean fieldLooksLikeTimestamp(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			String upperValue = inValue.toUpperCase();
			// This is a header line so look for english or local text
			return (upperValue.equals("TIMESTAMP")
				|| upperValue.equals("TIME")
				|| upperValue.equals(I18nManager.getText("fieldname.timestamp").toUpperCase()));
		}
		else
		{
			// must be at least 7 characters long
			if (inValue.length() < 7) {return false;}
			TimestampUtc stamp = new TimestampUtc(inValue);
			return stamp.isValid();
		}
	}

	/**
	 * Check whether the given String looks like a track segment field
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be a track segment
	 */
	private static boolean fieldLooksLikeSegment(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			String upperValue = inValue.toUpperCase();
			// This is a header line so look for english or local text
			return upperValue.equals("SEGMENT")
				|| upperValue.equals(I18nManager.getText("fieldname.newsegment").toUpperCase());
		}
		else
		{
			// can't reliably identify it just using the value
			return false;
		}
	}

	/**
	 * Check whether the given String looks like a waypoint type
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be a waypoint type
	 */
	private static boolean fieldLooksLikeWaypointType(String inValue, boolean inIsHeader)
	{
		if (inValue == null || inValue.equals("")) {return false;}
		if (inIsHeader)
		{
			String upperValue = inValue.toUpperCase();
			// This is a header line so look for english or local text
			return (upperValue.equals("TYPE")
				|| upperValue.equals(I18nManager.getText("fieldname.waypointtype").toUpperCase()));
		}
		else
		{
			// can't reliably identify it just using the value
			return false;
		}
	}
}
