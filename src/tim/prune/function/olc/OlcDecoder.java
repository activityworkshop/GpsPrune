package tim.prune.function.olc;

import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;

/**
 * Decoder of OLC (Open Location Code) strings
 */
public class OlcDecoder
{
	/**
	 * Decode the given String into an OlcArea object
	 * @param inCode code representing an OLC, either in short form or long form
	 * @param inGuideLatitude latitude guide to help guess the prefix
	 * @param inGuideLongitude longitude guide to help guess the prefix
	 * @return an OlcArea object, or null if parsing failed
	 */
	public static OlcArea decode(String inCode, double inGuideLatitude, double inGuideLongitude)
	{
		if (isValidLongForm(inCode)) {
			return decode(inCode);
		}
		if (!isValidShortForm(inCode)) {
			return null;
		}
		return findBestArea(inCode, inGuideLatitude, inGuideLongitude);
	}

	public static boolean isValidLongForm(String inCode)
	{
		if (inCode == null) {return false;}
		final int plusPos = inCode.indexOf('+');
		return inCode.indexOf(' ') < 0 && inCode.indexOf(',') < 0
			&& ((inCode.length() == 8 && plusPos < 0)	// long form up to (but not including) the +
				|| (inCode.length() > 8 && plusPos == 8)); // long form including + at position 8
	}

	public static boolean isValidShortForm(String inCode)
	{
		if (inCode == null) {return false;}
		final int plusPos = inCode.indexOf('+');
		return inCode.indexOf(' ') < 0 && inCode.indexOf(',') < 0
			&& (inCode.length() <= 8 && plusPos == 4); // truncated form with + at position 4
	}

	/**
	 * Decode the given String into an OlcArea object
	 * @param inCode code representing an OLC in long form
	 * @return an OlcArea object, or null if parsing failed
	 */
	public static OlcArea decode(String inCode)
	{
		if (!isValidLongForm(inCode)) {
			return null;
		}
		String code = inCode.trim().toUpperCase();
		if (code.length() < 8 || code.length() > 12) {
			return null;
		}
		double lat = 0.0, lon = 0.0;
		double resolution = 400.0;
		int charPos = 0;
		int numSteps = 0;
		boolean amPadding = false;
		try
		{
			while (charPos < inCode.length())
			{
				if (charPos == 0 || charPos == 2 || charPos == 4 || charPos == 6 || charPos == 9)
				{
					// take next two characters, make pair, position += 2
					CoordPair pair = CoordPair.decode(code.charAt(charPos), code.charAt(charPos+1));
					if (pair == CoordPair.PADDING) {
						amPadding = true;
					}
					else if (amPadding)
					{
						return null;
					}
					else
					{
						// Add to current lat, lon
						lat += (pair.lat * resolution);
						lon += (pair.lon * resolution);
						numSteps++;
						resolution /= 20.0;
					}
					charPos += 2;
				}
				else if (charPos == 8)
				{
					if (code.charAt(charPos) != '+')
					{
						return null;
					}
					charPos += 1;
				}
				else if (charPos == 11)
				{
					// take next character, make pair
					CoordPair pair = CoordPair.decode(code.charAt(charPos));
					// Add to current lat, lon
					lat += (pair.lat * resolution);
					lon += (pair.lon * resolution);
					charPos += 1;
					numSteps++;
					resolution /= 20.0;
				}
				else
				{
					return null;
				}
			}

			if (numSteps < 1) {
				return null;
			}
			// Make OlcArea object and return it
			lat -= 90.0;
			lon -= 180.0;
			if (numSteps < 6) {
				return new OlcArea(lat, lon, lat+resolution, lon+resolution, inCode);
			}
			else {
				// For 6 steps, just return a point, not a rectangle
				return new OlcArea(lat, lon, lat, lon, inCode);
			}
		}
		catch (ParseException e) {}
		return null;
	}

	/**
	 * @param inCode partial OLC code (truncated form with prefix missing
	 * @param inGuideLatitude latitude of nearby point
	 * @param inGuideLongitude longitude of nearby point
	 * @return the nearest OlcArea to the specified guide point
	 */
	private static OlcArea findBestArea(String inCode, double inGuideLatitude, double inGuideLongitude)
	{
		// Find the two latitude indexes
		double latIndex = (inGuideLatitude + 90.0) / 20.0;
		int latIndex1 = (int) Math.floor(latIndex);
		int latIndex2 = (int) Math.floor((latIndex - latIndex1) * 20.0);
		// And the same for longitude
		double lonIndex = ((inGuideLongitude + 180.0)%360.0) / 20.0;
		int lonIndex1 = (int) Math.floor(lonIndex);
		int lonIndex2 = (int) Math.floor((lonIndex - lonIndex1) * 20.0);
		// Now we have 9 possible squares to look through:
		DataPoint guidePoint = new DataPoint(new Latitude(inGuideLatitude, Coordinate.FORMAT_DEG),
			new Longitude(inGuideLongitude, Coordinate.FORMAT_DEG), null);
		OlcArea bestAnswer = null;
		double lowestDistance = 0.0;
		for (int dy=-1; dy<=1; dy++)
		{
			for (int dx=-1; dx<=1; dx++)
			{
				StringBuilder longCode = new StringBuilder();
				longCode.append(CoordPair.encode(latIndex1 + getDelta(latIndex2, dy)));
				longCode.append(CoordPair.encode(lonIndex1 + getDelta(lonIndex2, dx)));
				longCode.append(CoordPair.encode(latIndex2 + dy));
				longCode.append(CoordPair.encode(lonIndex2 + dx));
				longCode.append(inCode);
				OlcArea probeArea = decode(longCode.toString());
				double distance = calcDistance(probeArea, guidePoint);
				if (bestAnswer == null || distance < lowestDistance)
				{
					bestAnswer = probeArea;
					lowestDistance = distance;
				}
			}
		}
		return bestAnswer;
	}

	/**
	 * @param secondIndex lower-order index
	 * @param increment change in lower-order index from [-1, 0, 1]
	 * @return delta of higher-order index caused by carry
	 */
	private static int getDelta(int secondIndex, int increment)
	{
		if (secondIndex == 0 && increment < 0) {return -1;}
		if (secondIndex == 19 && increment > 0) {return 1;}
		return 0;
	}

	/**
	 * @param probeArea possible OLC area
	 * @param inGuidePoint guide point which should be nearby
	 * @return distance in radians from the given guide point to the centre of the olc area
	 */
	private static double calcDistance(OlcArea probeArea, DataPoint inGuidePoint)
	{
		DataPoint probePoint = new DataPoint(new Latitude(probeArea.middleLat(), Coordinate.FORMAT_DEG),
			new Longitude(probeArea.middleLon(), Coordinate.FORMAT_DEG), null);
		return DataPoint.calculateRadiansBetween(probePoint, inGuidePoint);
	}
}
