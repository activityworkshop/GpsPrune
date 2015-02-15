package tim.prune.jpeg.drew;

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

	/** APP1 Jpeg segment identifier -- where Exif data is kept. */
	private static final byte SEGMENT_APP1 = (byte)0xE1;

	/** Magic numbers to mark the beginning of all Jpegs */
	private static final int MAGIC_JPEG_BYTE_1 = 0xFF;
	private static final int MAGIC_JPEG_BYTE_2 = 0xD8;


	/**
	 * Get the Exif data segment for the specified file
	 * @param inFile File to read
	 * @return Exif data segment as byte array
	 * @throws JpegException on file read errors or exif data errors
	 */
	public static byte[] readExifSegment(File inFile) throws JpegException
	{
		JpegSegmentData data = readSegments(inFile);
		return data.getSegment(SEGMENT_APP1);
	}


	/**
	 * Obtain the Jpeg segment data from the specified file
	 * @param inFile File to read
	 * @return Jpeg segment data from file
	 * @throws JpegException on file read errors or exif data errors
	 */
	private static JpegSegmentData readSegments(File inFile) throws JpegException
	{
		JpegSegmentData segmentData = new JpegSegmentData();
		BufferedInputStream bStream = null;

		try
		{
			bStream = new BufferedInputStream(new FileInputStream(inFile));
			// first two bytes should be jpeg magic number
			final int magic1 = bStream.read() & 0xFF;
			final int magic2 = bStream.read() & 0xFF;
			if (magic1 != MAGIC_JPEG_BYTE_1 || magic2 != MAGIC_JPEG_BYTE_2) {
				throw new JpegException("not a jpeg file");
			}

			// Loop around segments found
			boolean foundExif = false;
			do
			{
				// next byte is 0xFF
				byte segmentIdentifier = (byte) (bStream.read() & 0xFF);
				if ((segmentIdentifier & 0xFF) != 0xFF)
				{
					throw new JpegException("expected jpeg segment start 0xFF, not 0x"
						+ Integer.toHexString(segmentIdentifier & 0xFF));
				}
				// next byte is <segment-marker>
				byte thisSegmentMarker = (byte) (bStream.read() & 0xFF);
				// next 2-bytes are <segment-size>: [high-byte] [low-byte]
				byte[] segmentLengthBytes = new byte[2];
				bStream.read(segmentLengthBytes, 0, 2);
				int segmentLength = ((segmentLengthBytes[0] << 8) & 0xFF00) | (segmentLengthBytes[1] & 0xFF);
				// segment length includes size bytes, so subtract two
				segmentLength -= 2;
				if (segmentLength > bStream.available())
					throw new JpegException("segment size would extend beyond file stream length");
				else if (segmentLength < 0)
					throw new JpegException("segment size would be less than zero");
				byte[] segmentBytes = new byte[segmentLength];
				int bytesRead = bStream.read(segmentBytes, 0, segmentLength);
				// Bail if not all bytes read in one go - otherwise following sections will be out of step
				if (bytesRead != segmentLength) {
					throw new JpegException("Tried to read " + segmentLength + " bytes but only got " + bytesRead);
				}
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
				// loop through to the next segment if exif hasn't already been found
				foundExif = (thisSegmentMarker == SEGMENT_APP1);
			}
			while (!foundExif);
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
}
