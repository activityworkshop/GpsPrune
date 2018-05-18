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


/**
 * Provides methods to read specific values from a byte array,
 * with a consistent, checked exception structure for issues.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class ByteArrayReader
{
	private final byte[] _buffer;
	private boolean _isMotorolaByteOrder = true;

	public ByteArrayReader(byte[] buffer)
	{
		if (buffer == null)
			throw new NullPointerException();

		_buffer = buffer;
	}

	public void setMotorolaByteOrder(boolean motorolaByteOrder)
	{
		_isMotorolaByteOrder = motorolaByteOrder;
	}

	public long getLength()
	{
		return _buffer.length;
	}

	protected byte getByte(int index)
	{
		return _buffer[index];
	}

	protected void validateIndex(int index, int bytesRequested) throws ExifException
	{
		if (!isValidIndex(index, bytesRequested))
			throw new ExifException("Invalid index " + index);
	}

	private boolean isValidIndex(int index, int bytesRequested)
	{
		return bytesRequested >= 0
			&& index >= 0
			&& ((long)index + (long)bytesRequested) <= (long)_buffer.length;
	}

	public byte[] getBytes(int index, int count) throws ExifException
	{
		validateIndex(index, count);

		byte[] bytes = new byte[count];
		System.arraycopy(_buffer, index, bytes, 0, count);
		return bytes;
	}

	/**
	 * Returns an unsigned 8-bit int calculated from one byte of data at the specified index.
	 *
	 * @param index position within the data buffer to read byte
	 * @return the 8 bit int value, between 0 and 255
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public short getUInt8(int index) throws ExifException
	{
		validateIndex(index, 1);

		return (short) (getByte(index) & 0xFF);
	}

	/**
	 * Returns a signed 8-bit int calculated from one byte of data at the specified index.
	 *
	 * @param index position within the data buffer to read byte
	 * @return the 8 bit int value, between 0x00 and 0xFF
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public byte getInt8(int index) throws ExifException
	{
		validateIndex(index, 1);

		return getByte(index);
	}

	/**
	 * Returns an unsigned 16-bit int calculated from two bytes of data at the specified index.
	 *
	 * @param index position within the data buffer to read first byte
	 * @return the 16 bit int value, between 0x0000 and 0xFFFF
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public int getUInt16(int index) throws ExifException
	{
		validateIndex(index, 2);

		if (_isMotorolaByteOrder) {
			// Motorola - MSB first
			return (getByte(index    ) << 8 & 0xFF00) |
				   (getByte(index + 1)      & 0xFF);
		} else {
			// Intel ordering - LSB first
			return (getByte(index + 1) << 8 & 0xFF00) |
				   (getByte(index    )      & 0xFF);
		}
	}

	/**
	 * Returns a signed 16-bit int calculated from two bytes of data at the specified index (MSB, LSB).
	 *
	 * @param index position within the data buffer to read first byte
	 * @return the 16 bit int value, between 0x0000 and 0xFFFF
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public short getInt16(int index) throws ExifException
	{
		validateIndex(index, 2);

		if (_isMotorolaByteOrder) {
			// Motorola - MSB first
			return (short) (((short)getByte(index    ) << 8 & (short)0xFF00) |
			                ((short)getByte(index + 1)      & (short)0xFF));
		} else {
			// Intel ordering - LSB first
			return (short) (((short)getByte(index + 1) << 8 & (short)0xFF00) |
			                ((short)getByte(index    )      & (short)0xFF));
		}
	}

	/**
	 * Get a 32-bit unsigned integer from the buffer, returning it as a long.
	 *
	 * @param index position within the data buffer to read first byte
	 * @return the unsigned 32-bit int value as a long, between 0x00000000 and 0xFFFFFFFF
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public long getUInt32(int index) throws ExifException
	{
		validateIndex(index, 4);

		if (_isMotorolaByteOrder) {
			// Motorola - MSB first (big endian)
			return (((long)getByte(index    )) << 24 & 0xFF000000L) |
				   (((long)getByte(index + 1)) << 16 & 0xFF0000L) |
				   (((long)getByte(index + 2)) << 8  & 0xFF00L) |
				   (((long)getByte(index + 3))       & 0xFFL);
		} else {
			// Intel ordering - LSB first (little endian)
			return (((long)getByte(index + 3)) << 24 & 0xFF000000L) |
				   (((long)getByte(index + 2)) << 16 & 0xFF0000L) |
				   (((long)getByte(index + 1)) << 8  & 0xFF00L) |
				   (((long)getByte(index    ))       & 0xFFL);
		}
	}

	/**
	 * Returns a signed 32-bit integer from four bytes of data at the specified index the buffer.
	 *
	 * @param index position within the data buffer to read first byte
	 * @return the signed 32 bit int value, between 0x00000000 and 0xFFFFFFFF
	 * @throws IOException the buffer does not contain enough bytes to service the request, or index is negative
	 */
	public int getInt32(int index) throws ExifException
	{
		validateIndex(index, 4);

		if (_isMotorolaByteOrder) {
			// Motorola - MSB first (big endian)
			return (getByte(index    ) << 24 & 0xFF000000) |
				   (getByte(index + 1) << 16 & 0xFF0000) |
				   (getByte(index + 2) << 8  & 0xFF00) |
				   (getByte(index + 3)       & 0xFF);
		} else {
			// Intel ordering - LSB first (little endian)
			return (getByte(index + 3) << 24 & 0xFF000000) |
				   (getByte(index + 2) << 16 & 0xFF0000) |
				   (getByte(index + 1) << 8  & 0xFF00) |
				   (getByte(index    )       & 0xFF);
		}
	}

	/**
	 * Creates a String from the _data buffer starting at the specified index,
	 * and ending where <code>byte=='\0'</code> or where <code>length==maxLength</code>.
	 *
	 * @param index          The index within the buffer at which to start reading the string.
	 * @param maxLengthBytes The maximum number of bytes to read.  If a zero-byte is not reached within this limit,
	 *                       reading will stop and the string will be truncated to this length.
	 * @return The read string.
	 * @throws IOException The buffer does not contain enough bytes to satisfy this request.
	 */
	public String getNullTerminatedString(int index, int maxLengthBytes) throws ExifException
	{
		// NOTE currently only really suited to single-byte character strings

		byte[] bytes = getBytes(index, maxLengthBytes);

		// Count the number of non-null bytes
		int length = 0;
		while (length < bytes.length && bytes[length] != '\0')
			length++;

		return new String(bytes, 0, length);
	}

	public String getString(int index, int bytesRequested) throws ExifException
	{
		// TODO: validate index
		return new String(getBytes(index, bytesRequested));
	}
}
