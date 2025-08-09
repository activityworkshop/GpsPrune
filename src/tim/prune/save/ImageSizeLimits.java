package tim.prune.save;

import tim.prune.data.Track;
import tim.prune.data.TrackExtents;

/**
 * Extract image size logic into single place
 */
public abstract class ImageSizeLimits
{
	private static final int MIN_PIXELS_ALLOWED = 100;
	private static final int MAX_PIXELS_ALLOWED = 8000;


	/**
	 * @param inTrack track object
	 * @param inZoom selected zoom level
	 * @return true if the image size is acceptable
	 */
	public static boolean isZoomLevelOk(Track inTrack, int inZoom)
	{
		final int pixelCount = getPixelCount(inTrack, inZoom);
		return pixelCount > MIN_PIXELS_ALLOWED
				&& pixelCount < MAX_PIXELS_ALLOWED;
	}

	/** @return the pixel width / height of the resulting image including border */
	private static int getPixelCount(Track inTrack, int inZoom)
	{
		// Get the extents of the track including a standard (10%) border around the data
		TrackExtents extents = new TrackExtents(inTrack);
		extents.applySquareBorder();

		// Work out how big the final image will be
		final int zoomFactor = 1 << inZoom;
		return (int) (extents.getXRange().getRange() * zoomFactor * 256);
	}
}
