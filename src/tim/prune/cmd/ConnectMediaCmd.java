package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Connect or disconnect a photo and/or audio to a single point
 */
public class ConnectMediaCmd extends Command
{
	private final DataPoint _point;
	private final Photo _photo;
	private final AudioClip _audio;

	public ConnectMediaCmd(DataPoint inPoint, Photo inPhoto, AudioClip inAudio) {
		this(null, inPoint, inPhoto, inAudio);
	}

	public ConnectMediaCmd(DataPoint inPoint, MediaObject inMedia) {
		this(null, inPoint, inMedia instanceof Photo ? (Photo) inMedia : null,
				inMedia instanceof AudioClip ? (AudioClip) inMedia : null);
	}

	protected ConnectMediaCmd(ConnectMediaCmd inParent, DataPoint inPoint, Photo inPhoto, AudioClip inAudio)
	{
		super(inParent);
		_point = inPoint;
		_photo = inPhoto;
		_audio = inAudio;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.DATA_EDITED | DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_point == null) {
			return false;
		}
		boolean connectPhoto = (_photo != null && _point.getPhoto() == null);
		boolean disconnectPhoto = (_photo == null && _point.getPhoto() != null);
		boolean connectAudio = (_audio != null && _point.getAudio() == null);
		boolean disconnectAudio = (_audio == null && _point.getAudio() != null);
		if (connectPhoto) {
			_point.setPhoto(_photo);
			_photo.setDataPoint(_point);
		}
		else if (disconnectPhoto) {
			_point.getPhoto().setDataPoint(null);
			_point.setPhoto(null);
		}
		if (connectAudio) {
			_point.setAudio(_audio);
			_audio.setDataPoint(_point);
		}
		else if (disconnectAudio) {
			_point.getAudio().setDataPoint(null);
			_point.setAudio(null);
		}
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		Photo photo = (_photo == null ? _point.getPhoto() : null);
		AudioClip audio = (_audio == null ? _point.getAudio() : null);
		return new ConnectMediaCmd(this, _point, photo, audio);
	}
}
