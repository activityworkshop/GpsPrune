package tim.prune.cmd;

import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;

/**
 * Holds the photo and/or audio of a single point
 */
public class PointAndMedia
{
	private final DataPoint _point;
	private final Photo _photo;
	private final AudioClip _audio;

	public PointAndMedia(DataPoint inPoint, Photo inPhoto, AudioClip inAudio)
	{
		_point = inPoint;
		_photo = inPhoto;
		_audio = inAudio;
	}

	public PointAndMedia(DataPoint inPoint, MediaObject inMedia)
	{
		_point = inPoint;
		_photo = (inMedia instanceof Photo ? (Photo) inMedia : null);
		_audio = (inMedia instanceof AudioClip ? (AudioClip) inMedia : null);
	}

	public DataPoint getPoint() {
		return _point;
	}

	public Photo getPhoto() {
		return _photo;
	}

	public AudioClip getAudio() {
		return _audio;
	}
}
