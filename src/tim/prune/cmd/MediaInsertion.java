package tim.prune.cmd;

import tim.prune.data.MediaObject;

/**
 * Data holder for inserting photos or inserting audios
 */
public class MediaInsertion implements Comparable<MediaInsertion>
{
	private final MediaObject _media;
	private final int _insertIndex;


	/**
	 * Constructor
	 * @param inMedia media object (Photo or audio) to insert
	 * @param inIndex index of insertion (or -1 to append)
	 */
	public MediaInsertion(MediaObject inMedia, int inIndex)
	{
		_media = inMedia;
		_insertIndex = inIndex;
	}

	public MediaObject getMedia() {
		return _media;
	}

	public int getInsertIndex() {
		return _insertIndex;
	}

	public int compareTo(MediaInsertion inOther) {
		return _insertIndex - inOther._insertIndex;
	}
}
