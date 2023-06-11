package tim.prune.cmd;

import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to load photos which already have points connected
 */
public class LoadPhotosWithPointsCmd extends CompoundCommand
{
	public LoadPhotosWithPointsCmd(List<Photo> inPhotos)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		for (Photo photo : inPhotos)
		{
			if (photo.getDataPoint() != null) {
				points.add(photo.getDataPoint());
			}
		}
		addCommand(new AppendMediaCmd(convertToMediaList(inPhotos)));
		if (!points.isEmpty()) {
			addCommand(new AppendRangeCmd(points));
		}
	}

	/**
	 * Convert a list of photos into a list of media objects
	 */
	private List<MediaObject> convertToMediaList(List<Photo> inPhotos) {
		return new ArrayList<>(inPhotos);
	}
}
