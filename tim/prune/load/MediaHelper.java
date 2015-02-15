package tim.prune.load;

import java.io.File;

import tim.prune.data.AudioFile;
import tim.prune.data.MediaFile;
import tim.prune.data.MediaList;
import tim.prune.data.Photo;
import tim.prune.data.Track;

/**
 * Class to provide helper functions for loading media
 */
public abstract class MediaHelper
{
	/** File filters */
	private static GenericFileFilter _jpegFilter = null, _audioFilter = null;

	/**
	 * Construct a MediaFile object for the given path
	 * @param inPath path to file
	 * @return either Photo or AudioFile object as appropriate, or null
	 */
	public static MediaFile createMediaFile(String inPath)
	{
		if (inPath == null) {return null;}
		File file = new File(inPath);
		if (!file.exists() || !file.canRead() || !file.isFile()) {return null;}
		// Initialise filters if necessary
		if (_jpegFilter == null) {
			_jpegFilter = new JpegFileFilter();
			_audioFilter = new AudioFileFilter();
		}
		// Check if filename looks like a jpeg
		if (_jpegFilter.acceptFilename(file.getName())) {
			return JpegLoader.createPhoto(file);
		}
		// Check if filename looks like an audio file
		if (_audioFilter.acceptFilename(file.getName())) {
			return new AudioFile(file);
		}
		// Neither photo nor audio
		return null;
	}

	/**
	 * Add all the media from the given track into the specified list
	 * @param inTrack track from which media to take
	 * @param inMediaList list to which media should be added
	 * @param inMediaClass class of media, either Photo or AudioFile
	 */
	public static void addMediaFromTrack(Track inTrack, MediaList inMediaList,
		Class<?> inMediaClass)
	{
		final int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			MediaFile media = null;
			if (inMediaClass == Photo.class) {
				media = inTrack.getPoint(i).getPhoto();
			}
			else if (inMediaClass == AudioFile.class) {
				media = inTrack.getPoint(i).getAudio();
			}
			if (media != null) {
				inMediaList.addMedia(media);
			}
		}
	}
}
