package tim.prune.function.olc;


/**
 * Decoder of OLC (Open Location Code) strings
 */
public class OlcDecoder
{
	/**
	 * Decode the given String into an OlcArea object
	 * @param inCode code representing an OLC
	 * @return an OlcArea object, or null if parsing failed
	 */
	public static OlcArea decode(String inCode)
	{
		if (inCode == null || inCode.length() < 8) {
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

			// Make OlcArea object and return it
			if (numSteps < 1)
			{
				return null;
			}
			else if (numSteps < 6)
			{
				// make four points
				lat -= 90.0;
				lon -= 180.0;
				return new OlcArea(lat, lon, lat+resolution, lon+resolution);
			}
			else
			{
				// make single point:
				lat -= 90.0;
				lon -= 180.0;
				return new OlcArea(lat, lon, lat+resolution*2.5, lon+resolution*2.0);
			}
		}
		catch (ParseException e) {}
		return null;
	}
}