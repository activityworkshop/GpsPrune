package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command class to remove (not delete) a single photo
 */
public class RemovePhotoCmd extends Command
{
	private final int _photoIndex;

	public RemovePhotoCmd(int inPhotoIndex) {
		this(null, inPhotoIndex);
	}

	protected RemovePhotoCmd(Command inParent, int inPhotoIndex)
	{
		super(inParent);
		_photoIndex = inPhotoIndex;
	}

	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	protected boolean executeCommand(TrackInfo inInfo)
	{
		try {
			inInfo.getPhotoList().delete(_photoIndex);
			return true;
		}
		catch (IndexOutOfBoundsException obe) {
			return false;
		}
	}

	protected Command makeInverse(TrackInfo inInfo)
	{
		final Photo photo = inInfo.getPhotoList().get(_photoIndex);
		return new InsertPhotoCmd(this, photo, _photoIndex);
	}
}
