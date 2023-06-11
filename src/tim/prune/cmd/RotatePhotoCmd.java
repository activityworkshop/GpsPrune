package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command class to rotate a photo by 90 degrees
 */
public class RotatePhotoCmd extends Command
{
	private final Photo _photo;
	private final boolean _rotateRight;

	public RotatePhotoCmd(Photo inPhoto, boolean inRotateRight) {
		this(null, inPhoto, inRotateRight);
	}

	private RotatePhotoCmd(RotatePhotoCmd inParent, Photo inPhoto, boolean inRotateRight)
	{
		super(inParent);
		_photo = inPhoto;
		_rotateRight = inRotateRight;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		_photo.rotate(_rotateRight);
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new RotatePhotoCmd(this, _photo, !_rotateRight);
	}
}
