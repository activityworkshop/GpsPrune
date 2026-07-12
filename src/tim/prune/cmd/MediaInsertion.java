package tim.prune.cmd;

import tim.prune.data.MediaObject;

/**
 * Data holder for inserting photos or inserting audios
 */
record MediaInsertion (MediaObject media, int insertIndex) implements Comparable<MediaInsertion>
{
	public int compareTo(MediaInsertion inOther) {
		return insertIndex - inOther.insertIndex;
	}
}
