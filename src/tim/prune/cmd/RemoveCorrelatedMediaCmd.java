package tim.prune.cmd;

import java.util.ArrayList;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command to remove all correlated photos and audio objects
 */
public class RemoveCorrelatedMediaCmd extends Command
{
	public RemoveCorrelatedMediaCmd() {
		super(null);
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		inInfo.getPhotoList().removeCorrelatedMedia();
		inInfo.getAudioList().removeCorrelatedMedia();
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<MediaInsertion> media = new ArrayList<>();
		for (int i=0; i<inInfo.getPhotoList().getCount(); i++)
		{
			Photo photo = inInfo.getPhotoList().get(i);
			if (photo.getDataPoint() != null) {
				media.add(new MediaInsertion(photo, i));
			}
		}
		for (int i=0; i<inInfo.getAudioList().getCount(); i++)
		{
			AudioClip audio = inInfo.getAudioList().get(i);
			if (audio.getDataPoint() != null) {
				media.add(new MediaInsertion(audio, i));
			}
		}
		return new InsertMediaCmd(this, media);
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}
}
