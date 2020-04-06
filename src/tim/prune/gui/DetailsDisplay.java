package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

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
import tim.prune.config.TimezoneHelper;
import tim.prune.data.AltitudeRange;
import tim.prune.data.AudioClip;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Photo;
import tim.prune.data.Selection;
import tim.prune.data.SourceInfo;
import tim.prune.data.SpeedCalculator;
import tim.prune.data.SpeedValue;
import tim.prune.data.TrackInfo;
import tim.prune.data.Unit;
import tim.prune.data.UnitSet;
import tim.prune.data.UnitSetLibrary;

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
	private JLabel _ptDateLabel = null, _ptTimeLabel = null;
	private JLabel _descLabel = null;
	private JLabel _speedLabel = null, _vSpeedLabel = null;
	private JLabel _nameLabel = null, _typeLabel = null;
	private JLabel _filenameLabel = null;

	// Range details
	private JLabel _rangeLabel = null;
	private JLabel _distanceLabel = null;
	private JLabel _durationLabel = null;
	private JLabel _altRangeLabel = null, _updownLabel = null;
	private JLabel _aveSpeedLabel = null;

	// Photo details
	private JPanel _photoDetailsPanel = null;
	private JLabel _photoLabel = null;
	private JLabel _photoPathLabel = null;
	private PhotoThumbnail _photoThumbnail = null;
	private JLabel _photoTimestampLabel = null;
	private JLabel _photoConnectedLabel = null;
	private JLabel _photoBearingLabel = null;
	private JPanel _rotationButtons = null;

	// Audio details
	private JPanel _audioDetailsPanel = null;
	private JLabel _audioLabel = null;
	private JLabel _audioPathLabel = null;
	private JLabel _audioConnectedLabel = null;
	private JLabel _audioTimestampLabel = null;
	private JLabel _audioLengthLabel = null;
	private JProgressBar _audioProgress = null;
	private JPanel _playAudioPanel = null;

	// Units
	private JComboBox<String> _coordFormatDropdown = null;
	private JComboBox<String> _distUnitsDropdown = null;
	// Timezone
	private TimeZone _timezone = null;

	// Cached labels
	private static final String LABEL_POINT_SELECTED = I18nManager.getText("details.index.selected") + ": ";
	private static final String LABEL_POINT_LATITUDE = I18nManager.getText("fieldname.latitude") + ": ";
	private static final String LABEL_POINT_LONGITUDE = I18nManager.getText("fieldname.longitude") + ": ";
	private static final String LABEL_POINT_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_POINT_DATE     = I18nManager.getText("fieldname.date") + ": ";
	private static final String LABEL_POINT_TIME     = I18nManager.getText("fieldname.timestamp") + ": ";
	private static final String LABEL_POINT_WAYPOINTNAME = I18nManager.getText("fieldname.waypointname") + ": ";
	private static final String LABEL_POINT_WAYPOINTTYPE = I18nManager.getText("fieldname.waypointtype") + ": ";
	private static final String LABEL_POINT_DESCRIPTION  = I18nManager.getText("fieldname.description") + ": ";
	private static final String LABEL_POINT_SPEED        = I18nManager.getText("fieldname.speed") + ": ";
	private static final String LABEL_POINT_VERTSPEED    = I18nManager.getText("fieldname.verticalspeed") + ": ";
	private static final String LABEL_POINT_FILENAME     = I18nManager.getText("details.track.file") + ": ";
	private static final String LABEL_RANGE_SELECTED = I18nManager.getText("details.range.selected") + ": ";
	private static final String LABEL_RANGE_DURATION = I18nManager.getText("fieldname.duration") + ": ";
	private static final String LABEL_RANGE_DISTANCE = I18nManager.getText("fieldname.distance") + ": ";
	private static final String LABEL_RANGE_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_RANGE_CLIMB = I18nManager.getText("details.range.climb") + ": ";
	private static final String LABEL_RANGE_DESCENT = ", " + I18nManager.getText("details.range.descent") + ": ";
	private static final String LABEL_AUDIO_FILE = I18nManager.getText("details.audio.file") + ": ";
	private static final String LABEL_FULL_PATH = I18nManager.getText("details.media.fullpath") + ": ";


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
		_ptDateLabel = new JLabel("");
		_ptDateLabel.setMinimumSize(new Dimension(120, 10));
		pointDetailsPanel.add(_ptDateLabel);
		_ptTimeLabel = new JLabel("");
		_ptTimeLabel.setMinimumSize(new Dimension(120, 10));
		pointDetailsPanel.add(_ptTimeLabel);
		_descLabel = new JLabel("");
		pointDetailsPanel.add(_descLabel);
		_speedLabel = new JLabel("");
		pointDetailsPanel.add(_speedLabel);
		_vSpeedLabel = new JLabel("");
		pointDetailsPanel.add(_vSpeedLabel);
		_nameLabel = new JLabel("");
		pointDetailsPanel.add(_nameLabel);
		_typeLabel = new JLabel("");
		pointDetailsPanel.add(_typeLabel);
		_filenameLabel = new JLabel("");
		pointDetailsPanel.add(_filenameLabel);
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
		_photoPathLabel = new JLabel("");
		_photoDetailsPanel.add(_photoPathLabel);
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
		_audioPathLabel = new JLabel("");
		_audioDetailsPanel.add(_audioPathLabel);
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
		_coordFormatDropdown = new JComboBox<String>(coordFormats);
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
		// Make dropdown for distance units
		_distUnitsDropdown = new JComboBox<String>();
		final UnitSet currUnits = Config.getUnitSet();
		for (int i=0; i<UnitSetLibrary.getNumUnitSets(); i++) {
			_distUnitsDropdown.addItem(I18nManager.getText(UnitSetLibrary.getUnitSet(i).getDistanceUnit().getNameKey()));
			if (UnitSetLibrary.getUnitSet(i) == currUnits) {_distUnitsDropdown.setSelectedIndex(i);}
		}
		_distUnitsDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Config.selectUnitSet(_distUnitsDropdown.getSelectedIndex());
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
		UnitSet unitSet = UnitSetLibrary.getUnitSet(_distUnitsDropdown.getSelectedIndex());
		String distUnitsStr = I18nManager.getText(unitSet.getDistanceUnit().getShortnameKey());
		String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
		if (_timezone == null || (inUpdateType | UNITS_CHANGED) > 0) {
			_timezone = TimezoneHelper.getSelectedTimezone();
		}
		if ((inUpdateType | UNITS_CHANGED) > 0) {
			Config.setConfigString(Config.KEY_COORD_DISPLAY_FORMAT, "" + getSelectedCoordFormat());
		}

		if (_track == null || currentPoint == null)
		{
			_indexLabel.setText(I18nManager.getText("details.nopointselection"));
			_latLabel.setText("");
			_longLabel.setText("");
			_altLabel.setText("");
			_ptDateLabel.setText("");
			_ptTimeLabel.setText("");
			_descLabel.setText("");
			_nameLabel.setText("");
			_typeLabel.setText("");
			_speedLabel.setText("");
			_vSpeedLabel.setText("");
			_filenameLabel.setText("");
		}
		else
		{
			_indexLabel.setText(LABEL_POINT_SELECTED
				+ (currentPointIndex+1) + " " + I18nManager.getText("details.index.of")
				+ " " + _track.getNumPoints());
			_latLabel.setText(LABEL_POINT_LATITUDE
				+ CoordDisplay.makeCoordinateLabel(currentPoint.getLatitude(), getSelectedCoordFormat()));
			_longLabel.setText(LABEL_POINT_LONGITUDE
				+ CoordDisplay.makeCoordinateLabel(currentPoint.getLongitude(), getSelectedCoordFormat()));
			Unit altUnit = Config.getUnitSet().getAltitudeUnit();
			_altLabel.setText(currentPoint.hasAltitude()?
				(LABEL_POINT_ALTITUDE + currentPoint.getAltitude().getValue(altUnit) + " " +
				I18nManager.getText(altUnit.getShortnameKey()))
				: "");
			if (currentPoint.hasTimestamp())
			{
				_ptDateLabel.setText(LABEL_POINT_DATE + currentPoint.getTimestamp().getDateText(_timezone));
				_ptTimeLabel.setText(LABEL_POINT_TIME + currentPoint.getTimestamp().getTimeText(_timezone));
			}
			else
			{
				_ptDateLabel.setText("");
				_ptTimeLabel.setText("");
			}
			// Maybe the point has a description?
			String pointDesc = currentPoint.getFieldValue(Field.DESCRIPTION);
			if (pointDesc == null || pointDesc.equals("") || currentPoint.hasMedia())
			{
				_descLabel.setText("");
				_descLabel.setToolTipText("");
			}
			else
			{
				if (pointDesc.length() < 5) {
					_descLabel.setText(LABEL_POINT_DESCRIPTION + pointDesc);
				}
				else {
					_descLabel.setText(shortenString(pointDesc));
				}
				_descLabel.setToolTipText(pointDesc);
			}

			// Speed can come from either timestamps and distances, or speed values in data
			SpeedValue speedValue = new SpeedValue();
			SpeedCalculator.calculateSpeed(_track, currentPointIndex, speedValue);
			if (speedValue.isValid())
			{
				String speed = DisplayUtils.roundedNumber(speedValue.getValue()) + " " + speedUnitsStr;
				_speedLabel.setText(LABEL_POINT_SPEED + speed);
			}
			else {
				_speedLabel.setText("");
			}

			// Now do the vertical speed in the same way
			SpeedCalculator.calculateVerticalSpeed(_track, currentPointIndex, speedValue);
			if (speedValue.isValid())
			{
				String vSpeedUnitsStr = I18nManager.getText(unitSet.getVerticalSpeedUnit().getShortnameKey());
				String speed = DisplayUtils.roundedNumber(speedValue.getValue()) + " " + vSpeedUnitsStr;
				_vSpeedLabel.setText(LABEL_POINT_VERTSPEED + speed);
			}
			else {
				_vSpeedLabel.setText("");
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

			// File to which point belongs
			final int numFiles = _trackInfo.getFileInfo().getNumFiles();
			String filename = null;
			if (numFiles > 1)
			{
				final SourceInfo info = _trackInfo.getFileInfo().getSourceForPoint(currentPoint);
				if (info != null) {
					filename = info.getName();
				}
			}
			if (filename != null)
			{
				_filenameLabel.setText(LABEL_POINT_FILENAME + filename);
				_filenameLabel.setToolTipText(filename);
			}
			else
			{
				_filenameLabel.setText("");
				_filenameLabel.setToolTipText("");
			}
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
			_distanceLabel.setText(LABEL_RANGE_DISTANCE + DisplayUtils.roundedNumber(selection.getMovingDistance()) + " " + distUnitsStr);
			final long numMovingSeconds = selection.getMovingSeconds();
			if (numMovingSeconds > 0L)
			{
				_durationLabel.setText(LABEL_RANGE_DURATION + DisplayUtils.buildDurationString(numMovingSeconds));
				_aveSpeedLabel.setText(I18nManager.getText("details.range.avespeed") + ": "
					+ DisplayUtils.roundedNumber(selection.getMovingDistance()/numMovingSeconds*3600.0) + " " + speedUnitsStr);
			}
			else
			{
				_durationLabel.setText("");
				_aveSpeedLabel.setText("");
			}
			AltitudeRange altRange = selection.getAltitudeRange();
			Unit altUnit = Config.getUnitSet().getAltitudeUnit();
			String altUnitsLabel = I18nManager.getText(altUnit.getShortnameKey());
			if (altRange.hasRange())
			{
				_altRangeLabel.setText(LABEL_RANGE_ALTITUDE
					+ altRange.getMinimum(altUnit) + altUnitsLabel + " "
					+ I18nManager.getText("details.altitude.to") + " "
					+ altRange.getMaximum(altUnit) + altUnitsLabel);
				_updownLabel.setText(LABEL_RANGE_CLIMB + altRange.getClimb(altUnit) + altUnitsLabel
					+ LABEL_RANGE_DESCENT + altRange.getDescent(altUnit) + altUnitsLabel);
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
			_photoPathLabel.setText("");
			_photoPathLabel.setToolTipText("");
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
			String fullPath = currentPhoto.getFullPath();
			String shortPath = shortenPath(fullPath);
			_photoPathLabel.setText(fullPath == null ? "" : LABEL_FULL_PATH + shortPath);
			_photoPathLabel.setToolTipText(currentPhoto.getFullPath());
			_photoTimestampLabel.setText(currentPhoto.hasTimestamp() ?
				(LABEL_POINT_TIME + currentPhoto.getTimestamp().getText(_timezone))
				: "");
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
			if ((inUpdateType & DataSubscriber.PHOTOS_MODIFIED) > 0) {
				_photoThumbnail.refresh();
			}
		}
		_photoThumbnail.repaint();

		// audio details
		_audioDetailsPanel.setVisible(_trackInfo.getAudioList().getNumAudios() > 0);
		AudioClip currentAudio = _trackInfo.getAudioList().getAudio(_trackInfo.getSelection().getCurrentAudioIndex());
		if (currentAudio == null)
		{
			_audioLabel.setText(I18nManager.getText("details.noaudio"));
			_audioPathLabel.setText("");
			_audioPathLabel.setToolTipText("");
			_audioTimestampLabel.setText("");
			_audioLengthLabel.setText("");
			_audioConnectedLabel.setText("");
		}
		else
		{
			_audioLabel.setText(LABEL_AUDIO_FILE + currentAudio.getName());
			String fullPath = currentAudio.getFullPath();
			String shortPath = shortenPath(fullPath);
			_audioPathLabel.setText(fullPath == null ? "" : LABEL_FULL_PATH + shortPath);
			_audioPathLabel.setToolTipText(fullPath == null ? "" : fullPath);
			_audioTimestampLabel.setText(currentAudio.hasTimestamp() ?
				(LABEL_POINT_TIME + currentAudio.getTimestamp().getText(_timezone))
				: "");
			int audioLength = currentAudio.getLengthInSeconds();
			_audioLengthLabel.setText(audioLength < 0?"":LABEL_RANGE_DURATION + DisplayUtils.buildDurationString(audioLength));
			_audioConnectedLabel.setText(I18nManager.getText("details.media.connected") + ": "
				+ (currentAudio.getCurrentStatus() == Photo.Status.NOT_CONNECTED ?
					I18nManager.getText("dialog.about.no"):I18nManager.getText("dialog.about.yes")));
		}
		_playAudioPanel.setVisible(currentAudio != null);
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

	/**
	 * @param inFullPath full file path or URL to be shortened
	 * @return shortened string from beginning of path
	 */
	private static String shortenPath(String inFullPath)
	{
		String path = inFullPath;
		// Chop off the home path if possible
		final String homePath = System.getProperty("user.home").toLowerCase();
		if (inFullPath != null && inFullPath.toLowerCase().startsWith(homePath)) {
			path = inFullPath.substring(homePath.length()+1);
		}
		return shortenString(path);
	}

	/**
	 * @param inString string to shorten
	 * @return shortened string from the beginning
	 */
	private static String shortenString(String inString)
	{
		// Limit is hardcoded here, maybe it should depend on parent component width and font size etc?
		if (inString == null || inString.length() < 21) {
			return inString;
		}
		// string is too long
		return inString.substring(0, 20) + "...";
	}

	/**
	 * @return the currently selected coordinate display format
	 */
	private int getSelectedCoordFormat()
	{
		switch (_coordFormatDropdown.getSelectedIndex())
		{
			case 1: // degminsec
				return Coordinate.FORMAT_DEG_MIN_SEC;
			case 2: // degmin
				return Coordinate.FORMAT_DEG_MIN;
			case 3: // degrees
				return Coordinate.FORMAT_DEG;
			default: // just as it was
				return Coordinate.FORMAT_NONE;
		}
	}
}
