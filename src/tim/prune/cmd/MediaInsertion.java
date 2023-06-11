package tim.prune.cmd;

import tim.prune.data.MediaObject;

/**
 * Data holder for inserting photos or inserting audios
 */
public class MediaInsertion
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

	/**
	 * Constructor
	 * @param inMedia media object to append
	 */
	public MediaInsertion(MediaObject inMedia) {
		this(inMedia, -1);
	}

	public MediaObject getMedia() {
		return _media;
	}

	public int getInsertIndex() {
		return _insertIndex;
	}
}
