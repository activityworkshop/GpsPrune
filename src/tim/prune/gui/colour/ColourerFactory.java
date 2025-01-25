package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.ColourUtils;

/**
 * Factory for the creation of PointColourer objects
 */
public abstract class ColourerFactory
{
	/** Enumeration of colourer types */
	public enum ColourerId
	{
		NONE,
		BY_FILE,
		BY_SEGMENT,
		BY_ALTITUDE,
		BY_SPEED,
		BY_VSPEED,
		BY_GRADIENT,
		BY_DATE
	}

	/**
	 * @param inDesc Single character used as a code (in Config string)
	 * @return associated ColourerId
	 */
	private static ColourerId getColourerId(char inDesc)
	{
		switch (inDesc)
		{
			case 'f': return ColourerId.BY_FILE;
			case 's': return ColourerId.BY_SEGMENT;
			case 'a': return ColourerId.BY_ALTITUDE;
			case 'p': return ColourerId.BY_SPEED;
			case 'v': return ColourerId.BY_VSPEED;
			case 'g': return ColourerId.BY_GRADIENT;
			case 'd': return ColourerId.BY_DATE;
		}
		return ColourerId.NONE;
	}

	/**
	 * @param inColourer colourer object
	 * @return associated prefix
	 */
	private static char getColourerPrefix(PointColourer inColourer)
	{
		if (inColourer instanceof FileColourer) {return 'f';}
		if (inColourer instanceof SegmentColourer) {return 's';}
		if (inColourer instanceof AltitudeColourer) {return 'a';}
		if (inColourer instanceof SpeedColourer) {return 'p';}
		if (inColourer instanceof VertSpeedColourer) {return 'v';}
		if (inColourer instanceof GradientColourer) {return 'g';}
		if (inColourer instanceof DateColourer) {return 'd';}
		throw new IllegalArgumentException("Unrecognised colourer: " + inColourer.getClass().getName());
	}

	/**
	 * Create a new PointColourer object given the parameters
	 * @param inId id of colourer to create
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inMaxColours maximum number of colours
	 * @param inWideHues true for wide mode, false for narrow
	 * @return PointColourer object, or null
	 */
	public static PointColourer createColourer(ColourerId inId, Color inStartColour, Color inEndColour,
		String inMaxColours, boolean inWideHues)
	{
		try
		{
			switch (inId)
			{
				case NONE: return null;
				case BY_FILE: return new FileColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours), inWideHues);
				case BY_SEGMENT: return new SegmentColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours), inWideHues);
				case BY_ALTITUDE: return new AltitudeColourer(inStartColour, inEndColour, inWideHues);
				case BY_SPEED: return new SpeedColourer(inStartColour, inEndColour, inWideHues);
				case BY_VSPEED: return new VertSpeedColourer(inStartColour, inEndColour, inWideHues);
				case BY_GRADIENT: return new GradientColourer(inStartColour, inEndColour, inWideHues);
				case BY_DATE: return new DateColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours), inWideHues);
			}
		}
		catch (NumberFormatException ignored) {} // drop out to return null
		return null;
	}

	/**
	 * Create a PointColourer object from the given description
	 * @param inString string from config
	 * @return PointColourer object, or null if string was invalid
	 */
	public static PointColourer createColourer(String inString)
	{
		try
		{
			String[] comps = inString.split(";");
			if (comps.length == 4 || comps.length == 5)
			{
				ColourerId colourerType = getColourerId(comps[0].charAt(0));
				Color startColour = ColourUtils.colourFromHex(comps[1]);
				Color endColour   = ColourUtils.colourFromHex(comps[2]);
				String maxColours = comps[3];
				boolean isWide = (comps.length == 5 && comps[4].equals("w"));
				return createColourer(colourerType, startColour, endColour, maxColours, isWide);
			}
		}
		catch (NullPointerException | NumberFormatException ignored) {}
		return null;
	}

	/**
	 * Convert the given PointColourer object into a string for the config
	 * @param inColourer PointColourer object
	 * @return string describing object (for later re-creation) or null
	 */
	public static String pointColourerToString(PointColourer inColourer)
	{
		if (inColourer != null)
		{
			final String startColour = ColourUtils.makeHexCode(inColourer.getStartColour());
			final String endColour = ColourUtils.makeHexCode(inColourer.getEndColour());
			final boolean isContinuous = inColourer instanceof ContinuousPointColourer;
			final int maxColours = isContinuous ? 0 : inColourer.getMaxColours();

			String result = getColourerPrefix(inColourer) + ";" + startColour + ";" + endColour + ";" + maxColours;
			if (inColourer.isWideHueScaling()) {
				result = result + ";w";
			}
			return result;
		}
		return null;
	}

	/**
	 * Get the colourer-specific end of the description key for translation
	 * @param inId id of colourer
	 * @return end of description key for combobox text
	 */
	public static String getDescriptionKey(ColourerId inId)
	{
		switch (inId)
		{
			case NONE:        return "none";
			case BY_FILE:     return "byfile";
			case BY_SEGMENT:  return "bysegment";
			case BY_ALTITUDE: return "byaltitude";
			case BY_SPEED:    return "byspeed";
			case BY_VSPEED:   return "byvertspeed";
			case BY_GRADIENT: return "bygradient";
			case BY_DATE:     return "bydate";
		}
		return null;
	}

	/**
	 * Get the id of the given colourer, according to its class
	 * @param inColourer point colourer object, or null
	 * @return id, for example for selection in dropdown
	 */
	public static ColourerId getId(PointColourer inColourer)
	{
		if (inColourer != null)
		{
			if (inColourer instanceof FileColourer)      {return ColourerId.BY_FILE;}
			if (inColourer instanceof SegmentColourer)   {return ColourerId.BY_SEGMENT;}
			if (inColourer instanceof AltitudeColourer)  {return ColourerId.BY_ALTITUDE;}
			if (inColourer instanceof SpeedColourer)     {return ColourerId.BY_SPEED;}
			if (inColourer instanceof VertSpeedColourer) {return ColourerId.BY_VSPEED;}
			if (inColourer instanceof GradientColourer)  {return ColourerId.BY_GRADIENT;}
			if (inColourer instanceof DateColourer)      {return ColourerId.BY_DATE;}
		}
		return ColourerId.NONE;
	}
}
