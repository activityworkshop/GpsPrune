package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command to insert or append a single photo to the list
 */
public class InsertPhotoCmd extends Command
{
	private final Photo _photo;
	private final int _photoIndex;


	/**
	 * Constructor
	 * @param inPhoto photo to append
	 */
	public InsertPhotoCmd(Photo inPhoto) {
		this(null, inPhoto, -1);
	}

	/**
	 * Constructor to make inverse
	 * @param inParent parent command
	 * @param inPhoto photo
	 * @param inPhotoIndex point index of insertion
	 */
	InsertPhotoCmd(Command inParent, Photo inPhoto, int inPhotoIndex)
	{
		super(inParent);
		_photo = inPhoto;
		_photoIndex = inPhotoIndex;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_photo == null) {
			return false;
		}
		if (_photoIndex < 0) {
			inInfo.getPhotoList().add(_photo);
		}
		else {
			inInfo.getPhotoList().add(_photo, _photoIndex);
		}
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		int photoIndex = _photoIndex < 0 ? inInfo.getPhotoList().getCount() : _photoIndex;
		return new RemovePhotoCmd(this, photoIndex);
	}
}
