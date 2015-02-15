package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

import tim.prune.DataSubscriber;
import tim.prune.FunctionLibrary;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.AudioClip;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.IntegerRange;
import tim.prune.data.Photo;
import tim.prune.data.Selection;
import tim.prune.data.TrackInfo;

/**
 * Class to hold point details and selection details
 * as a visual component
 */
public class DetailsDisplay extends GenericDisplay
{
	// Point details
	private JLabel _indexLabel = null;
	private JLabel _latLabel = null, _longLabel = null;
	private JLabel _altLabel = null;
	private JLabel _timeLabel = null, _speedLabel = null;
	private JLabel _nameLabel = null, _typeLabel = null;

	// Range details
	private JLabel _rangeLabel = null;
	private JLabel _distanceLabel = null;
	private JLabel _durationLabel = null;
	private JLabel _altRangeLabel = null, _updownLabel = null;
	private JLabel _aveSpeedLabel = null;

	// Photo details
	private JPanel _photoDetailsPanel = null;
	private JLabel _photoLabel = null;
	private PhotoThumbnail _photoThumbnail = null;
	private JLabel _photoTimestampLabel = null;
	private JLabel _photoConnectedLabel = null;
	private JLabel _photoBearingLabel = null;
	private JPanel _rotationButtons = null;

	// Audio details
	private JPanel _audioDetailsPanel = null;
	private JLabel _audioLabel = null;
	private JLabel _audioConnectedLabel = null;
	private JLabel _audioTimestampLabel = null;
	private JLabel _audioLengthLabel = null;
	private JProgressBar _audioProgress = null;
	private JPanel _playAudioPanel = null;

	// Units
	private JComboBox _coordFormatDropdown = null;
	private JComboBox _distUnitsDropdown = null;
	// Formatter
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	// Cached labels
	private static final String LABEL_POINT_SELECTED = I18nManager.getText("details.index.selected") + ": ";
	private static final String LABEL_POINT_LATITUDE = I18nManager.getText("fieldname.latitude") + ": ";
	private static final String LABEL_POINT_LONGITUDE = I18nManager.getText("fieldname.longitude") + ": ";
	private static final String LABEL_POINT_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_POINT_TIMESTAMP = I18nManager.getText("fieldname.timestamp") + ": ";
	private static final String LABEL_POINT_WAYPOINTNAME = I18nManager.getText("fieldname.waypointname") + ": ";
	private static final String LABEL_POINT_WAYPOINTTYPE = I18nManager.getText("fieldname.waypointtype") + ": ";
	private static final String LABEL_RANGE_SELECTED = I18nManager.getText("details.range.selected") + ": ";
	private static final String LABEL_RANGE_DURATION = I18nManager.getText("fieldname.duration") + ": ";
	private static final String LABEL_RANGE_DISTANCE = I18nManager.getText("fieldname.distance") + ": ";
	private static final String LABEL_RANGE_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_RANGE_CLIMB = I18nManager.getText("details.range.climb") + ": ";
	private static final String LABEL_RANGE_DESCENT = ", " + I18nManager.getText("details.range.descent") + ": ";
	private static final String LABEL_AUDIO_FILE = I18nManager.getText("details.audio.file") + ": ";
	private static String LABEL_POINT_ALTITUDE_UNITS = null;
	private static Altitude.Format LABEL_POINT_ALTITUDE_FORMAT = Altitude.Format.NO_FORMAT;


	/**
	 * Constructor
	 * @param inTrackInfo Track info object
	 */
	public DetailsDisplay(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		Font biggerFont = new JLabel().getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);

		// Point details panel
		JPanel pointDetailsPanel = makeDetailsPanel("details.pointdetails", biggerFont);
		_indexLabel = new JLabel(I18nManager.getText("details.nopointselection"));
		pointDetailsPanel.add(_indexLabel);
		_latLabel = new JLabel("");
		pointDetailsPanel.add(_latLabel);
		_longLabel = new JLabel("");
		pointDetailsPanel.add(_longLabel);
		_altLabel = new JLabel("");
		pointDetailsPanel.add(_altLabel);
		_timeLabel = new JLabel("");
		_timeLabel.setMinimumSize(new Dimension(120, 10));
		pointDetailsPanel.add(_timeLabel);
		_speedLabel = new JLabel("");
		pointDetailsPanel.add(_speedLabel);
		_nameLabel = new JLabel("");
		pointDetailsPanel.add(_nameLabel);
		_typeLabel = new JLabel("");
		pointDetailsPanel.add(_typeLabel);
		pointDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// range details panel
		JPanel rangeDetailsPanel = makeDetailsPanel("details.rangedetails", biggerFont);
		_rangeLabel = new JLabel(I18nManager.getText("details.norangeselection"));
		rangeDetailsPanel.add(_rangeLabel);
		_distanceLabel = new JLabel("");
		rangeDetailsPanel.add(_distanceLabel);
		_durationLabel = new JLabel("");
		rangeDetailsPanel.add(_durationLabel);
		_aveSpeedLabel = new JLabel("");
		rangeDetailsPanel.add(_aveSpeedLabel);
		_altRangeLabel = new JLabel("");
		rangeDetailsPanel.add(_altRangeLabel);
		_updownLabel = new JLabel("");
		rangeDetailsPanel.add(_updownLabel);
		rangeDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// photo details panel
		_photoDetailsPanel = makeDetailsPanel("details.photodetails", biggerFont);
		_photoLabel = new JLabel(I18nManager.getText("details.nophoto"));
		_photoDetailsPanel.add(_photoLabel);
		_photoTimestampLabel = new JLabel("");
		_photoTimestampLabel.setMinimumSize(new Dimension(120, 10));
		_photoDetailsPanel.add(_photoTimestampLabel);
		_photoConnectedLabel = new JLabel("");
		_photoDetailsPanel.add(_photoConnectedLabel);
		_photoBearingLabel = new JLabel("");
		_photoDetailsPanel.add(_photoBearingLabel);
		_photoThumbnail = new PhotoThumbnail();
		_photoThumbnail.setVisible(false);
		_photoThumbnail.setPreferredSize(new Dimension(100, 100));
		_photoDetailsPanel.add(_photoThumbnail);
		// Rotate buttons
		JButton rotLeft = makeRotateButton(IconManager.ROTATE_LEFT, FunctionLibrary.FUNCTION_ROTATE_PHOTO_LEFT);
		JButton rotRight = makeRotateButton(IconManager.ROTATE_RIGHT, FunctionLibrary.FUNCTION_ROTATE_PHOTO_RIGHT);
		JButton popup = makeRotateButton(IconManager.SHOW_DETAILS, FunctionLibrary.FUNCTION_PHOTO_POPUP);
		_rotationButtons = new JPanel();
		_rotationButtons.add(rotLeft);
		_rotationButtons.add(rotRight);
		_rotationButtons.add(Box.createHorizontalStrut(10));
		_rotationButtons.add(popup);
		_rotationButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		_rotationButtons.setVisible(false);
		_photoDetailsPanel.add(_rotationButtons);
		_photoDetailsPanel.setVisible(false);

		// audio details panel
		_audioDetailsPanel = makeDetailsPanel("details.audiodetails", biggerFont);
		_audioLabel = new JLabel(I18nManager.getText("details.noaudio"));
		_audioDetailsPanel.add(_audioLabel);
		_audioTimestampLabel = new JLabel("");
		_audioTimestampLabel.setMinimumSize(new Dimension(120, 10));
		_audioDetailsPanel.add(_audioTimestampLabel);
		_audioLengthLabel = new JLabel("");
		_audioDetailsPanel.add(_audioLengthLabel);
		_audioConnectedLabel = new JLabel("");
		_audioDetailsPanel.add(_audioConnectedLabel);
		_audioProgress = new JProgressBar(0, 100);
		_audioProgress.setString(I18nManager.getText("details.audio.playing"));
		_audioProgress.setStringPainted(true);
		_audioProgress.setVisible(false);
		_audioDetailsPanel.add(_audioProgress);
		_playAudioPanel = new JPanel();
		_playAudioPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton playAudio = makeRotateButton(IconManager.PLAY_AUDIO, FunctionLibrary.FUNCTION_PLAY_AUDIO);
		playAudio.addActionListener(new AudioListener(_audioProgress));
		_playAudioPanel.add(playAudio);
		JButton stopAudio = makeRotateButton(IconManager.STOP_AUDIO, FunctionLibrary.FUNCTION_STOP_AUDIO);
		_playAudioPanel.add(stopAudio);
		_playAudioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		_playAudioPanel.setVisible(false);
		_audioDetailsPanel.add(_playAudioPanel);
		_audioDetailsPanel.setVisible(false);

		// add the details panels to the main panel
		mainPanel.add(pointDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(rangeDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(_photoDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(_audioDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);

		// Add format, units selection
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		JLabel coordFormatLabel = new JLabel(I18nManager.getText("details.coordformat") + ": ");
		coordFormatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerPanel.add(coordFormatLabel);
		String[] coordFormats = {I18nManager.getText("units.original"), I18nManager.getText("units.degminsec"),
			I18nManager.getText("units.degmin"), I18nManager.getText("units.deg")};
		_coordFormatDropdown = new JComboBox(coordFormats);
		_coordFormatDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dataUpdated(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_coordFormatDropdown);
		_coordFormatDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel unitsLabel = new JLabel(I18nManager.getText("details.distanceunits") + ": ");
		unitsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerPanel.add(unitsLabel);
		String[] distUnits = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.miles")};
		_distUnitsDropdown = new JComboBox(distUnits);
		if (!Config.getConfigBoolean(Config.KEY_METRIC_UNITS)) {_distUnitsDropdown.setSelectedIndex(1);}
		_distUnitsDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Config.setConfigBoolean(Config.KEY_METRIC_UNITS, _distUnitsDropdown.getSelectedIndex() == 0);
				UpdateMessageBroker.informSubscribers(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_distUnitsDropdown);
		_distUnitsDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(lowerPanel, BorderLayout.SOUTH);
	}


	/**
	 * Notification that Track has been updated
	 * @param inUpdateType byte to specify what has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Update current point data, if any
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		Selection selection = _trackInfo.getSelection();
		if ((inUpdateType | DATA_ADDED_OR_REMOVED) > 0) selection.markInvalid();
		int currentPointIndex = selection.getCurrentPointIndex();
		_speedLabel.setText("");
		Distance.Units distUnits = _distUnitsDropdown.getSelectedIndex()==0?Distance.Units.KILOMETRES:Distance.Units.MILES;
		String distUnitsStr = I18nManager.getText(_distUnitsDropdown.getSelectedIndex()==0?"units.kilometres.short":"units.miles.short");
		String speedUnitsStr = I18nManager.getText(_distUnitsDropdown.getSelectedIndex()==0?"units.kmh":"units.mph");
		if (_track == null || currentPoint == null)
		{
			_indexLabel.setText(I18nManager.getText("details.nopointselection"));
			_latLabel.setText("");
			_longLabel.setText("");
			_altLabel.setText("");
			_timeLabel.setText("");
			_nameLabel.setText("");
			_typeLabel.setText("");
		}
		else
		{
			_indexLabel.setText(LABEL_POINT_SELECTED
				+ (currentPointIndex+1) + " " + I18nManager.getText("details.index.of")
				+ " " + _track.getNumPoints());
			_latLabel.setText(makeCoordinateLabel(LABEL_POINT_LATITUDE, currentPoint.getLatitude(), _coordFormatDropdown.getSelectedIndex()));
			_longLabel.setText(makeCoordinateLabel(LABEL_POINT_LONGITUDE, currentPoint.getLongitude(), _coordFormatDropdown.getSelectedIndex()));
			_altLabel.setText(currentPoint.hasAltitude()?
				(LABEL_POINT_ALTITUDE + currentPoint.getAltitude().getValue() + getAltitudeUnitsLabel(currentPoint.getAltitude().getFormat()))
				:"");
			if (currentPoint.getTimestamp().isValid())
			{
				if (currentPointIndex > 0 && currentPointIndex < (_trackInfo.getTrack().getNumPoints()-1))
				{
					DataPoint prevPoint = _trackInfo.getTrack().getPoint(currentPointIndex - 1);
					DataPoint nextPoint = _trackInfo.getTrack().getPoint(currentPointIndex + 1);
					if (prevPoint.getTimestamp().isValid() && nextPoint.getTimestamp().isValid())
					{
						// use total distance and total time between neighbouring points
						long diff = nextPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp());
						if (diff < 1000 && diff > 0)
						{
							double rads = DataPoint.calculateRadiansBetween(prevPoint, currentPoint) +
								DataPoint.calculateRadiansBetween(currentPoint, nextPoint);
							double dist = Distance.convertRadiansToDistance(rads, distUnits);
							String speed = roundedNumber(3600 * dist / diff) + " " + speedUnitsStr;
							_speedLabel.setText(I18nManager.getText("fieldname.speed") + ": " + speed);
						}
					}
				}
				_timeLabel.setText(LABEL_POINT_TIMESTAMP + currentPoint.getTimestamp().getText());
			}
			else {
				_timeLabel.setText("");
			}
			// Waypoint name
			final String name = currentPoint.getWaypointName();
			if (name != null && !name.equals(""))
			{
				_nameLabel.setText(LABEL_POINT_WAYPOINTNAME + name);
			}
			else _nameLabel.setText("");
			// Waypoint type
			final String type = currentPoint.getFieldValue(Field.WAYPT_TYPE);
			if (type != null && !type.equals("")) {
				_typeLabel.setText(LABEL_POINT_WAYPOINTTYPE + type);
			}
			else _typeLabel.setText("");
		}

		// Update range details
		if (_track == null || !selection.hasRangeSelected())
		{
			_rangeLabel.setText(I18nManager.getText("details.norangeselection"));
			_distanceLabel.setText("");
			_durationLabel.setText("");
			_altRangeLabel.setText("");
			_updownLabel.setText("");
			_aveSpeedLabel.setText("");
		}
		else
		{
			_rangeLabel.setText(LABEL_RANGE_SELECTED
				+ (selection.getStart()+1) + " " + I18nManager.getText("details.range.to")
				+ " " + (selection.getEnd()+1));
			_distanceLabel.setText(LABEL_RANGE_DISTANCE + roundedNumber(selection.getDistance(distUnits)) + " " + distUnitsStr);
			if (selection.getNumSeconds() > 0)
			{
				_durationLabel.setText(LABEL_RANGE_DURATION + DisplayUtils.buildDurationString(selection.getNumSeconds()));
				_aveSpeedLabel.setText(I18nManager.getText("details.range.avespeed") + ": "
					+ roundedNumber(selection.getDistance(distUnits)/selection.getNumSeconds()*3600.0) + " " + speedUnitsStr);
			}
			else {
				_durationLabel.setText("");
				_aveSpeedLabel.setText("");
			}
			String altUnitsLabel = getAltitudeUnitsLabel(selection.getAltitudeFormat());
			IntegerRange altRange = selection.getAltitudeRange();
			if (altRange.getMinimum() >= 0 && altRange.getMaximum() >= 0)
			{
				_altRangeLabel.setText(LABEL_RANGE_ALTITUDE
					+ altRange.getMinimum() + altUnitsLabel + " "
					+ I18nManager.getText("details.altitude.to") + " "
					+ altRange.getMaximum() + altUnitsLabel);
				_updownLabel.setText(LABEL_RANGE_CLIMB + selection.getClimb() + altUnitsLabel
					+ LABEL_RANGE_DESCENT + selection.getDescent() + altUnitsLabel);
			}
			else
			{
				_altRangeLabel.setText("");
				_updownLabel.setText("");
			}
		}
		// show photo details and thumbnail
		_photoDetailsPanel.setVisible(_trackInfo.getPhotoList().getNumPhotos() > 0);
		Photo currentPhoto = _trackInfo.getPhotoList().getPhoto(_trackInfo.getSelection().getCurrentPhotoIndex());
		if ((currentPoint == null || currentPoint.getPhoto() == null) && currentPhoto == null)
		{
			// no photo, hide details
			_photoLabel.setText(I18nManager.getText("details.nophoto"));
			_photoTimestampLabel.setText("");
			_photoConnectedLabel.setText("");
			_photoBearingLabel.setText("");
			_photoThumbnail.setVisible(false);
			_rotationButtons.setVisible(false);
		}
		else
		{
			if (currentPhoto == null) {currentPhoto = currentPoint.getPhoto();}
			_photoLabel.setText(I18nManager.getText("details.photofile") + ": " + currentPhoto.getName());
			_photoTimestampLabel.setText(currentPhoto.hasTimestamp()?(LABEL_POINT_TIMESTAMP + currentPhoto.getTimestamp().getText()):"");
			_photoConnectedLabel.setText(I18nManager.getText("details.media.connected") + ": "
				+ (currentPhoto.getCurrentStatus() == Photo.Status.NOT_CONNECTED ?
					I18nManager.getText("dialog.about.no"):I18nManager.getText("dialog.about.yes")));
			if (currentPhoto.getBearing() >= 0.0 && currentPhoto.getBearing() <= 360.0)
			{
				_photoBearingLabel.setText(I18nManager.getText("details.photo.bearing") + ": "
					+ (int) currentPhoto.getBearing() + " \u00B0");
			}
			else _photoBearingLabel.setText("");
			_photoThumbnail.setVisible(true);
			_photoThumbnail.setPhoto(currentPhoto);
			_rotationButtons.setVisible(true);
			if ((inUpdateType & DataSubscriber.PHOTOS_MODIFIED) > 0) {_photoThumbnail.refresh();}
		}
		_photoThumbnail.repaint();

		// audio details
		_audioDetailsPanel.setVisible(_trackInfo.getAudioList().getNumAudios() > 0);
		AudioClip currentAudio = _trackInfo.getAudioList().getAudio(_trackInfo.getSelection().getCurrentAudioIndex());
		if (currentAudio == null) {
			_audioLabel.setText(I18nManager.getText("details.noaudio"));
			_audioTimestampLabel.setText("");
			_audioLengthLabel.setText("");
			_audioConnectedLabel.setText("");
		}
		else
		{
			_audioLabel.setText(LABEL_AUDIO_FILE + currentAudio.getName());
			_audioTimestampLabel.setText(currentAudio.hasTimestamp()?(LABEL_POINT_TIMESTAMP + currentAudio.getTimestamp().getText()):"");
			int audioLength = currentAudio.getLengthInSeconds();
			_audioLengthLabel.setText(audioLength < 0?"":LABEL_RANGE_DURATION + DisplayUtils.buildDurationString(audioLength));
			_audioConnectedLabel.setText(I18nManager.getText("details.media.connected") + ": "
				+ (currentAudio.getCurrentStatus() == Photo.Status.NOT_CONNECTED ?
					I18nManager.getText("dialog.about.no"):I18nManager.getText("dialog.about.yes")));
		}
		_playAudioPanel.setVisible(currentAudio != null);
	}


	/**
	 * Choose the appropriate altitude units label for the specified format
	 * @param inFormat altitude format
	 * @return language-sensitive string
	 */
	private static String getAltitudeUnitsLabel(Altitude.Format inFormat)
	{
		if (inFormat == LABEL_POINT_ALTITUDE_FORMAT && LABEL_POINT_ALTITUDE_UNITS != null)
			return LABEL_POINT_ALTITUDE_UNITS;
		LABEL_POINT_ALTITUDE_FORMAT = inFormat;
		if (inFormat == Altitude.Format.METRES)
			return " " + I18nManager.getText("units.metres.short");
		return " " + I18nManager.getText("units.feet.short");
	}


	/**
	 * Construct an appropriate coordinate label using the selected format
	 * @param inPrefix prefix of label
	 * @param inCoordinate coordinate
	 * @param inFormat index of format selection dropdown
	 * @return language-sensitive string
	 */
	private static String makeCoordinateLabel(String inPrefix, Coordinate inCoordinate, int inFormat)
	{
		String coord = null;
		switch (inFormat) {
			case 1: // degminsec
				coord = inCoordinate.output(Coordinate.FORMAT_DEG_MIN_SEC); break;
			case 2: // degmin
				coord = inCoordinate.output(Coordinate.FORMAT_DEG_MIN); break;
			case 3: // degrees
				coord = inCoordinate.output(Coordinate.FORMAT_DEG); break;
			default: // just as it was
				coord = inCoordinate.output(Coordinate.FORMAT_NONE);
		}
		// Fix broken degree signs (due to unicode mangling)
		final char brokenDeg = 65533;
		if (coord.indexOf(brokenDeg) >= 0) {
			coord = coord.replaceAll(String.valueOf(brokenDeg), "\u00B0");
		}
		return inPrefix + restrictDP(coord);
	}


	/**
	 * Format a number to a sensible precision
	 * @param inDist distance
	 * @return formatted String
	 */
	private String roundedNumber(double inDist)
	{
		// Set precision of formatter
		int numDigits = 0;
		if (inDist < 1.0)
			numDigits = 3;
		else if (inDist < 10.0)
			numDigits = 2;
		else if (inDist < 100.0)
			numDigits = 1;
		// set formatter
		_distanceFormatter.setMaximumFractionDigits(numDigits);
		_distanceFormatter.setMinimumFractionDigits(numDigits);
		return _distanceFormatter.format(inDist);
	}

	/**
	 * Restrict the given coordinate to a limited number of decimal places for display
	 * @param inCoord coordinate string
	 * @return chopped string
	 */
	private static String restrictDP(String inCoord)
	{
		final int DECIMAL_PLACES = 7;
		if (inCoord == null) return "";
		final int dotPos = Math.max(inCoord.lastIndexOf('.'), inCoord.lastIndexOf(','));
		if (dotPos >= 0) {
			final int chopPos = dotPos + DECIMAL_PLACES;
			if (chopPos < (inCoord.length()-1)) {
				return inCoord.substring(0, chopPos);
			}
		}
		return inCoord;
	}

	/**
	 * Make a details subpanel
	 * @param inNameKey key to use for top label
	 * @param inFont font for top label
	 * @return panel with correct layout, label
	 */
	private static JPanel makeDetailsPanel(String inNameKey, Font inFont)
	{
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
		detailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel detailsLabel = new JLabel(I18nManager.getText(inNameKey));
		detailsLabel.setFont(inFont);
		detailsPanel.add(detailsLabel);
		return detailsPanel;
	}

	/**
	 * Create a little button for rotating the current photo
	 * @param inIcon icon to use (from IconManager)
	 * @param inFunction function to call (from FunctionLibrary)
	 * @return button object
	 */
	private static JButton makeRotateButton(String inIcon, GenericFunction inFunction)
	{
		JButton button = new JButton(IconManager.getImageIcon(inIcon));
		button.setToolTipText(I18nManager.getText(inFunction.getNameKey()));
		button.setMargin(new Insets(0, 2, 0, 2));
		button.addActionListener(new FunctionLauncher(inFunction));
		return button;
	}
}
