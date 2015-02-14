package tim.prune.drew.jpeg;

import java.io.*;

/**
 * Class to perform read functions of Jpeg files, returning specific file segments
 * Based on Drew Noakes' Metadata extractor at http://drewnoakes.com
 */
public class JpegSegmentReader
{
	/** Start of scan marker */
	private static final byte SEGMENT_SOS = (byte)0xDA;

	/** End of image marker */
	private static final byte MARKER_EOI = (byte)0xD9;

	/** APP0 Jpeg segment identifier -- Jfif data. */
	public static final byte SEGMENT_APP0 = (byte)0xE0;
	/** APP1 Jpeg segment identifier -- where Exif data is kept. */
	public static final byte SEGMENT_APP1 = (byte)0xE1;
	/** APP2 Jpeg segment identifier. */
	public static final byte SEGMENT_APP2 = (byte)0xE2;
	/** APP3 Jpeg segment identifier. */
	public static final byte SEGMENT_APP3 = (byte)0xE3;
	/** APP4 Jpeg segment identifier. */
	public static final byte SEGMENT_APP4 = (byte)0xE4;
	/** APP5 Jpeg segment identifier. */
	public static final byte SEGMENT_APP5 = (byte)0xE5;
	/** APP6 Jpeg segment identifier. */
	public static final byte SEGMENT_APP6 = (byte)0xE6;
	/** APP7 Jpeg segment identifier. */
	public static final byte SEGMENT_APP7 = (byte)0xE7;
	/** APP8 Jpeg segment identifier. */
	public static final byte SEGMENT_APP8 = (byte)0xE8;
	/** APP9 Jpeg segment identifier. */
	public static final byte SEGMENT_APP9 = (byte)0xE9;
	/** APPA Jpeg segment identifier -- can hold Unicode comments. */
	public static final byte SEGMENT_APPA = (byte)0xEA;
	/** APPB Jpeg segment identifier. */
	public static final byte SEGMENT_APPB = (byte)0xEB;
	/** APPC Jpeg segment identifier. */
	public static final byte SEGMENT_APPC = (byte)0xEC;
	/** APPD Jpeg segment identifier -- IPTC data in here. */
	public static final byte SEGMENT_APPD = (byte)0xED;
	/** APPE Jpeg segment identifier. */
	public static final byte SEGMENT_APPE = (byte)0xEE;
	/** APPF Jpeg segment identifier. */
	public static final byte SEGMENT_APPF = (byte)0xEF;
	/** Start Of Image segment identifier. */
	public static final byte SEGMENT_SOI = (byte)0xD8;
	/** Define Quantization Table segment identifier. */
	public static final byte SEGMENT_DQT = (byte)0xDB;
	/** Define Huffman Table segment identifier. */
	public static final byte SEGMENT_DHT = (byte)0xC4;
	/** Start-of-Frame Zero segment identifier. */
	public static final byte SEGMENT_SOF0 = (byte)0xC0;
	/** Jpeg comment segment identifier. */
	public static final byte SEGMENT_COM = (byte)0xFE;

	/** Magic numbers to mark the beginning of all Jpegs */
	private static final int MAGIC_JPEG_BYTE_1 = 0xFF;
	private static final int MAGIC_JPEG_BYTE_2 = 0xD8;


	/**
	 * Obtain the Jpeg segment data from the specified file
	 * @param inFile File to read
	 * @return Jpeg segment data from file
	 * @throws JpegException on file read errors or exif data errors
	 */
	public static JpegSegmentData readSegments(File inFile) throws JpegException
	{
		JpegSegmentData segmentData = new JpegSegmentData();

		BufferedInputStream bStream = null;

		try
		{
			bStream = new BufferedInputStream(new FileInputStream(inFile));
			int offset = 0;
			// first two bytes should be jpeg magic number
			int magic1 = bStream.read() & 0xFF;
			int magic2 = bStream.read() & 0xFF;
			checkMagicNumbers(magic1, magic2);

			offset += 2;
			// Loop around segments found
			do
			{
				// next byte is 0xFF
				byte segmentIdentifier = (byte) (bStream.read() & 0xFF);
				if ((segmentIdentifier & 0xFF) != 0xFF)
				{
					throw new JpegException("expected jpeg segment start identifier 0xFF at offset "
						+ offset + ", not 0x" + Integer.toHexString(segmentIdentifier & 0xFF));
				}
				offset++;
				// next byte is <segment-marker>
				byte thisSegmentMarker = (byte) (bStream.read() & 0xFF);
				offset++;
				// next 2-bytes are <segment-size>: [high-byte] [low-byte]
				byte[] segmentLengthBytes = new byte[2];
				bStream.read(segmentLengthBytes, 0, 2);
				offset += 2;
				int segmentLength = ((segmentLengthBytes[0] << 8) & 0xFF00) | (segmentLengthBytes[1] & 0xFF);
				// segment length includes size bytes, so subtract two
				segmentLength -= 2;
				if (segmentLength > bStream.available())
					throw new JpegException("segment size would extend beyond file stream length");
				else if (segmentLength < 0)
					throw new JpegException("segment size would be less than zero");
				byte[] segmentBytes = new byte[segmentLength];
				bStream.read(segmentBytes, 0, segmentLength);
				offset += segmentLength;
				if ((thisSegmentMarker & 0xFF) == (SEGMENT_SOS & 0xFF))
				{
					// The 'Start-Of-Scan' segment comes last so break out of loop
					break;
				}
				else if ((thisSegmentMarker & 0xFF) == (MARKER_EOI & 0xFF))
				{
					// the 'End-Of-Image' segment - should already have exited by now
					break;
				}
				else
				{
					segmentData.addSegment(thisSegmentMarker, segmentBytes);
				}
				// loop through to the next segment
			}
			while (true);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new JpegException("Jpeg file not found");
		}
		catch (IOException ioe)
		{
			throw new JpegException("IOException processing Jpeg file: " + ioe.getMessage(), ioe);
		}
		finally
		{
			try
			{
				if (bStream != null) {
					bStream.close();
				}
			}
			catch (IOException ioe) {
				throw new JpegException("IOException processing Jpeg file: " + ioe.getMessage(), ioe);
			}
		}
		// Return the result
		return segmentData;
	}


	/**
	 * Helper method that validates the Jpeg file's magic number.
	 * @param inMagic1 first half of magic number
	 * @param inMagic2 second half of magic number
	 * @throws JpegException if numbers do not match magic numbers expected
	 */
	private static void checkMagicNumbers(int inMagic1, int inMagic2) throws JpegException
	{
		if (inMagic1 != MAGIC_JPEG_BYTE_1 || inMagic2 != MAGIC_JPEG_BYTE_2)
		{
			throw new JpegException("not a jpeg file");
		}
	}
}