package tim.prune.save.xml;

import java.nio.charset.Charset;

/**
 * Class to collect the bytes from an input stream
 * and turn them into a String
 */
public class ByteBuffer
{
	// Array of bytes
	private byte[] _bytes = new byte[1024];
	// Current position to append
	private int _currPos = 0;
	// Flag for recognising utf8 encoded streams
	private boolean _streamUtf8 = false;
	// Flag for whether system is utf8 or not
	private final boolean _systemUtf8 = XmlUtils.isSystemUtf8();

	/**
	 * Append the given byte to the buffer
	 * @param inB byte to append
	 */
	public void appendByte(byte inB)
	{
		// Resize array if necessary
		if (_currPos >= _bytes.length)
		{
			byte[] bigger = new byte[_bytes.length * 2];
			System.arraycopy(_bytes, 0, bigger, 0, _bytes.length);
			_bytes = bigger;
		}
		// Append byte and increment counter
		_bytes[_currPos] = inB;
		_currPos++;
	}

	/**
	 * Clear the buffer and reset
	 */
	public void clear()
	{
		_currPos = 0;
		// Reduce size back to default if it's got too big
		if (_bytes.length > 5000) {
			_bytes = new byte[1024];
		}
	}

	/**
	 * Set the flag that this stream is encoded with utf8
	 */
	public void setEncodingUtf8() {
		_streamUtf8 = true;
	}

	/**
	 * @return contents of buffer as a String
	 */
	public String toString()
	{
		// Sometimes the encoding of the read file isn't the default encoding of the system
		if (_streamUtf8 && !_systemUtf8)
		{
			return new String(_bytes, 0, _currPos, Charset.forName("UTF-8"));
		}
		// Otherwise just use system encoding
		return new String(_bytes, 0, _currPos);
	}

	/**
	 * Look for the given character sequence in the last characters read
	 * @param inChars sequence to look for
	 * @return true if sequence found
	 */
	public boolean foundSequence(char[] inChars)
	{
		final int numChars = inChars.length;
		if (_currPos < numChars) {return false;}
		for (int i=0; i<numChars; i++)
		{
			char searchChar = inChars[numChars - 1 - i];
			char sourceChar = (char) _bytes[_currPos - 1 - i];
			if (searchChar != sourceChar) {return false;}
		}
		return true;
	}
}
