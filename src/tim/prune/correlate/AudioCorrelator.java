package tim.prune.correlate;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CorrelateMediaCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.MediaObject;
import tim.prune.data.TimeDifference;
import tim.prune.data.Timestamp;
import tim.prune.data.TimestampUtc;

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

	/** @return the name key */
	public String getNameKey() {
		return "function.correlateaudios";
	}

	/** @return type key */
	protected String getMediaTypeKey() {
		return "audio";
	}

	/** @return audio list*/
	protected MediaList<?> getMediaList() {
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
	private static boolean getAudioLengthAvailability(MediaList<AudioClip> inAudios)
	{
		for (int i=0; i<inAudios.getCount(); i++)
		{
			AudioClip a = inAudios.get(i);
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
		MediaList<AudioClip> audios = _app.getTrackInfo().getAudioList();
		// Loop through audios deciding whether to set correlate flag or not
		int numAudios = audios.getCount();
		for (int i=0; i<numAudios; i++)
		{
			AudioClip audio = audios.get(i);
			PointMediaPair pair = getPointPairForMedia(_app.getTrackInfo().getTrack(), audio, inTimeDiff);
			MediaPreviewTableRow row = new MediaPreviewTableRow(pair);
			// Don't try to correlate audios which don't have points either side
			boolean correlate = pair.isValid();
			// Don't select audios which already have a point
			if (audio.getCurrentStatus() != AudioClip.Status.NOT_CONNECTED) {correlate = false;}
			// Check time limits, distance limits
			if (timeLimit != null && correlate) {
				long numSecs = pair.getMinSeconds();
				correlate = (numSecs <= timeLimit.getTotalSeconds());
			}
			if (angDistLimit > 0.0 && correlate)
			{
				final double angDistPair = DataPoint.calculateRadiansBetween(pair.getPointBefore(), pair.getPointAfter());
				double frac = pair.getFraction();
				if (frac > 0.5) {frac = 1 - frac;}
				final double angDistPhoto = angDistPair * frac;
				correlate = (angDistPhoto < angDistLimit);
			}
			// Don't select audios which are already correlated to the same point
			if (pair.getSecondsBefore() == 0L && pair.getPointBefore().isDuplicate(audio.getDataPoint())) {
				correlate = false;
			}
			row.setCorrelateFlag(correlate);
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
				getName(), JOptionPane.ERROR_MESSAGE);
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
	 * Finish the correlation by creating the appropriate command
	 * and passing it back to the App
	 */
	protected void finishCorrelation()
	{
		PointMediaPair[] pointPairs = getPointPairs();
		if (pointPairs == null || pointPairs.length <= 0) {
			return;
		}

		ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
		ArrayList<PointAndMedia> pointAudioPairs = new ArrayList<>();
		fillListsForCommand(pointPairs, pointsToCreate, pointAudioPairs);

		Command command = new CorrelateMediaCmd(MediaLinkType.LINK_AUDIOS, pointsToCreate, pointAudioPairs);
		command.setDescription(makeUndoText(pointAudioPairs.size()));
		command.setConfirmText(makeConfirmText(pointAudioPairs.size()));
		_app.execute(command);
	}

	static void fillListsForCommand(PointMediaPair[] inPointPairs, List<DataPoint> inPointsToCreate, List<PointAndMedia> inPointAudioPairs)
	{
		for (PointMediaPair pair : inPointPairs)
		{
			if (pair != null && pair.isValid())
			{
				AudioClip audioToLink = (AudioClip) pair.getMedia();
				if (pair.getMinSeconds() == 0L)
				{
					// exact match
					DataPoint point = pair.getPointBefore();
					AudioClip pointAudio = pair.getPointBefore().getAudio();
					if (pointAudio == null && !pointAlreadyBeingConnected(point, inPointAudioPairs))
					{
						// audio coincides with audioless point so connect the two
						inPointAudioPairs.add(new PointAndMedia(point, null, audioToLink));
					}
					else if (pointAudio != null && pointAudio.equals(pair.getMedia())) {
						// audio is already connected, nothing to do
					}
					else
					{
						// point is already connected to a different audio, so need to clone point
						DataPoint pointToAdd = pair.getPointBefore().clonePoint();
						inPointsToCreate.add(pointToAdd);
						inPointAudioPairs.add(new PointAndMedia(pointToAdd, null, audioToLink));
					}
				}
				else
				{
					// audio time falls between two points, so need to interpolate new one
					DataPoint pointToAdd = DataPoint.interpolate(pair.getPointBefore(), pair.getPointAfter(), pair.getFraction());
					pointToAdd.setSegmentStart(true);
					inPointsToCreate.add(pointToAdd);
					inPointAudioPairs.add(new PointAndMedia(pointToAdd, null, audioToLink));
				}
			}
		}
	}
}
