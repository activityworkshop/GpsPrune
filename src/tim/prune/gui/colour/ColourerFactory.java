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
	 * Does the specified colourer need a field for maximum number of colours?
	 * @param inId id of colourer
	 * @return true if max colours required, false otherwise
	 */
	public static boolean isMaxColoursRequired(ColourerId inId)
	{
		switch (inId)
		{
			case NONE:        return false;
			case BY_FILE:     return FileColourer.isMaxColoursRequired();
			case BY_SEGMENT:  return SegmentColourer.isMaxColoursRequired();
			case BY_ALTITUDE: return AltitudeColourer.isMaxColoursRequired();
			case BY_SPEED:    return SpeedColourer.isMaxColoursRequired();
			case BY_VSPEED:   return VertSpeedColourer.isMaxColoursRequired();
			case BY_GRADIENT: return GradientColourer.isMaxColoursRequired();
			case BY_DATE:     return DateColourer.isMaxColoursRequired();
		}
		return false;
	}

	/**
	 * Does the specified colourer need fields for start and end colours?
	 * @param inId id of colourer
	 * @return true if colours required, false otherwise
	 */
	public static boolean areColoursRequired(ColourerId inId)
	{
		// all of them except NONE need start and end colours
		return inId != ColourerId.NONE;
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
	 * Create a new PointColourer object given the parameters
	 * @param inId id of colourer to create
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inMaxColours maximum number of colours
	 * @return PointColourer object, or null
	 */
	public static PointColourer createColourer(ColourerId inId, Color inStartColour, Color inEndColour, String inMaxColours)
	{
		try
		{
			switch (inId)
			{
				case NONE: return null;
				case BY_FILE: return new FileColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours));
				case BY_SEGMENT: return new SegmentColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours));
				case BY_ALTITUDE: return new AltitudeColourer(inStartColour, inEndColour);
				case BY_SPEED: return new SpeedColourer(inStartColour, inEndColour);
				case BY_VSPEED: return new VertSpeedColourer(inStartColour, inEndColour);
				case BY_GRADIENT: return new GradientColourer(inStartColour, inEndColour);
				case BY_DATE: return new DateColourer(inStartColour, inEndColour, Integer.parseInt(inMaxColours));
			}
		}
		catch (NumberFormatException nfe) {} // drop out to return null
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
			if (comps.length == 4)
			{
				ColourerId colourerType = getColourerId(comps[0].charAt(0));
				Color startColour = ColourUtils.colourFromHex(comps[1]);
				Color endColour   = ColourUtils.colourFromHex(comps[2]);
				String maxColours = comps[3];
				return createColourer(colourerType, startColour, endColour, maxColours);
			}
		}
		catch (NullPointerException npe) {}
		catch (NumberFormatException nfe) {}
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
			final int maxColours = inColourer.getMaxColours();
			if (inColourer instanceof FileColourer) {
				return "f;" + startColour + ";" + endColour + ";" + maxColours;
			}
			else if (inColourer instanceof SegmentColourer) {
				return "s;" + startColour + ";" + endColour + ";" + maxColours;
			}
			else if (inColourer instanceof AltitudeColourer) {
				return "a;" + startColour + ";" + endColour + ";0";
			}
			else if (inColourer instanceof SpeedColourer) {
				return "p;" + startColour + ";" + endColour + ";0";
			}
			else if (inColourer instanceof VertSpeedColourer) {
				return "v;" + startColour + ";" + endColour + ";0";
			}
			else if (inColourer instanceof GradientColourer) {
				return "g;" + startColour + ";" + endColour + ";0";
			}
			else if (inColourer instanceof DateColourer) {
				return "d;" + startColour + ";" + endColour + ";" + maxColours;
			}
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
