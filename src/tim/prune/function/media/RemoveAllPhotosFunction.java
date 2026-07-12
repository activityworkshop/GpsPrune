package tim.prune.function.media;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.ConnectMultipleMediaCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.RemoveMediaCmd;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;


/**
 * Function to remove all photos
 */
public class RemoveAllPhotosFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public RemoveAllPhotosFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.removeallphotos";
	}

	/**
	 * Remove all the photos, and if any have points then either delete or unlink them
	 */
	public void begin()
	{
		final Command removeCommand = makeCommand(_app.getTrackInfo(), shouldDeletePoints());
		_app.execute(removeCommand);
	}

	/** @return an appropriate Command depending on the data and the response from the user */
	static Command makeCommand(TrackInfo inTrackInfo, PopupResponse inResponse)
	{
		final MediaList<Photo> photoList = inTrackInfo.getPhotoList();
		switch (inResponse)
		{
			case MEDIA_NOT_CONNECTED:
				// Just remove the photos, none of them are connected
				return makeRemoveMediaCmd(photoList, false);
			case UNLINK:
				// Remove the photos and unlink the points
				return makeRemoveAndUnlinkCmd(photoList);
			case DELETE:
				// Remove the photos, delete the points, unlink the points from their audios if necessary
				return makeRemoveAndDeleteCmd(inTrackInfo, photoList);
			case CANCEL:
			default:
				return null;
		}
	}

	/** @return a command to remove all the unconnected photos */
	private static RemoveMediaCmd makeRemoveMediaCmd(MediaList<Photo> inPhotoList, boolean inAlsoRemoveAudio)
	{
		final ArrayList<MediaObject> mediaToRemove = new ArrayList<>();
		final int numPhotos = inPhotoList.getCount();
		for (int i=0; i<numPhotos; i++)
		{
			final Photo photo = inPhotoList.get(i);
			mediaToRemove.add(photo);
			if (inAlsoRemoveAudio
					&& photo.getDataPoint() != null
					&& photo.getDataPoint().getAudio() != null)
			{
				mediaToRemove.add(photo.getDataPoint().getAudio());
			}
		}
		RemoveMediaCmd command = new RemoveMediaCmd(mediaToRemove);
		command.setDescription(I18nManager.getTextWithNumber("undo.removeallphotos", numPhotos));
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.allphotos.removed", numPhotos));
		return command;
	}

	/** @return a command to remove all photos and disconnect them if necessary */
	private static Command makeRemoveAndUnlinkCmd(MediaList<Photo> inPhotoList)
	{
		CompoundCommand command = new CompoundCommand(DataSubscriber.DATA_EDITED | DataSubscriber.MEDIA_MODIFIED);
		command.addCommand(makeRemoveMediaCmd(inPhotoList, false));
		command.addCommand(makeUnlinkCommand(inPhotoList));
		final int numPhotos = inPhotoList.getCount();
		command.setDescription(I18nManager.getTextWithNumber("undo.removeallphotos", numPhotos));
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.allphotos.removed", numPhotos));
		return command;
	}

	/** @return a command to unlink all the connected photos */
	private static Command makeUnlinkCommand(MediaList<Photo> inPhotoList)
	{
		ArrayList<PointAndMedia> mediaList = new ArrayList<>();
		final int numPhotos = inPhotoList.getCount();
		for (int i=0; i<numPhotos; i++)
		{
			final Photo photo = inPhotoList.get(i);
			final DataPoint point = photo.getDataPoint();
			if (point != null) {
				mediaList.add(new PointAndMedia(point, null, null));
			}
		}
		return new ConnectMultipleMediaCmd(MediaLinkType.LINK_PHOTOS, mediaList);
	}

	/** @return a command to remove all photos and delete the associated points */
	private static Command makeRemoveAndDeleteCmd(TrackInfo inTrackInfo, MediaList<Photo> inPhotoList)
	{
		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		List<PointFlag> segmentBreaks = new ArrayList<>();

		final Track track = inTrackInfo.getTrack();
		final int numPoints = track.getNumPoints();
		boolean deletedPrevious = false;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.getPhoto() == null)
			{
				indexesToKeep.add(i);
				if (deletedPrevious && !point.getSegmentStart()) {
					segmentBreaks.add(new PointFlag(point, true));
				}
				deletedPrevious = false;
			}
			else {
				indexesToDelete.add(i);
				deletedPrevious = point.getSegmentStart();
			}
		}
		final CompoundCommand command = new ShuffleAndCropCmd(indexesToKeep, indexesToDelete, segmentBreaks);
		command.addCommand(makeRemoveMediaCmd(inPhotoList, true));
		final int numPhotos = inPhotoList.getCount();
		command.setDescription(I18nManager.getTextWithNumber("undo.removeallphotos", numPhotos));
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.allphotos.removed", numPhotos));
		return command;
	}

	/**
	 * Determine whether to delete the connected point(s), just unlink, or cancel
	 * @return decision what do to with the connected points
	 */
	private PopupResponse shouldDeletePoints()
	{
		if (!hasAnyConnectedPhotos()) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		final boolean hasAudio = hasPhotosWithAudio();
		final String message = hasAudio ?
			I18nManager.getText("dialog.removeallphotos.deletepointsandaudios")
			: I18nManager.getText("dialog.removeallphotos.deletepoints");

		int response = JOptionPane.showConfirmDialog(_app.getFrame(), message, getName(),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return (response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK);
	}

	/** @return true if there is at least one photo which is connected to a point */
	private boolean hasAnyConnectedPhotos() {
		return _app.getTrackInfo().getPhotoList().hasCorrelatedMedia();
	}

	/** @return true if there is at least one photo which is attached to an audio clip */
	private boolean hasPhotosWithAudio()
	{
		final MediaList<Photo> photoList = _app.getTrackInfo().getPhotoList();
		final int numPhotos = photoList.getCount();
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = photoList.get(i);
			if (photo.getDataPoint() != null && photo.getDataPoint().getAudio() != null) {
				return true;
			}
		}
		return false;
	}
}
