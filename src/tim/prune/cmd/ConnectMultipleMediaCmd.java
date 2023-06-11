package tim.prune.cmd;

import java.util.ArrayList;
import java.util.List;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Connect or disconnect multiple photos and/or audios and their corresponding points
 */
public class ConnectMultipleMediaCmd extends Command
{
	private final MediaLinkType _linkType;
	private final ArrayList<PointAndMedia> _data = new ArrayList<>();

	public ConnectMultipleMediaCmd(MediaLinkType inLinkType, List<PointAndMedia> inData) {
		this(null, inLinkType, inData);
	}

	private ConnectMultipleMediaCmd(ConnectMultipleMediaCmd inParent, MediaLinkType inLinkType,
		List<PointAndMedia> inData)
	{
		super(inParent);
		_linkType = inLinkType;
		_data.addAll(inData);
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_data.isEmpty()) {
			return false;
		}
		for (PointAndMedia pointData : _data)
		{
			DataPoint point = pointData.getPoint();
			if (point == null) {
				continue;
			}
			if (_linkType.handlePhotos())
			{
				// Deal with the photo
				Photo photo = pointData.getPhoto();
				if (point.getPhoto() != null) {
					point.getPhoto().setDataPoint(null);
				}
				point.setPhoto(photo);
				if (photo != null) {
					photo.setDataPoint(point);
				}
			}
			if (_linkType.handleAudios())
			{
				// Deal with the audio
				AudioClip audio = pointData.getAudio();
				if (point.getAudio() != null) {
					point.getAudio().setDataPoint(null);
				}
				point.setAudio(audio);
				if (audio != null) {
					audio.setDataPoint(point);
				}
			}
		}
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		ArrayList<PointAndMedia> currentData = new ArrayList<>();
		for (PointAndMedia pointData : _data)
		{
			DataPoint point = pointData.getPoint();
			if (point != null) {
				currentData.add(new PointAndMedia(point, point.getPhoto(), point.getAudio()));
			}
		}
		return new ConnectMultipleMediaCmd(this, _linkType, currentData);
	}
}
