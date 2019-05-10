package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of audio clips, using the MediaList superclass
 */
public class AudioList extends MediaList
{
	/**
	 * Empty constructor
	 */
	public AudioList() {
		this(null);
	}

	/**
	 * Constructor
	 * @param inList ArrayList containing audio clip objects
	 */
	private AudioList(ArrayList<MediaObject> inList) {
		super(inList);
	}

	/**
	 * @return clone of list contents
	 */
	public AudioList cloneList()
	{
		if (getNumMedia() == 0) return this;
		ArrayList<MediaObject> listCopy = new ArrayList<MediaObject>();
		listCopy.addAll(_media);
		return new AudioList(listCopy);
	}

	/**
	 * @return the number of audio clips in the list
	 */
	public int getNumAudios() {
		return getNumMedia();
	}

	/**
	 * Add an audio clip to the list
	 * @param inAudio object to add
	 */
	public void addAudio(AudioClip inAudio) {
		addMedia(inAudio);
	}

	/**
	 * Add an audio clip to the list
	 * @param inAudio object to add
	 * @param inIndex index at which to add
	 */
	public void addAudio(AudioClip inAudio, int inIndex) {
		addMedia(inAudio, inIndex);
	}

	/**
	 * Remove the selected audio clip from the list
	 * @param inIndex index number to remove
	 */
	public void deleteAudio(int inIndex) {
		deleteMedia(inIndex);
	}

	/**
	 * Get the index of the given audio clip
	 * @param inAudio object to check
	 * @return index of this object in the list, or -1 if not found
	 */
	public int getAudioIndex(AudioClip inAudio) {
		return getMediaIndex(inAudio);
	}

	/**
	 * Get the Audio object at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified object
	 */
	public AudioClip getAudio(int inIndex) {
		return (AudioClip) getMedia(inIndex);
	}

	/**
	 * @return true if list contains correlated objects
	 */
	public boolean hasCorrelatedAudios() {
		return hasCorrelatedMedia();
	}

	/**
	 * Remove all correlated media from the list
	 */
	public void removeCorrelatedAudios() {
		removeCorrelatedMedia();
	}
}
