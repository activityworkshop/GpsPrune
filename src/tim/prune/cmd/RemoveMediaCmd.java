package tim.prune.cmd;

import java.util.ArrayList;
import java.util.List;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command to remove one or more media items (photos and/or audios)
 */
public class RemoveMediaCmd extends Command
{
	protected final ArrayList<MediaObject> _media = new ArrayList<>();


	public RemoveMediaCmd(List<MediaObject> inMedia) {
		this(null, inMedia);
	}

	RemoveMediaCmd(Command inParent, List<MediaObject> inMedia)
	{
		super(inParent);
		if (inMedia != null) {
			_media.addAll(inMedia);
		}
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		for (MediaObject media : _media)
		{
			if (media instanceof Photo) {
				inInfo.getPhotoList().delete((Photo) media);
			}
			else if (media instanceof AudioClip) {
				inInfo.getAudioList().delete((AudioClip) media);
			}
			else {
				return false;
			}
		}
		return !_media.isEmpty();
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<MediaInsertion> inserts = new ArrayList<>();
		for (MediaObject mediaObj : _media)
		{
			if (mediaObj instanceof Photo) {
				Photo photo = (Photo) mediaObj;
				inserts.add(new MediaInsertion(photo, inInfo.getPhotoList().getIndexOf(photo)));
			}
			else if (mediaObj instanceof AudioClip) {
				AudioClip audio = (AudioClip) mediaObj;
				inserts.add(new MediaInsertion(audio, inInfo.getAudioList().getIndexOf(audio)));
			}
		}
		return new InsertMediaCmd(this, inserts);
	}
}
