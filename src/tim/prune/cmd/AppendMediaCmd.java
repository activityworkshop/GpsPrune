package tim.prune.cmd;

import java.util.ArrayList;
import java.util.List;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.MediaList;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command to append one or more photos and/or audios to the lists
 */
public class AppendMediaCmd extends Command
{
	private final ArrayList<MediaObject> _media = new ArrayList<>();


	/**
	 * Constructor
	 * @param inMedia photos/audios to append
	 */
	public AppendMediaCmd(List<MediaObject> inMedia) {
		this(null, inMedia);
	}

	/**
	 * Constructor to make inverse
	 * @param inParent parent command
	 * @param inMedia media
	 */
	AppendMediaCmd(Command inParent, List<MediaObject> inMedia)
	{
		super(inParent);
		_media.addAll(inMedia);
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_media.isEmpty()) {
			return false;
		}
		int numAdded = 0;
		MediaList<Photo> photoList = inInfo.getPhotoList();
		MediaList<AudioClip> audioList = inInfo.getAudioList();
		for (MediaObject media : _media)
		{
			if (media instanceof Photo)
			{
				Photo photo = (Photo) media;
				if (photo != null && !photoList.contains(photo))
				{
					photoList.add(photo);
					numAdded++;
				}
			}
			else if (media instanceof AudioClip)
			{
				AudioClip audio = (AudioClip) media;
				if (audio != null && !audioList.contains(audio))
				{
					audioList.add(audio);
					numAdded++;
				}
			}
		}
		return numAdded > 0;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		return new RemoveMediaCmd(this, _media);
	}
}
