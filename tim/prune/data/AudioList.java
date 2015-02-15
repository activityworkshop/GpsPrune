package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of audio files, using the MediaList superclass
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
	 * @param inList ArrayList containing audio file objects
	 */
	private AudioList(ArrayList<MediaFile> inList) {
		super(inList);
	}

	/**
	 * @return clone of list contents
	 */
	public AudioList cloneList()
	{
		if (getNumMedia() == 0) return this;
		ArrayList<MediaFile> listCopy = new ArrayList<MediaFile>();
		listCopy.addAll(_media);
		return new AudioList(listCopy);
	}

	/**
	 * @return the number of audio files in the list
	 */
	public int getNumAudios() {
		return getNumMedia();
	}

	/**
	 * Add an audio file to the list
	 * @param inAudio object to add
	 */
	public void addAudio(AudioFile inAudio) {
		addMedia(inAudio);
	}

	/**
	 * Add an audio file to the list
	 * @param inAudio object to add
	 * @param inIndex index at which to add
	 */
	public void addAudio(AudioFile inAudio, int inIndex) {
		addMedia(inAudio, inIndex);
	}

	/**
	 * Remove the selected audio file from the list
	 * @param inIndex index number to remove
	 */
	public void deleteAudio(int inIndex) {
		deleteMedia(inIndex);
	}

	/**
	 * Get the index of the given audio file
	 * @param inAudio object to check
	 * @return index of this object in the list, or -1 if not found
	 */
	public int getAudioIndex(AudioFile inAudio) {
		return getMediaIndex(inAudio);
	}

	/**
	 * Get the Audio object at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified object
	 */
	public AudioFile getAudio(int inIndex) {
		return (AudioFile) getMedia(inIndex);
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
