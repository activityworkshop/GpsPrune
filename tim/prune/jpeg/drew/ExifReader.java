package tim.prune.jpeg.drew;

import java.io.File;
import java.util.HashMap;

import tim.prune.jpeg.ExifGateway;
import tim.prune.jpeg.JpegData;

/**
 * Extracts Exif data from a JPEG header segment
 * Based on Drew Noakes' Metadata extractor at http://drewnoakes.com
 * which in turn is based on code from Jhead http://www.sentex.net/~mwandel/jhead/
 */
public class ExifReader
{
	/** The JPEG segment as an array of bytes */
	private final byte[] _data;

	/**
	 * Represents the native byte ordering used in the JPEG segment.  If true,
	 * then we're using Motorola ordering (Big endian), else we're using Intel
	 * ordering (Little endian).
	 */
	private boolean _isMotorolaByteOrder;

	/** Thumbnail offset */
	private int _thumbnailOffset = -1;
	/** Thumbnail length */
	private int _thumbnailLength = -1;

	/** The number of bytes used per format descriptor */
	private static final int[] BYTES_PER_FORMAT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

	/** The number of formats known */
	private static final int MAX_FORMAT_CODE = 12;

	// Format types
	// Note: Cannot use the DataFormat enumeration in the case statement that uses these tags.
	//	   Is there a better way?
	//private static final int FMT_BYTE = 1;
	private static final int FMT_STRING = 2;
	//private static final int FMT_USHORT = 3;
	//private static final int FMT_ULONG = 4;
	private static final int FMT_URATIONAL = 5;
	//private static final int FMT_SBYTE = 6;
	//private static final int FMT_UNDEFINED = 7;
	//private static final int FMT_SSHORT = 8;
	//private static final int FMT_SLONG = 9;
	private static final int FMT_SRATIONAL = 10;
	//private static final int FMT_SINGLE = 11;
	//private static final int FMT_DOUBLE = 12;

	public static final int TAG_EXIF_OFFSET = 0x8769;
	public static final int TAG_INTEROP_OFFSET = 0xA005;
	public static final int TAG_GPS_INFO_OFFSET = 0x8825;
	public static final int TAG_MAKER_NOTE = 0x927C;

	public static final int TIFF_HEADER_START_OFFSET = 6;

	/** GPS tag version GPSVersionID 0 0 BYTE 4 */
	public static final int TAG_GPS_VERSION_ID = 0x0000;
	/** North or South Latitude GPSLatitudeRef 1 1 ASCII 2 */
	public static final int TAG_GPS_LATITUDE_REF = 0x0001;
	/** Latitude GPSLatitude 2 2 RATIONAL 3 */
	public static final int TAG_GPS_LATITUDE = 0x0002;
	/** East or West Longitude GPSLongitudeRef 3 3 ASCII 2 */
	public static final int TAG_GPS_LONGITUDE_REF = 0x0003;
	/** Longitude GPSLongitude 4 4 RATIONAL 3 */
	public static final int TAG_GPS_LONGITUDE = 0x0004;
	/** Altitude reference GPSAltitudeRef 5 5 BYTE 1 */
	public static final int TAG_GPS_ALTITUDE_REF = 0x0005;
	/** Altitude GPSAltitude 6 6 RATIONAL 1 */
	public static final int TAG_GPS_ALTITUDE = 0x0006;
	/** GPS time (atomic clock) GPSTimeStamp 7 7 RATIONAL 3 */
	public static final int TAG_GPS_TIMESTAMP = 0x0007;
	/** GPS date (atomic clock) GPSDateStamp 23 1d RATIONAL 3 */
	public static final int TAG_GPS_DATESTAMP = 0x001d;
	/** "Original" Exif timestamp */
	public static final int TAG_DATETIME_ORIGINAL = 0x9003;
	/** "Creation" or "Digitized" timestamp */
    public static final int TAG_DATETIME_DIGITIZED = 0x9004;
	/** Thumbnail offset */
	private static final int TAG_THUMBNAIL_OFFSET = 0x0201;
	/** Thumbnail length */
	private static final int TAG_THUMBNAIL_LENGTH = 0x0202;
	/** Orientation of image */
	private static final int TAG_ORIENTATION = 0x0112;
	/** Bearing direction of image */
	private static final int TAG_BEARING = 0x0011;


	/**
	 * Creates an ExifReader for a Jpeg file
	 * @param inFile File object to attempt to read from
	 * @throws JpegException on failure
	 */
	public ExifReader(File inFile) throws JpegException
	{
		_data = JpegSegmentReader.readExifSegment(inFile);
	}

	/**
	 * Performs the Exif data extraction
	 * @return the GPS data found in the file
	 */
	public JpegData extract()
	{
		JpegData metadata = new JpegData();
		if (_data==null)
			return metadata;

		// check for the header length
		if (_data.length<=14)
		{
			metadata.addError("Exif data segment must contain at least 14 bytes");
			return metadata;
		}

		// check for the header preamble
		if (!"Exif\0\0".equals(new String(_data, 0, 6)))
		{
			metadata.addError("Exif data segment doesn't begin with 'Exif'");
			return metadata;
		}

		// this should be either "MM" or "II"
		String byteOrderIdentifier = new String(_data, 6, 2);
		if (!setByteOrder(byteOrderIdentifier))
		{
			metadata.addError("Unclear distinction between Motorola/Intel byte ordering: " + byteOrderIdentifier);
			return metadata;
		}

		// Check the next two values are 0x2A as expected
		if (get16Bits(8)!=0x2a)
		{
			metadata.addError("Invalid Exif start - should have 0x2A at offset 8 in Exif header");
			return metadata;
		}

		int firstDirectoryOffset = get32Bits(10) + TIFF_HEADER_START_OFFSET;

		// Check that offset is within range
		if (firstDirectoryOffset>=_data.length - 1)
		{
			metadata.addError("First exif directory offset is beyond end of Exif data segment");
			// First directory normally starts 14 bytes in -- try it here and catch another error in the worst case
			firstDirectoryOffset = 14;
		}

		HashMap<Integer, String> processedDirectoryOffsets = new HashMap<Integer, String>();

		// 0th IFD (we merge with Exif IFD)
		processDirectory(metadata, false, processedDirectoryOffsets, firstDirectoryOffset, TIFF_HEADER_START_OFFSET);

		return metadata;
	}


	/**
	 * Set the byte order identifier
	 * @param byteOrderIdentifier String from exif
	 * @return true if recognised, false otherwise
	 */
	private boolean setByteOrder(String byteOrderIdentifier)
	{
		if ("MM".equals(byteOrderIdentifier)) {
			_isMotorolaByteOrder = true;
		} else if ("II".equals(byteOrderIdentifier)) {
			_isMotorolaByteOrder = false;
		} else {
			return false;
		}
		return true;
	}


	/**
	 * Recursive call to process one of the nested Tiff IFD directories.
	 * 2 bytes: number of tags
	 * for each tag
	 *   2 bytes: tag type
	 *   2 bytes: format code
	 *   4 bytes: component count
	 */
	private void processDirectory(JpegData inMetadata, boolean inIsGPS, HashMap<Integer, String> inDirectoryOffsets,
		int inDirOffset, int inTiffHeaderOffset)
	{
		// check for directories we've already visited to avoid stack overflows when recursive/cyclic directory structures exist
		if (inDirectoryOffsets.containsKey(Integer.valueOf(inDirOffset)))
			return;

		// remember that we've visited this directory so that we don't visit it again later
		inDirectoryOffsets.put(Integer.valueOf(inDirOffset), "processed");

		if (inDirOffset >= _data.length || inDirOffset < 0)
		{
			inMetadata.addError("Ignored directory marked to start outside data segment");
			return;
		}

		// First two bytes in the IFD are the number of tags in this directory
		int dirTagCount = get16Bits(inDirOffset);
		// If no tags, exit without complaint
		if (dirTagCount == 0) return;

		if (!isDirectoryLengthValid(inDirOffset, inTiffHeaderOffset))
		{
			inMetadata.addError("Directory length is not valid");
			return;
		}

		inMetadata.setExifDataPresent();
		// Handle each tag in this directory
		for (int tagNumber = 0; tagNumber<dirTagCount; tagNumber++)
		{
			final int tagOffset = calculateTagOffset(inDirOffset, tagNumber);

			// 2 bytes for the tag type
			final int tagType = get16Bits(tagOffset);

			// 2 bytes for the format code
			final int formatCode = get16Bits(tagOffset + 2);
			if (formatCode < 1 || formatCode > MAX_FORMAT_CODE)
			{
				inMetadata.addError("Invalid format code: " + formatCode);
				continue;
			}

			// 4 bytes dictate the number of components in this tag's data
			final int componentCount = get32Bits(tagOffset + 4);
			if (componentCount < 0)
			{
				inMetadata.addError("Negative component count in EXIF");
				continue;
			}
			// each component may have more than one byte... calculate the total number of bytes
			final int byteCount = componentCount * BYTES_PER_FORMAT[formatCode];
			final int tagValueOffset = calculateTagValueOffset(byteCount, tagOffset, inTiffHeaderOffset);
			if (tagValueOffset < 0 || tagValueOffset > _data.length)
			{
				inMetadata.addError("Illegal pointer offset value in EXIF");
				continue;
			}

			// Check that this tag isn't going to allocate outside the bounds of the data array.
			// This addresses an uncommon OutOfMemoryError.
			if (byteCount < 0 || tagValueOffset + byteCount > _data.length)
			{
				inMetadata.addError("Illegal number of bytes: " + byteCount);
				continue;
			}

			// Calculate the value as an offset for cases where the tag represents a directory
			final int subdirOffset = inTiffHeaderOffset + get32Bits(tagValueOffset);

			// Look in both basic Exif tags (for timestamp, thumbnail) and Gps tags (for lat, long, altitude, timestamp)
			switch (tagType)
			{
				case TAG_EXIF_OFFSET:
					processDirectory(inMetadata, false, inDirectoryOffsets, subdirOffset, inTiffHeaderOffset);
					continue;
				case TAG_INTEROP_OFFSET:
					// ignore
					continue;
				case TAG_GPS_INFO_OFFSET:
					processDirectory(inMetadata, true, inDirectoryOffsets, subdirOffset, inTiffHeaderOffset);
					continue;
				case TAG_MAKER_NOTE:
					// ignore
					continue;
				default:
					// not a known directory, so must just be a normal tag
					if (inIsGPS)
					{
						processGpsTag(inMetadata, tagType, tagValueOffset, componentCount, formatCode);
					}
					else
					{
						processExifTag(inMetadata, tagType, tagValueOffset, componentCount, formatCode);
					}
					break;
			}
		}

		// at the end of each IFD is an optional link to the next IFD
		final int finalTagOffset = calculateTagOffset(inDirOffset, dirTagCount);
		int nextDirectoryOffset = get32Bits(finalTagOffset);
		if (nextDirectoryOffset != 0)
		{
			nextDirectoryOffset += inTiffHeaderOffset;
			if (nextDirectoryOffset>=_data.length)
			{
				// Last 4 bytes of IFD reference another IFD with an address that is out of bounds
				return;
			}
			else if (nextDirectoryOffset < inDirOffset)
			{
				// Last 4 bytes of IFD reference another IFD with an address before the start of this directory
				return;
			}
			// the next directory is of same type as this one
			processDirectory(inMetadata, false, inDirectoryOffsets, nextDirectoryOffset, inTiffHeaderOffset);
		}
	}


	/**
	 * Check if the directory length is valid
	 * @param dirStartOffset start offset for directory
	 * @param tiffHeaderOffset Tiff header offeset
	 * @return true if length is valid
	 */
	private boolean isDirectoryLengthValid(int inDirStartOffset, int inTiffHeaderOffset)
	{
		int dirTagCount = get16Bits(inDirStartOffset);
		int dirLength = (2 + (12 * dirTagCount) + 4);
		if (dirLength + inDirStartOffset + inTiffHeaderOffset >= _data.length)
		{
			// Note: Files that had thumbnails trimmed with jhead 1.3 or earlier might trigger this
			return false;
		}
		return true;
	}


	/**
	 * Process a GPS tag and put the contents in the given metadata
	 * @param inMetadata metadata holding extracted values
	 * @param inTagType tag type (eg latitude)
	 * @param inTagValueOffset start offset in data array
	 * @param inComponentCount component count for tag
	 * @param inFormatCode format code, eg byte
	 */
	private void processGpsTag(JpegData inMetadata, int inTagType, int inTagValueOffset,
		int inComponentCount, int inFormatCode)
	{
		try
		{
			// Only interested in tags latref, lat, longref, lon, altref, alt and gps timestamp
			switch (inTagType)
			{
				case TAG_GPS_LATITUDE_REF:
					inMetadata.setLatitudeRef(readString(inTagValueOffset, inFormatCode, inComponentCount));
					break;
				case TAG_GPS_LATITUDE:
					Rational[] latitudes = readRationalArray(inTagValueOffset, inFormatCode, inComponentCount);
					inMetadata.setLatitude(new double[] {latitudes[0].doubleValue(), latitudes[1].doubleValue(),
						ExifGateway.convertToPositiveValue(latitudes[2].getNumerator(), latitudes[2].getDenominator())});
					break;
				case TAG_GPS_LONGITUDE_REF:
					inMetadata.setLongitudeRef(readString(inTagValueOffset, inFormatCode, inComponentCount));
					break;
				case TAG_GPS_LONGITUDE:
					Rational[] longitudes = readRationalArray(inTagValueOffset, inFormatCode, inComponentCount);
					inMetadata.setLongitude(new double[] {longitudes[0].doubleValue(), longitudes[1].doubleValue(),
						ExifGateway.convertToPositiveValue(longitudes[2].getNumerator(), longitudes[2].getDenominator())});
					break;
				case TAG_GPS_ALTITUDE_REF:
					inMetadata.setAltitudeRef(_data[inTagValueOffset]);
					break;
				case TAG_GPS_ALTITUDE:
					inMetadata.setAltitude(readRational(inTagValueOffset, inFormatCode, inComponentCount).intValue());
					break;
				case TAG_GPS_TIMESTAMP:
					Rational[] times = readRationalArray(inTagValueOffset, inFormatCode, inComponentCount);
					inMetadata.setGpsTimestamp(new int[] {times[0].intValue(), times[1].intValue(), times[2].intValue()});
					break;
				case TAG_GPS_DATESTAMP:
					Rational[] dates = readRationalArray(inTagValueOffset, inFormatCode, inComponentCount);
					if (dates != null) {
						inMetadata.setGpsDatestamp(new int[] {dates[0].intValue(), dates[1].intValue(), dates[2].intValue()});
					}
					else
					{
						// Not in rational array format, but maybe as String?
						String date = readString(inTagValueOffset, inFormatCode, inComponentCount);
						if (date != null && date.length() == 10) {
							inMetadata.setGpsDatestamp(new int[] {Integer.parseInt(date.substring(0, 4)),
								Integer.parseInt(date.substring(5, 7)), Integer.parseInt(date.substring(8))});
						}
					}
					break;
				case TAG_BEARING:
					Rational val = readRational(inTagValueOffset, inFormatCode, inComponentCount);
					if (val != null) {
						inMetadata.setBearing(val.doubleValue());
					}
					break;
				default: // ignore all other tags
			}
		}
		catch (Exception e) {} // ignore and continue
	}


	/**
	 * Process a general Exif tag
	 * @param inMetadata metadata holding extracted values
	 * @param inTagType tag type (eg latitude)
	 * @param inTagValueOffset start offset in data array
	 * @param inComponentCount component count for tag
	 * @param inFormatCode format code, eg byte
	 */
	private void processExifTag(JpegData inMetadata, int inTagType, int inTagValueOffset,
		int inComponentCount, int inFormatCode)
	{
		// Only interested in original timestamp, thumbnail offset and thumbnail length
		if (inTagType == TAG_DATETIME_ORIGINAL) {
			inMetadata.setOriginalTimestamp(readString(inTagValueOffset, inFormatCode, inComponentCount));
		}
		else if (inTagType == TAG_DATETIME_DIGITIZED) {
			inMetadata.setDigitizedTimestamp(readString(inTagValueOffset, inFormatCode, inComponentCount));
		}
		else if (inTagType == TAG_THUMBNAIL_OFFSET) {
			_thumbnailOffset = TIFF_HEADER_START_OFFSET + get16Bits(inTagValueOffset);
			extractThumbnail(inMetadata);
		}
		else if (inTagType == TAG_THUMBNAIL_LENGTH) {
			_thumbnailLength = get16Bits(inTagValueOffset);
			extractThumbnail(inMetadata);
		}
		else if (inTagType == TAG_ORIENTATION) {
			if (inMetadata.getOrientationCode() < 1) {
				inMetadata.setOrientationCode(get16Bits(inTagValueOffset));
			}
		}
	}

	/**
	 * Attempt to extract the thumbnail image
	 */
	private void extractThumbnail(JpegData inMetadata)
	{
		if (_thumbnailOffset > 0 && _thumbnailLength > 0 && inMetadata.getThumbnailImage() == null)
		{
			byte[] thumbnailBytes = new byte[_thumbnailLength];
			System.arraycopy(_data, _thumbnailOffset, thumbnailBytes, 0, _thumbnailLength);
			inMetadata.setThumbnailImage(thumbnailBytes);
		}
	}


	/**
	 * Calculate the tag value offset
	 * @param inByteCount
	 * @param inDirEntryOffset
	 * @param inTiffHeaderOffset
	 * @return new offset
	 */
	private int calculateTagValueOffset(int inByteCount, int inDirEntryOffset, int inTiffHeaderOffset)
	{
		if (inByteCount > 4)
		{
			// If it's bigger than 4 bytes, the dir entry contains an offset.
			// dirEntryOffset must be passed, as some makers (e.g. FujiFilm) incorrectly use an
			// offset relative to the start of the makernote itself, not the TIFF segment.
			final int offsetVal = get32Bits(inDirEntryOffset + 8);
			if (offsetVal + inByteCount > _data.length)
			{
				// Bogus pointer offset and / or bytecount value
				return -1; // signal error
			}
			return inTiffHeaderOffset + offsetVal;
		}
		else
		{
			// 4 bytes or less and value is in the dir entry itself
			return inDirEntryOffset + 8;
		}
	}


	/**
	 * Creates a String from the _data buffer starting at the specified offset,
	 * and ending where byte=='\0' or where length==maxLength.
	 * @param inOffset start offset
	 * @param inFormatCode format code - should be string
	 * @param inMaxLength max length of string
	 * @return contents of tag, or null if format incorrect
	 */
	private String readString(int inOffset, int inFormatCode, int inMaxLength)
	{
		if (inFormatCode != FMT_STRING) return null;
		// Calculate length
		int length = 0;
		while ((inOffset + length)<_data.length
			&& _data[inOffset + length]!='\0'
			&& length < inMaxLength)
		{
			length++;
		}
		return new String(_data, inOffset, length);
	}

	/**
	 * Creates a Rational from the _data buffer starting at the specified offset
	 * @param inOffset start offset
	 * @param inFormatCode format code - should be srational or urational
	 * @param inCount component count - should be 1
	 * @return contents of tag as a Rational object
	 */
	private Rational readRational(int inOffset, int inFormatCode, int inCount)
	{
		// Check the format is a single rational as expected
		if (inFormatCode != FMT_SRATIONAL && inFormatCode != FMT_URATIONAL
			|| inCount != 1) return null;
		return new Rational(get32Bits(inOffset), get32Bits(inOffset + 4));
	}


	/**
	 * Creates a Rational array from the _data buffer starting at the specified offset
	 * @param inOffset start offset
	 * @param inFormatCode format code - should be srational or urational
	 * @param inCount component count - number of components
	 * @return contents of tag as an array of Rational objects
	 */
	private Rational[] readRationalArray(int inOffset, int inFormatCode, int inCount)
	{
		// Check the format is rational as expected
		if (inFormatCode != FMT_SRATIONAL && inFormatCode != FMT_URATIONAL)
			return null;
		// Build array of Rationals
		Rational[] answer = new Rational[inCount];
		for (int i=0; i<inCount; i++)
			answer[i] = new Rational(get32Bits(inOffset + (8 * i)), get32Bits(inOffset + 4 + (8 * i)));
		return answer;
	}


	/**
	 * Determine the offset at which a given InteropArray entry begins within the specified IFD.
	 * @param dirStartOffset the offset at which the IFD starts
	 * @param entryNumber the zero-based entry number
	 */
	private int calculateTagOffset(int dirStartOffset, int entryNumber)
	{
		// add 2 bytes for the tag count
		// each entry is 12 bytes, so we skip 12 * the number seen so far
		return dirStartOffset + 2 + (12 * entryNumber);
	}


	/**
	 * Get a 16 bit value from file's native byte order.  Between 0x0000 and 0xFFFF.
	 */
	private int get16Bits(int offset)
	{
		if (offset<0 || offset+2>_data.length)
			throw new ArrayIndexOutOfBoundsException("attempt to read data outside of exif segment (index "
				+ offset + " where max index is " + (_data.length - 1) + ")");

		if (_isMotorolaByteOrder) {
			// Motorola - MSB first
			return (_data[offset] << 8 & 0xFF00) | (_data[offset + 1] & 0xFF);
		} else {
			// Intel ordering - LSB first
			return (_data[offset + 1] << 8 & 0xFF00) | (_data[offset] & 0xFF);
		}
	}


	/**
	 * Get a 32 bit value from file's native byte order.
	 */
	private int get32Bits(int offset)
	{
		if (offset < 0 || offset+4 > _data.length)
			throw new ArrayIndexOutOfBoundsException("attempt to read data outside of exif segment (index "
				+ offset + " where max index is " + (_data.length - 1) + ")");

		if (_isMotorolaByteOrder)
		{
			// Motorola - MSB first
			return (_data[offset] << 24 & 0xFF000000) |
					(_data[offset + 1] << 16 & 0xFF0000) |
					(_data[offset + 2] << 8 & 0xFF00) |
					(_data[offset + 3] & 0xFF);
		}
		else
		{
			// Intel ordering - LSB first
			return (_data[offset + 3] << 24 & 0xFF000000) |
					(_data[offset + 2] << 16 & 0xFF0000) |
					(_data[offset + 1] << 8 & 0xFF00) |
					(_data[offset] & 0xFF);
		}
	}
}
