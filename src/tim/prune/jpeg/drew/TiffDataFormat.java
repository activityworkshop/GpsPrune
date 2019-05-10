/*
 * Copyright 2002-2015 Drew Noakes
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package tim.prune.jpeg.drew;

/**
 * An enumeration of data formats used by the TIFF specification.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class TiffDataFormat
{
	public static final int CODE_INT8_U = 1;
	public static final int CODE_STRING = 2;
	public static final int CODE_INT16_U = 3;
	public static final int CODE_INT32_U = 4;
	public static final int CODE_RATIONAL_U = 5;
	public static final int CODE_INT8_S = 6;
	public static final int CODE_UNDEFINED = 7;
	public static final int CODE_INT16_S = 8;
	public static final int CODE_INT32_S = 9;
	public static final int CODE_RATIONAL_S = 10;
	public static final int CODE_SINGLE = 11;
	public static final int CODE_DOUBLE = 12;

	public static int getComponentSize(int tiffFormatCode)
	{
		switch (tiffFormatCode)
		{
			case CODE_INT8_U:
			case CODE_STRING:
			case CODE_INT8_S:
			case CODE_UNDEFINED:
				return 1;
			case CODE_INT16_U:
			case CODE_INT16_S:
			case CODE_INT32_U:
			case CODE_INT32_S:
			case CODE_SINGLE:
				return 4;
			case CODE_RATIONAL_U:
			case CODE_RATIONAL_S:
			case CODE_DOUBLE:
				return 8;
			default:
				return 0;
		}
	}
}
