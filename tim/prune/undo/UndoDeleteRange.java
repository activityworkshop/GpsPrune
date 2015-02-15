package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.AudioList;
import tim.prune.data.DataPoint;
import tim.prune.data.PhotoList;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a range of points
 */
public class UndoDeleteRange extends UndoDeleteOperation
{
	/**
	 * Inner class to hold a single range information set
	 */
	static class RangeInfo
	{
		public int _startIndex = -1;
		public DataPoint[] _points = null;
		public DataPoint _nextTrackPoint = null;
		public boolean _segmentStart = false;

		/**
		 * @param inPoint next track point after deleted section, or null
		 */
		public void setNextTrackPoint(DataPoint inPoint)
		{
			_nextTrackPoint = inPoint;
			if (inPoint != null) {
				_segmentStart = inPoint.getSegmentStart();
			}

		}

		/**
		 * @return true if the range is valid
		 */
		public boolean isValid()
		{
			return _startIndex >= 0 && _points != null && _points.length > 0;
		}

		/**
		 * @return end index of range
		 */
		public int getEndIndex()
		{
			return _startIndex + _points.length - 1;
		}
	}


	// Instance variables for UndoDeleteRange
	private RangeInfo _rangeInfo1 = null;
	private RangeInfo _rangeInfo2 = null;
	private PhotoList _photoList = null;
	private AudioList _audioList = null;
	private String _nameKey = null;
	private int _totalDeleted = 0;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 * @param inNameKey key to use for undo text
	 * @param inStartIndex1 start index of first deleted segment
	 * @param inDeleteMedias1 flags to delete media for range1
	 * @param inStartIndex2 start index of second segment
	 * @param inDeleteMedias2 flags to delete media for range2
	 */
	public UndoDeleteRange(TrackInfo inTrackInfo, String inNameKey,
		int inStartIndex1, boolean[] inDeleteMedias1,
		int inStartIndex2, boolean[] inDeleteMedias2)
	{
		_nameKey = inNameKey;
		boolean mediaDeleted = false;
		_totalDeleted = 0;
		// Check if there's a valid first range
		if (inStartIndex1 >= 0 && inDeleteMedias1 != null)
		{
			final int numPoints = inDeleteMedias1.length;
			if (numPoints > 0)
			{
				_totalDeleted += numPoints;
				_rangeInfo1 = new RangeInfo();
				_rangeInfo1._startIndex = inStartIndex1;

				for (int i=0; i<numPoints && !mediaDeleted; i++) {
					if (inDeleteMedias1[i]) mediaDeleted = true;
				}
				// Clone points
				_rangeInfo1._points = inTrackInfo.getTrack().cloneRange(inStartIndex1, inStartIndex1 + numPoints - 1);
				// Save segment flag of following track point
				_rangeInfo1.setNextTrackPoint(inTrackInfo.getTrack().getNextTrackPoint(inStartIndex1 + numPoints));
			}
		}
		// And the same for the second range, if any
		if (inStartIndex2 >= 0 && inDeleteMedias2 != null)
		{
			final int numPoints = inDeleteMedias2.length;
			if (numPoints > 0)
			{
				_totalDeleted += numPoints;
				_rangeInfo2 = new RangeInfo();
				_rangeInfo2._startIndex = inStartIndex2;
				for (int i=0; i<numPoints && !mediaDeleted; i++) {
					if (inDeleteMedias2[i]) mediaDeleted = true;
				}

				// Clone points
				_rangeInfo2._points = inTrackInfo.getTrack().cloneRange(inStartIndex2, inStartIndex2 + numPoints - 1);
				// Save segment flag of following track point
				_rangeInfo2.setNextTrackPoint(inTrackInfo.getTrack().getNextTrackPoint(inStartIndex2 + numPoints));
			}
		}
		// If any media have been deleted, then the lists must be copied
		if (mediaDeleted)
		{
			_photoList = inTrackInfo.getPhotoList().cloneList();
			_audioList = inTrackInfo.getAudioList().cloneList();
		}
	}


	/**
	 * @return description of operation including number of points deleted
	 */
	public String getDescription()
	{
		return I18nManager.getText(_nameKey) + " (" + _totalDeleted + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo)
	{
		// restore photos and audios to how they were before
		if (_photoList != null) {
			inTrackInfo.getPhotoList().restore(_photoList);
		}
		if (_audioList != null) {
			inTrackInfo.getAudioList().restore(_audioList);
		}

		// Undo both the ranges
		performUndo(inTrackInfo, _rangeInfo1);
		performUndo(inTrackInfo, _rangeInfo2);
		// If there's a current point/range selected, maybe need to adjust start and/or end
		if (_rangeInfo1 != null && _rangeInfo1.isValid()) {
			modifySelection(inTrackInfo, _rangeInfo1._startIndex, _rangeInfo1.getEndIndex());
		}
		if (_rangeInfo2 != null && _rangeInfo2.isValid()) {
			modifySelection(inTrackInfo, _rangeInfo2._startIndex, _rangeInfo2.getEndIndex());
		}
	}

	/**
	 * Perform the undo on a single deleted range
	 * @param inTrackInfo track info object
	 * @param inRangeInfo info object describing deleted range
	 */
	private void performUndo(TrackInfo inTrackInfo, RangeInfo inRangeInfo)
	{
		if (inRangeInfo == null || !inRangeInfo.isValid()) return;

		// reconnect photos and audios to points
		final int numPoints = inRangeInfo._points.length;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inRangeInfo._points[i];
			if (point != null && point.hasMedia())
			{
				if (point.getPhoto() != null) {
					point.getPhoto().setDataPoint(point);
				}
				if (point.getAudio() != null) {
					point.getAudio().setDataPoint(point);
				}
			}
		}
		// restore point array into track
		inTrackInfo.getTrack().insertRange(inRangeInfo._points, inRangeInfo._startIndex);
		// Restore segment flag of following track point
		if (inRangeInfo._nextTrackPoint != null) {
			inRangeInfo._nextTrackPoint.setSegmentStart(inRangeInfo._segmentStart);
		}
	}
}
