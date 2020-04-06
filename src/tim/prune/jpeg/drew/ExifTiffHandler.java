/*
 * Copyright 2002-2015 Drew Noakes
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package tim.prune.jpeg.drew;

import tim.prune.jpeg.JpegData;

/**
 * Implementation of TiffHandler used for handling TIFF tags according to the Exif standard.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class ExifTiffHandler
{
	private JpegData _jpegData = null;
	private long _thumbnailOffset = -1L, _thumbnailLength = -1L;

	/** This tag is a pointer to the Exif SubIFD. */
	private static final int DIR_EXIF_SUB_IFD_OFFSET = 0x8769;
	/** This tag is a pointer to the Exif GPS IFD. */
	private static final int DIR_GPS_INFO_OFFSET = 0x8825;

	private static final int TAG_GPS_LATITUDE_REF  = 0x0001;
	private static final int TAG_GPS_LATITUDE      = 0x0002;
	private static final int TAG_GPS_LONGITUDE_REF = 0x0003;
	private static final int TAG_GPS_LONGITUDE     = 0x0004;
	private static final int TAG_GPS_ALTITUDE      = 0x0006;
	private static final int TAG_GPS_BEARING       = 0x0011;

	private static final int TAG_ORIENTATION       = 0x0112;
	private static final int TAG_THUMBNAIL_OFFSET  = 0x0201;
	private static final int TAG_THUMBNAIL_LENGTH  = 0x0202;

	private static final int TAG_SUB_ORITIME       = 0x9003;
	private static final int TAG_SUB_DIGITIME      = 0x9004;


	/**
	 * Constructor
	 * @param jpegData data object to populate with received results
	 */
	public ExifTiffHandler(JpegData jpegData)
	{
		_jpegData = jpegData;
		_thumbnailOffset = _thumbnailLength = -1L;
	}

	public boolean isTagIfdPointer(int tagType)
	{
		if (tagType == DIR_EXIF_SUB_IFD_OFFSET) {
			return true;
		} else if (tagType == DIR_GPS_INFO_OFFSET) {
			return true;
		}

		return false;
	}

	public void completed(final ByteArrayReader reader, final int tiffHeaderOffset)
	{
		// after the extraction process, if we have the correct tags, we may be able to store thumbnail information
		if (_thumbnailOffset >= 0L && _thumbnailLength > 0L)
		{
			try {
				byte[] thumbData = reader.getBytes(tiffHeaderOffset + (int) _thumbnailOffset, (int) _thumbnailLength);
				if (thumbData != null)
				{
					byte[] thumbCopy = new byte[thumbData.length];
					System.arraycopy(thumbData, 0, thumbCopy, 0, thumbData.length);
					_jpegData.setThumbnailImage(thumbCopy);
				}
			} catch (ExifException ex) {}
		}
	}

	public void setRationalArray(int tagId, Rational[] array)
	{
		switch (tagId)
		{
			case TAG_GPS_LATITUDE:
				_jpegData.setLatitude(new double[] {array[0].doubleValue(), array[1].doubleValue(),
					array[2].convertToPositiveValue()});
				break;
			case TAG_GPS_LONGITUDE:
				_jpegData.setLongitude(new double[] {array[0].doubleValue(), array[1].doubleValue(),
					array[2].convertToPositiveValue()});
				break;
		}
	}

	public void setRational(int tagId, Rational rational)
	{
		switch (tagId)
		{
			case TAG_GPS_ALTITUDE:
				_jpegData.setAltitude(rational.intValue());
				return;
			case TAG_GPS_BEARING:
				_jpegData.setBearing(rational.doubleValue());
				return;
		}
		// maybe it was an integer passed as a rational?
		if (rational.getDenominator() == 1L) {
			setIntegerValue(tagId, rational.intValue());
		}
	}

	public void setString(int tagId, String string)
	{
		switch (tagId)
		{
			case TAG_SUB_ORITIME:
				_jpegData.setOriginalTimestamp(string);
				break;
			case TAG_SUB_DIGITIME:
				_jpegData.setDigitizedTimestamp(string);
				break;
			case TAG_GPS_LATITUDE_REF:
				_jpegData.setLatitudeRef(string);
				break;
			case TAG_GPS_LONGITUDE_REF:
				_jpegData.setLongitudeRef(string);
				break;
		}
	}

	public void setIntegerValue(int tagId, int intVal)
	{
		switch (tagId)
		{
			case TAG_ORIENTATION:
				_jpegData.setOrientationCode(intVal);
				break;
			case TAG_THUMBNAIL_OFFSET:
				_thumbnailOffset = intVal;
				break;
			case TAG_THUMBNAIL_LENGTH:
				_thumbnailLength = intVal;
				break;
			case TAG_GPS_BEARING:
				_jpegData.setBearing(intVal);
				break;
		}
	}


	/**
	 * Decide, based on the directory id and the tag id, if we want to parse and process it
	 * @param inDirectoryId
	 * @param childTagId
	 * @return true if the tag should be parsed
	 */
	public boolean isInterestingTag(int inDirectoryId, int childTagId)
	{
		switch (inDirectoryId)
		{
			case DIR_GPS_INFO_OFFSET:
				return true;
			case DIR_EXIF_SUB_IFD_OFFSET:
				return childTagId == TAG_SUB_ORITIME
					|| childTagId == TAG_SUB_DIGITIME;
			case 0:
				return childTagId == TAG_THUMBNAIL_OFFSET
					|| childTagId == TAG_THUMBNAIL_LENGTH
					|| childTagId == TAG_ORIENTATION;
		}
		return false;
	}
}
