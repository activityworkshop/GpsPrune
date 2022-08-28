package tim.prune.gui.map.tile;

/**
 * Container for the results of a TileWorker
 * having processed a tile definition
 */
public class TileBytes
{
	private byte[] _data = null;

	public void addBytes(byte[] inData, int inNumBytes)
	{
		if (inNumBytes > 0)
		{
			final int totalBytes = (_data == null ? 0 : _data.length) + inNumBytes;
			byte[] result = new byte[totalBytes];
			int targetIdx = 0;
			if (_data != null) {
				System.arraycopy(_data, 0, result, 0, _data.length);
				targetIdx = _data.length;
			}
			System.arraycopy(inData, 0, result, targetIdx, inNumBytes);
			_data = result;
		}
	}

	/**
	 * @return true if there are no bytes
	 */
	public boolean isEmpty() {
		return _data == null || _data.length == 0;
	}

	/**
	 * @return byte data
	 */
	public byte[] getData() {
		return _data;
	}
}
