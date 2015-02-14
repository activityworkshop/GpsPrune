package tim.prune.load;

import tim.prune.I18nManager;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Timestamp;

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
		// Loop over values looking for a Latitude value
		if (inValues != null)
		{
			for (int v=0; v<inValues.length; v++)
			{
				Latitude lat = new Latitude(inValues[v]);
				if (lat.isValid()) {return false;}
			}
		}
		// No valid Latitude value found so presume header
		return true;
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
					continue;
				}
				// check for longitude
				if (!checkArrayHasField(fields, Field.LONGITUDE) && fieldLooksLikeLongitude(value, isHeader))
				{
					fields[f] = Field.LONGITUDE;
					continue;
				}
				// check for altitude
				if (!checkArrayHasField(fields, Field.ALTITUDE) && fieldLooksLikeAltitude(value, isHeader))
				{
					fields[f] = Field.ALTITUDE;
					continue;
				}
				// check for waypoint name
				if (!checkArrayHasField(fields, Field.WAYPT_NAME) && fieldLooksLikeName(value, isHeader))
				{
					fields[f] = Field.WAYPT_NAME;
					continue;
				}
				// check for timestamp
				if (!checkArrayHasField(fields, Field.TIMESTAMP) && fieldLooksLikeTimestamp(value, isHeader))
				{
					fields[f] = Field.TIMESTAMP;
					continue;
				}
				// check for tracksegment
				if (!checkArrayHasField(fields, Field.NEW_SEGMENT) && fieldLooksLikeSegment(value, isHeader))
				{
					fields[f] = Field.NEW_SEGMENT;
					continue;
				}
				// check for waypoint type
				if (!checkArrayHasField(fields, Field.WAYPT_TYPE) && fieldLooksLikeWaypointType(value, isHeader))
				{
					fields[f] = Field.WAYPT_TYPE;
					continue;
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
				else {
					customFieldNum++;
					fields[f] = new Field(customPrefix + (customFieldNum));
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
	 * @param inFields
	 * @param inCheckField
	 * @return true if Field is contained within the array
	 */
	private static boolean checkArrayHasField(Field[] inFields, Field inCheckField)
	{
		for (int f=0; f<inFields.length; f++)
		{
			if (inFields[f] != null && inFields[f].equals(inCheckField)) {
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
	private static boolean fieldLooksLikeLatitude(String inValue, boolean inIsHeader)
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
			// Note this will also catch longitudes too
			Latitude lat = new Latitude(inValue);
			return lat.isValid();
		}
	}

	/**
	 * Check whether the given String looks like a Longitude value
	 * @param inValue value from file
	 * @param inIsHeader true if this is a header line, false for data
	 * @return true if it could be longitude
	 */
	private static boolean fieldLooksLikeLongitude(String inValue, boolean inIsHeader)
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
			Longitude lon = new Longitude(inValue);
			return lon.isValid();
		}
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
			Timestamp stamp = new Timestamp(inValue);
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
