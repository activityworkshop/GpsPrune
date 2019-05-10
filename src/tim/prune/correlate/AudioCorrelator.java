package tim.prune.correlate;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.AudioClip;
import tim.prune.data.AudioList;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.MediaList;
import tim.prune.data.TimeDifference;
import tim.prune.data.Timestamp;
import tim.prune.data.TimestampUtc;
import tim.prune.undo.UndoCorrelateAudios;

/**
 * Class to manage the automatic correlation of audio clips to points
 * which is very similar to the PhotoCorrelator apart from the clip lengths
 */
public class AudioCorrelator extends Correlator
{
	private AudioTimestampSelector _fileTimesSelector = null, _correlTimesSelector = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public AudioCorrelator(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.correlateaudios";
	}

	/** @return type key */
	protected String getMediaTypeKey() {
		return "audio";
	}

	/** @return photo list*/
	protected MediaList getMediaList() {
		return _app.getTrackInfo().getAudioList();
	}


	/**
	 * @return first gui panel including timestamp specification (beginning, middle, end)
	 */
	protected JPanel makeFirstPanel()
	{
		// First panel for timestamp stuff
		JPanel card1 = new JPanel();
		card1.setLayout(new FlowLayout(FlowLayout.CENTER));
		JPanel grid1 = new JPanel();
		grid1.setLayout(new GridLayout(0, 1));
		_fileTimesSelector = new AudioTimestampSelector("dialog.correlate.filetimes", "dialog.correlate.filetimes2");
		grid1.add(_fileTimesSelector);
		_correlTimesSelector = new AudioTimestampSelector("dialog.correlate.correltimes", null);
		grid1.add(_correlTimesSelector);
		card1.add(grid1);
		return card1;
	}


	/**
	 * @return array of boolean flags denoting availability of cards
	 */
	protected boolean[] getCardEnabledFlags()
	{
		boolean[] cards = super.getCardEnabledFlags();
		cards[0] = getAudioLengthAvailability(_app.getTrackInfo().getAudioList());
		return cards;
	}

	/**
	 * @param inAudios AudioList object
	 * @return true if there are any audio lengths available
	 */
	private static boolean getAudioLengthAvailability(AudioList inAudios)
	{
		for (int i=0; i<inAudios.getNumMedia(); i++)
		{
			AudioClip a = inAudios.getAudio(i);
			if (a.getLengthInSeconds() > 0) {return true;}
		}
		return false;
	}

	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inTimeDiff TimeDifference to use for preview
	 * @param inShowWarning true to show warning if all points out of range
	 */
	protected void createPreview(TimeDifference inTimeDiff, boolean inShowWarning)
	{
		TimeDifference timeLimit = parseTimeLimit();
		double angDistLimit = parseDistanceLimit();
		MediaPreviewTableModel model = new MediaPreviewTableModel("dialog.correlate.select.audioname");
		AudioList audios = _app.getTrackInfo().getAudioList();
		// Loop through audios deciding whether to set correlate flag or not
		int numAudios = audios.getNumAudios();
		for (int i=0; i<numAudios; i++)
		{
			AudioClip audio = audios.getAudio(i);
			PointMediaPair pair = getPointPairForMedia(_app.getTrackInfo().getTrack(), audio, inTimeDiff);
			MediaPreviewTableRow row = new MediaPreviewTableRow(pair);
			// Don't try to correlate audios which don't have points either side
			boolean correlateAudio = pair.isValid();
			// Don't select audios which already have a point
			if (audio.getCurrentStatus() != AudioClip.Status.NOT_CONNECTED) {correlateAudio = false;}
			// Check time limits, distance limits
			if (timeLimit != null && correlateAudio) {
				long numSecs = pair.getMinSeconds();
				correlateAudio = (numSecs <= timeLimit.getTotalSeconds());
			}
			if (angDistLimit > 0.0 && correlateAudio)
			{
				final double angDistPair = DataPoint.calculateRadiansBetween(pair.getPointBefore(), pair.getPointAfter());
				double frac = pair.getFraction();
				if (frac > 0.5) {frac = 1 - frac;}
				final double angDistPhoto = angDistPair * frac;
				correlateAudio = (angDistPhoto < angDistLimit);
			}
			// Don't select audios which are already correlated to the same point
			if (pair.getSecondsBefore() == 0L && pair.getPointBefore().isDuplicate(audio.getDataPoint())) {
				correlateAudio = false;
			}
			row.setCorrelateFlag(correlateAudio);
			model.addRow(row);
		}
		_previewTable.setModel(model);
		// Set distance units
		model.setDistanceUnits(getSelectedDistanceUnits());
		// Set column widths
		_previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		final int[] colWidths = {150, 160, 100, 100, 50};
		for (int i=0; i<model.getColumnCount(); i++) {
			_previewTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
		}
		// check if any audios found
		_okButton.setEnabled(model.hasAnySelected());
		if (inShowWarning && !model.hasAnySelected())
		{
			JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.correlate.alloutsiderange"),
				I18nManager.getText(getNameKey()), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @return modified timestamp of specified media object
	 */
	protected Timestamp getMediaTimestamp(MediaObject inMedia)
	{
		Timestamp tstamp = super.getMediaTimestamp(inMedia);
		long mediaMillis = tstamp.getMilliseconds(TimezoneHelper.getSelectedTimezone());
		try {
			AudioClip audio = (AudioClip) inMedia;
			int audioLength = audio.getLengthInSeconds();
			// Each option is worth half the length of the audio clip, so need to divide by 2
			int secsToAdd = audioLength *
				(_correlTimesSelector.getSelectedOption() - _fileTimesSelector.getSelectedOption()) / 2;
			if (audioLength > 0 && secsToAdd != 0)
			{
				mediaMillis += (secsToAdd * 1000L);
				tstamp = new TimestampUtc(mediaMillis);
				// Here we create a Utc timestamp but it's only temporary for the correlation
				// so it will never have to react to timezone changes
			}
		}
		catch (ClassCastException cce) {}
		return tstamp;
	}

	/**
	 * Finish the correlation by modifying the track
	 * and passing the Undo information back to the App
	 */
	protected void finishCorrelation()
	{
		// TODO: Probably should be able to combine this into the Correlator?
		PointMediaPair[] pointPairs = getPointPairs();
		if (pointPairs == null || pointPairs.length <= 0) {return;}

		// begin to construct undo information
		UndoCorrelateAudios undo = new UndoCorrelateAudios(_app.getTrackInfo());
		// loop over Audios
		int arraySize = pointPairs.length;
		int i = 0, numAudios = 0;
		int numPointsToCreate = 0;
		PointMediaPair pair = null;
		for (i=0; i<arraySize; i++)
		{
			pair = pointPairs[i];
			if (pair != null && pair.isValid())
			{
				if (pair.getMinSeconds() == 0L)
				{
					// exact match
					AudioClip pointAudio = pair.getPointBefore().getAudio();
					if (pointAudio == null)
					{
						// photo coincides with audioless point so connect the two
						pair.getPointBefore().setAudio((AudioClip) pair.getMedia());
						pair.getMedia().setDataPoint(pair.getPointBefore());
					}
					else if (pointAudio.equals(pair.getMedia())) {
						// photo is already connected, nothing to do
					}
					else {
						// point is already connected to a different audio, so need to clone point
						numPointsToCreate++;
					}
				}
				else
				{
					// audio time falls between two points, so need to interpolate new one
					numPointsToCreate++;
				}
				numAudios++;
			}
		}
		// Second loop, to create points if necessary
		if (numPointsToCreate > 0)
		{
			// make new array for added points
			DataPoint[] addedPoints = new DataPoint[numPointsToCreate];
			int pointNum = 0;
			DataPoint pointToAdd = null;
			for (i=0; i<arraySize; i++)
			{
				pair = pointPairs[i];
				if (pair != null && pair.isValid())
				{
					pointToAdd = null;
					if (pair.getMinSeconds() == 0L && pair.getPointBefore().getAudio() != null
					 && !pair.getPointBefore().getAudio().equals(pair.getMedia()))
					{
						// clone point
						pointToAdd = pair.getPointBefore().clonePoint();
					}
					else if (pair.getMinSeconds() > 0L)
					{
						// interpolate point
						pointToAdd = DataPoint.interpolate(pair.getPointBefore(), pair.getPointAfter(), pair.getFraction());
					}
					if (pointToAdd != null)
					{
						// link audio to point
						pointToAdd.setAudio((AudioClip) pair.getMedia());
						pair.getMedia().setDataPoint(pointToAdd);
						// set to start of segment so not joined in track
						pointToAdd.setSegmentStart(true);
						// add to point array
						addedPoints[pointNum] = pointToAdd;
						pointNum++;
					}
				}
			}
			// expand track
			_app.getTrackInfo().getTrack().appendPoints(addedPoints);
		}

		// send undo information back to controlling app
		undo.setNumAudiosCorrelated(numAudios);
		_app.completeFunction(undo, ("" + numAudios + " "
			 + (numAudios==1?I18nManager.getText("confirm.correlateaudios.single"):I18nManager.getText("confirm.correlateaudios.multi"))));
		// observers already informed by track update if new points created
		if (numPointsToCreate == 0) {
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		}
	}
}
