/*
 * Copyright 2002-2015 Drew Noakes
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package tim.prune.jpeg.drew;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tim.prune.jpeg.JpegData;

/**
 * Processes TIFF-formatted data, using an ExifTiffHandler
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class TiffProcessor
{
	/**
	 * Processes a TIFF data sequence.
	 *
	 * @param reader the {@link RandomAccessReader} from which the data should be read
	 * @param jpegData the data to populate
	 * @param tiffHeaderOffset the offset within <code>reader</code> at which the TIFF header starts
	 * @throws ExifException if an error occurred during the processing of TIFF data that could
	 *                       not be ignored or recovered from
	 * @throws IOException an error occurred while accessing the required data
	 */
	public static void processTiff(final ByteArrayReader reader, JpegData jpegData,
		final int tiffHeaderOffset) throws ExifException, IOException
	{
		// This must be either "MM" or "II".
		short byteOrderIdentifier = reader.getInt16(tiffHeaderOffset);

		if (byteOrderIdentifier == 0x4d4d) { // "MM"
			reader.setMotorolaByteOrder(true);
		} else if (byteOrderIdentifier == 0x4949) { // "II"
			reader.setMotorolaByteOrder(false);
		} else {
			throw new ExifException("Unclear distinction between Motorola/Intel byte ordering: " + byteOrderIdentifier);
		}


		int firstIfdOffset = reader.getInt32(4 + tiffHeaderOffset) + tiffHeaderOffset;

		// David Ekholm sent a digital camera image that has this problem
		if (firstIfdOffset >= reader.getLength() - 1) {
			//handler.warn("First IFD offset is beyond the end of the TIFF data segment -- trying default offset");
			// First directory normally starts immediately after the offset bytes, so try that
			firstIfdOffset = tiffHeaderOffset + 2 + 2 + 4;
		}

		// Make a handler object to use for the processing
		ExifTiffHandler handler = new ExifTiffHandler(jpegData);

		Set<Integer> processedIfdOffsets = new HashSet<Integer>();
		processDirectory(handler, reader, processedIfdOffsets, firstIfdOffset, tiffHeaderOffset, 0);

		handler.completed(reader, tiffHeaderOffset);
	}

	/**
	 * Processes a TIFF IFD.
	 *
	 * IFD Header:
	 * <ul>
	 *     <li><b>2 bytes</b> number of tags</li>
	 * </ul>
	 * Tag structure:
	 * <ul>
	 *     <li><b>2 bytes</b> tag type</li>
	 *     <li><b>2 bytes</b> format code (values 1 to 12, inclusive)</li>
	 *     <li><b>4 bytes</b> component count</li>
	 *     <li><b>4 bytes</b> inline value, or offset pointer if too large to fit in four bytes</li>
	 * </ul>
	 *
	 * @param handler the {@link ExifTiffHandler} that will coordinate processing and accept read values
	 * @param reader the byte reader from which the data should be read
	 * @param processedIfdOffsets the set of visited IFD offsets, to avoid revisiting the same IFD in an endless loop
	 * @param ifdOffset the offset within <code>reader</code> at which the IFD data starts
	 * @param tiffHeaderOffset the offset within <code>reader</code> at which the TIFF header starts
	 * @param inDirectoryId directory id
	 * @throws IOException an error occurred while accessing the required data
	 */
	private static void processDirectory(final ExifTiffHandler handler,
		final ByteArrayReader reader, final Set<Integer> processedIfdOffsets,
		final int ifdOffset, final int tiffHeaderOffset, int inDirectoryId) throws ExifException
	{
		// check for directories we've already visited to avoid stack overflows when recursive/cyclic directory structures exist
		if (processedIfdOffsets.contains(Integer.valueOf(ifdOffset))) {
			return;
		}

		// remember that we've visited this directory so that we don't visit it again later
		processedIfdOffsets.add(ifdOffset);

		if (ifdOffset >= reader.getLength() || ifdOffset < 0) {
			//handler.error("Ignored IFD marked to start outside data segment");
			return;
		}

		// First two bytes in the IFD are the number of tags in this directory
		int dirTagCount = reader.getUInt16(ifdOffset);

		int dirLength = (2 + (12 * dirTagCount) + 4);
		if (dirLength + ifdOffset > reader.getLength()) {
			//handler.error("Illegally sized IFD");
			return;
		}


		// Handle each tag in this directory
		//
		int invalidTiffFormatCodeCount = 0;
		for (int tagNumber = 0; tagNumber < dirTagCount; tagNumber++)
		{
			final int tagOffset = calculateTagOffset(ifdOffset, tagNumber);

			// 2 bytes for the tag id
			final int childTagId = reader.getUInt16(tagOffset);

			// 2 bytes for the format code
			final int formatCode = reader.getUInt16(tagOffset + 2);
			final int componentSizeInBytes = TiffDataFormat.getComponentSize(formatCode);

			if (componentSizeInBytes == 0)
			{
				// This error suggests that we are processing at an incorrect index and will generate
				// rubbish until we go out of bounds (which may be a while).
				if (++invalidTiffFormatCodeCount > 5) {
					//handler.error("Stopping processing as too many errors seen in TIFF IFD");
					return;
				}
				continue;
			}

			// 4 bytes dictate the number of components in this tag's data
			final int componentCount = reader.getInt32(tagOffset + 4);
			if (componentCount < 0) {
				//handler.error("Negative TIFF tag component count");
				continue;
			}

			final int byteCount = componentCount * componentSizeInBytes;

			final int tagValueOffset;
			if (byteCount > 4)
			{
				// If it's bigger than 4 bytes, the dir entry contains an offset.
				final int offsetVal = reader.getInt32(tagOffset + 8);
				if (offsetVal + byteCount > reader.getLength()) {
					// Bogus pointer offset and / or byteCount value
					//handler.error("Illegal TIFF tag pointer offset");
					continue;
				}
				tagValueOffset = tiffHeaderOffset + offsetVal;
			}
			else {
				// 4 bytes or less and value is in the dir entry itself.
				tagValueOffset = tagOffset + 8;
			}

			if (tagValueOffset < 0 || tagValueOffset > reader.getLength()) {
				//handler.error("Illegal TIFF tag pointer offset");
				continue;
			}

			// Check that this tag isn't going to allocate outside the bounds of the data array.
			// This addresses an uncommon OutOfMemoryError.
			if (byteCount < 0 || tagValueOffset + byteCount > reader.getLength()) {
				//handler.error("Illegal number of bytes for TIFF tag data: " + byteCount);
				continue;
			}

			// Special handling for tags that point to other IFDs
			if (byteCount == 4 && handler.isTagIfdPointer(childTagId)) {
				final int subDirOffset = tiffHeaderOffset + reader.getInt32(tagValueOffset);
				processDirectory(handler, reader, processedIfdOffsets, subDirOffset, tiffHeaderOffset, childTagId);
			}
			else if (handler.isInterestingTag(inDirectoryId, childTagId))
			{
				processTag(handler, childTagId, tagValueOffset, componentCount, formatCode, reader);
			}
		}

		// at the end of each IFD is an optional link to the next IFD
		final int finalTagOffset = calculateTagOffset(ifdOffset, dirTagCount);
		int nextIfdOffset = reader.getInt32(finalTagOffset);
		if (nextIfdOffset != 0)
		{
			nextIfdOffset += tiffHeaderOffset;
			if (nextIfdOffset >= reader.getLength()) {
				// Last 4 bytes of IFD reference another IFD with an address that is out of bounds
				// Note this could have been caused by jhead 1.3 cropping too much
				return;
			}
			else if (nextIfdOffset < ifdOffset) {
				// Last 4 bytes of IFD reference another IFD with an address that is before the start of this directory
				return;
			}

			processDirectory(handler, reader, processedIfdOffsets, nextIfdOffset, tiffHeaderOffset, inDirectoryId);
		}
	}


	/**
	 * Process a single tag value
	 */
	private static void processTag(final ExifTiffHandler handler,
		final int tagId, final int tagValueOffset,
		final int componentCount, final int formatCode,
		final ByteArrayReader reader) throws ExifException
	{
		switch (formatCode)
		{
			case TiffDataFormat.CODE_STRING:
				handler.setString(tagId, reader.getNullTerminatedString(tagValueOffset, componentCount));
				break;
			case TiffDataFormat.CODE_RATIONAL_S:
				if (componentCount == 1) {
					handler.setRational(tagId, new Rational(reader.getInt32(tagValueOffset), reader.getInt32(tagValueOffset + 4)));
				} else if (componentCount > 1) {
					Rational[] array = new Rational[componentCount];
					for (int i = 0; i < componentCount; i++)
						array[i] = new Rational(reader.getInt32(tagValueOffset + (8 * i)), reader.getInt32(tagValueOffset + 4 + (8 * i)));
					handler.setRationalArray(tagId, array);
				}
				break;
			case TiffDataFormat.CODE_RATIONAL_U:
				if (componentCount == 1) {
					handler.setRational(tagId, new Rational(reader.getUInt32(tagValueOffset), reader.getUInt32(tagValueOffset + 4)));
				} else if (componentCount > 1) {
					Rational[] array = new Rational[componentCount];
					for (int i = 0; i < componentCount; i++)
						array[i] = new Rational(reader.getUInt32(tagValueOffset + (8 * i)), reader.getUInt32(tagValueOffset + 4 + (8 * i)));
					handler.setRationalArray(tagId, array);
				}
				break;
			case TiffDataFormat.CODE_INT8_S:
				if (componentCount == 1) {
					handler.setIntegerValue(tagId, reader.getInt8(tagValueOffset));
				}
				break;
			case TiffDataFormat.CODE_INT8_U:
				if (componentCount == 1) {
					handler.setIntegerValue(tagId, reader.getUInt8(tagValueOffset));
				}
				break;
			case TiffDataFormat.CODE_INT16_S:
				if (componentCount == 1) {
					handler.setIntegerValue(tagId, reader.getInt16(tagValueOffset));
				}
				break;
			case TiffDataFormat.CODE_INT16_U:
				if (componentCount == 1) {
					handler.setIntegerValue(tagId, reader.getUInt16(tagValueOffset));
				}
				break;
			case TiffDataFormat.CODE_INT32_S:
				// NOTE 'long' in this case means 32 bit, not 64
				if (componentCount == 1) {
					handler.setIntegerValue(tagId, reader.getInt32(tagValueOffset));
				}
				break;
			case TiffDataFormat.CODE_INT32_U:
				// NOTE 'long' in this case means 32 bit, not 64
				if (componentCount == 1) {
					handler.setRational(tagId, new Rational(reader.getUInt32(tagValueOffset), 1L));
				}
				break;
			case TiffDataFormat.CODE_SINGLE:
			case TiffDataFormat.CODE_DOUBLE:
			case TiffDataFormat.CODE_UNDEFINED:
			default:
				break;
		}
	}

	/**
	 * Determine the offset of a given tag within the specified IFD.
	 *
	 * @param ifdStartOffset the offset at which the IFD starts
	 * @param entryNumber    the zero-based entry number
	 */
	private static int calculateTagOffset(int ifdStartOffset, int entryNumber)
	{
		// Add 2 bytes for the tag count.
		// Each entry is 12 bytes.
		return ifdStartOffset + 2 + (12 * entryNumber);
	}
}
