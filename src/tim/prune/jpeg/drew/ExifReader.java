package tim.prune.jpeg.drew;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import tim.prune.jpeg.JpegData;


/**
 * Extracts Exif data from a JPEG header segment
 * Based on Drew Noakes' Metadata extractor at https://drewnoakes.com
 * which in turn is based on code from Jhead http://www.sentex.net/~mwandel/jhead/
 */
public class ExifReader
{
	/** Magic numbers to mark the beginning of all Jpegs */
	private static final int MAGIC_JPEG_BYTE_1 = 0xFF;
	private static final int MAGIC_JPEG_BYTE_2 = 0xD8;

	/** 6-byte preamble before starting the TIFF data. */
	private static final String JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\0\0";

	/** Start of segment marker */
	private static final byte SEGMENT_SOS = (byte) 0xDA;

	/** End of segment marker */
	private static final byte MARKER_EOI = (byte) 0xD9;

	/**
	 * Processes the provided JPEG data, and extracts the specified JPEG segments into a JpegData object.
	 * @param inFile a {@link File} from which the JPEG data will be read.
	 */
	public static JpegData readMetadata(File inFile) throws ExifException
	{
		JpegData jpegData = new JpegData();
		BufferedInputStream bStream = null;

		try
		{
			bStream = new BufferedInputStream(new FileInputStream(inFile));
			byte[] segmentBytes = readSegments(bStream);
			if (segmentBytes != null)
			{
				// Got the bytes for the required segment, now extract the data
				extract(segmentBytes, jpegData);
			}
		}
		catch (IOException ioe) {
			throw new ExifException("IO Exception: " + ioe.getMessage());
		}
		finally
		{
			if (bStream != null) {
				try {
					bStream.close();
				} catch (IOException ioe) {}
			}
		}
		return jpegData;
	}

	/**
	 * Reads the relevant segment and returns the bytes.
	 */
	private static byte[] readSegments(final BufferedInputStream bStream)
		throws ExifException, IOException
	{
		// first two bytes should be JPEG magic number
		final int magic1 = bStream.read() & 0xFF;
		final int magic2 = bStream.read() & 0xFF;
		if (magic1 != MAGIC_JPEG_BYTE_1 || magic2 != MAGIC_JPEG_BYTE_2) {
			throw new ExifException("Jpeg file failed Magic check");
		}

		final Byte segmentTypeByte = (byte)0xE1; // JpegSegmentType.APP1.byteValue;

		do {
			// Find the segment marker. Markers are zero or more 0xFF bytes, followed
			// by a 0xFF and then a byte not equal to 0x00 or 0xFF.

			final short segmentIdentifier = (short) bStream.read();

			// We must have at least one 0xFF byte
			if (segmentIdentifier != 0xFF)
				throw new ExifException("Expected JPEG segment start identifier 0xFF, not 0x" + Integer.toHexString(segmentIdentifier).toUpperCase());

			// Read until we have a non-0xFF byte. This identifies the segment type.
			byte currSegmentType = (byte) bStream.read();
			while (currSegmentType == (byte)0xFF) {
				currSegmentType = (byte) bStream.read();
			}

			if (currSegmentType == 0)
				throw new ExifException("Expected non-zero byte as part of JPEG marker identifier");

			if (currSegmentType == SEGMENT_SOS) {
				// The 'Start-Of-Scan' segment's length doesn't include the image data, instead would
				// have to search for the two bytes: 0xFF 0xD9 (EOI).
				// It comes last so simply return at this point
				return null;
			}

			if (currSegmentType == MARKER_EOI) {
				// the 'End-Of-Image' segment -- this should never be found in this fashion
				return null;
			}

			// next 2-bytes are <segment-size>: [high-byte] [low-byte]
			int segmentLength = (bStream.read() << 8) + bStream.read();
			// segment length includes size bytes, so subtract two
			segmentLength -= 2;

			if (segmentLength < 0)
				throw new ExifException("JPEG segment size would be less than zero");

			byte[] segmentBytes = new byte[segmentLength];
			int bytesRead = bStream.read(segmentBytes, 0, segmentLength);
			// Bail if not all bytes read in one go - otherwise following sections will be out of step
			if (bytesRead != segmentLength) {
				throw new ExifException("Tried to read " + segmentLength + " bytes but only got " + bytesRead);
			}
			// Check whether we are interested in this segment
			if (segmentTypeByte == currSegmentType)
			{
				// Pass the appropriate byte arrays to reader.
				if (canProcess(segmentBytes)) {
					return segmentBytes;
				}
			}

		} while (true);
	}

	private static boolean canProcess(final byte[] segmentBytes)
	{
		return segmentBytes.length >= JPEG_EXIF_SEGMENT_PREAMBLE.length() && new String(segmentBytes, 0, JPEG_EXIF_SEGMENT_PREAMBLE.length()).equalsIgnoreCase(JPEG_EXIF_SEGMENT_PREAMBLE);
	}

	/**
	 * Given the bytes, parse them recursively to fill the JpegData
	 * @param segmentBytes bytes out of the file
	 * @param jdata jpeg data to be populated
	 */
	private static void extract(final byte[] segmentBytes, final JpegData jdata)
	{
		if (segmentBytes == null)
			throw new NullPointerException("segmentBytes cannot be null");

		try
		{
			ByteArrayReader reader = new ByteArrayReader(segmentBytes);

			// Check for the header preamble
			try {
				if (!reader.getString(0, JPEG_EXIF_SEGMENT_PREAMBLE.length()).equals(JPEG_EXIF_SEGMENT_PREAMBLE)) {
					// TODO what to do with this error state?
					System.err.println("Invalid JPEG Exif segment preamble");
					return;
				}
			} catch (ExifException e) {
				// TODO what to do with this error state?
				e.printStackTrace(System.err);
				return;
			}

			// Read the TIFF-formatted Exif data
			TiffProcessor.processTiff(
				reader,
				jdata,
				JPEG_EXIF_SEGMENT_PREAMBLE.length()
			);

		} catch (ExifException e) {
			// TODO what to do with this error state?
			e.printStackTrace(System.err);
		} catch (IOException e) {
			// TODO what to do with this error state?
			e.printStackTrace(System.err);
		}
	}
}
