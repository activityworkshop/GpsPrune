package tim.prune.function.info;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.AltitudeRange;
import tim.prune.data.AudioClip;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.FileInfo;
import tim.prune.data.FileType;
import tim.prune.data.NumberUtils;
import tim.prune.data.Photo;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Selection;
import tim.prune.data.SourceInfo;
import tim.prune.data.SpeedCalculator;
import tim.prune.data.SpeedValue;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSet;
import tim.prune.gui.CoordDisplay;
import tim.prune.gui.DisplayUtils;


/**
 * Class to show the full point/range details in a separate popup
 */
public class ShowFullDetails extends GenericFunction
{
	private JDialog _dialog = null;
	private JTabbedPane _tabs = null;
	private JButton _okButton = null;

	private JTextArea _fileTextArea = null;
	private JTextArea _pointTextArea = null;
	private JTextArea _rangeTextArea = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public ShowFullDetails(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.viewfulldetails";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		updateDetails();
		_dialog.setVisible(true);
		_okButton.requestFocus();
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		_tabs = new JTabbedPane();
		mainPanel.add(_tabs, BorderLayout.CENTER);

		// Closer for the escape key
		KeyListener escapeCloser = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		};
		_tabs.addKeyListener(escapeCloser);

		_fileTextArea = new JTextArea(I18nManager.getText("details.nofileloaded"));
		JPanel filePanel = makePanelForTab(_fileTextArea);
		_tabs.add(I18nManager.getText("details.filedetails"), filePanel);

		_pointTextArea = new JTextArea(I18nManager.getText("details.nopointselection"));
		JPanel pointPanel = makePanelForTab(_pointTextArea);
		_tabs.add(I18nManager.getText("details.pointdetails"), pointPanel);

		_rangeTextArea = new JTextArea(I18nManager.getText("details.norangeselection"));
		JPanel rangePanel = makePanelForTab(_rangeTextArea);
		_tabs.add(I18nManager.getText("details.rangedetails"), rangePanel);

		// OK button at the bottom
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> _dialog.dispose());
		_okButton.addKeyListener(escapeCloser);
		okPanel.add(_okButton);
		mainPanel.add(okPanel, BorderLayout.SOUTH);
		return mainPanel;
	}


	private JPanel makePanelForTab(JTextArea inTextArea)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		inTextArea.setEditable(false);
		inTextArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(inTextArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(500, 230));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Update the labels with the current details
	 */
	private void updateDetails()
	{
		int tabToShow = 0;

		_fileTextArea.setText(makeFileDescription(_app.getTrackInfo().getFileInfo()));

		if (_app.getTrackInfo().getCurrentPoint() != null)
		{
			final String pointString = makePointDescription(_app.getTrackInfo().getTrack(),
				_app.getTrackInfo().getSelection().getCurrentPointIndex());
			_pointTextArea.setText(pointString);
			// Select point tab
			tabToShow = 1;
		}
		else {
			_pointTextArea.setText(I18nManager.getText("details.nopointselection"));
		}

		Selection selection = _app.getTrackInfo().getSelection();
		if (selection.hasRangeSelected())
		{
			final int altitudeTolerance = getConfig().getConfigInt(Config.KEY_ALTITUDE_TOLERANCE) / 100;
			RangeStatsWithGradients stats = new RangeStatsWithGradients(_app.getTrackInfo().getTrack(),
				selection.getStart(), selection.getEnd(), altitudeTolerance);
			SpeedValue maxSpeed = calculateMaxSpeed(_app.getTrackInfo().getTrack(),
				selection.getStart(), selection.getEnd());
			_rangeTextArea.setText(makeRangeDescription(stats, maxSpeed));
			// Select range tab
			tabToShow = 2;
		}
		else {
			_rangeTextArea.setText(I18nManager.getText("details.norangeselection"));
		}
		_tabs.setSelectedIndex(tabToShow);
	}

	/**
	 * @return string describing the file or files loaded
	 */
	private String makeFileDescription(FileInfo inFileInfo)
	{
		StringBuilder builder = new StringBuilder();
		boolean foundFile = false;
		int numSources = inFileInfo.getNumFiles();
		for (int idx=0; idx < numSources; idx++)
		{
			SourceInfo source = inFileInfo.getSource(idx);
			FileType type = source.getFileType();
			if (type == null || type == FileType.GPSBABEL) {
				continue;
			}
			if (foundFile) {
				builder.append("========================================\n");
			}
			foundFile = true;
			addTextPair(builder, "details.track.file", source.getName());
			addTextPair(builder, "details.track.filetype", I18nManager.getText(type.getTextKey()));
			addTextPair(builder, "details.track.fileversion", source.getFileVersion());
			addTextPair(builder, "details.track.filepath", source.getFile().getAbsolutePath());
			// could also show file size, timestamp if anybody cares?
			addTextPair(builder, "dialog.exportgpx.name", source.getFileTitle());
			addTextPair(builder, "dialog.exportgpx.desc", source.getFileDescription());
			addTextPair(builder, "dialog.exportgpx.extensions", source.getExtensions());
			builder.append('\n');
		}

		if (foundFile) {
			return builder.toString();
		}
		return I18nManager.getText("details.nofileloaded");
	}

	/**
	 * Calculate the maximum horizontal speed value in the given selection
	 * @param inTrack track object
	 * @param inStartIndex start of selection
	 * @param inEndIndex end of selection
	 * @return max speed, if any
	 */
	private SpeedValue calculateMaxSpeed(Track inTrack, int inStartIndex, int inEndIndex)
	{
		SpeedValue maxSpeed = new SpeedValue();
		SpeedValue currSpeed = new SpeedValue();
		for (int i=inStartIndex; i<=inEndIndex; i++)
		{
			SpeedCalculator.calculateSpeed(inTrack, i, getConfig().getUnitSet(), currSpeed);
			if (currSpeed.isValid() && (!maxSpeed.isValid() || currSpeed.getValue() > maxSpeed.getValue()))
			{
				maxSpeed.setValue(currSpeed.getValue());
			}
		}
		return maxSpeed;
	}

	/**
	 * @param inTrack current track
	 * @param inPointIndex current point index
	 * @return string describing point details
	 */
	private String makePointDescription(Track inTrack, int inPointIndex)
	{
		DataPoint point = inTrack.getPoint(inPointIndex);
		if (point == null) {
			return "";
		}

		final Coordinate.Format coordDisplayFormat = Coordinate.getCoordinateFormatForDisplay(
			getConfig().getConfigString(Config.KEY_COORD_DISPLAY_FORMAT));
		StringBuilder result = new StringBuilder();
		final String latStr = CoordDisplay.makeCoordinateLabel(point.getLatitude(), coordDisplayFormat);
		final String lonStr = CoordDisplay.makeCoordinateLabel(point.getLongitude(), coordDisplayFormat);
		addTextPair(result, "fieldname.latitude", latStr);
		addTextPair(result, "fieldname.longitude", lonStr);
		addTextPair(result, "fieldname.coordinates", latStr + NumberUtils.getValueSeparator() + lonStr);

		UnitSet unitSet = getConfig().getUnitSet();
		if (point.hasAltitude())
		{
			final Unit altUnit = unitSet.getAltitudeUnit();
			addTextPair(result, "fieldname.altitude", point.getAltitude().getLocalStringValue(altUnit),
				I18nManager.getText(altUnit.getShortnameKey()));
		}
		if (point.hasTimestamp())
		{
			TimeZone timezone = TimezoneHelper.getSelectedTimezone(getConfig());
			addTextPair(result, "fieldname.date", point.getTimestamp().getDateText(timezone));
			addTextPair(result, "fieldname.timestamp", point.getTimestamp().getTimeText(timezone));
		}

		addTextPair(result, "fieldname.waypointname", point.getWaypointName());

		addTextPair(result, "fieldname.description", point.getFieldValue(Field.DESCRIPTION));

		addTextPair(result, "fieldname.comment", point.getFieldValue(Field.COMMENT));
		addTextPair(result, "fieldname.symbol", point.getFieldValue(Field.SYMBOL));

		addTextPair(result, "fieldname.waypointtype", point.getFieldValue(Field.WAYPT_TYPE));

		// Speed can come from either timestamps and distances, or speed values in data
		SpeedValue speedValue = new SpeedValue();
		SpeedCalculator.calculateSpeed(inTrack, inPointIndex, unitSet, speedValue);
		if (speedValue.isValid())
		{
			final String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
			String speed = DisplayUtils.roundedNumber(speedValue.getValue());
			addTextPair(result, "fieldname.speed", speed, speedUnitsStr);
		}

		// Now do the vertical speed in the same way
		SpeedCalculator.calculateVerticalSpeed(inTrack, inPointIndex, unitSet, speedValue);
		if (speedValue.isValid())
		{
			final String vSpeedUnitsStr = I18nManager.getText(unitSet.getVerticalSpeedUnit().getShortnameKey());
			String speed = DisplayUtils.roundedNumber(speedValue.getValue());
			addTextPair(result, "fieldname.verticalspeed", speed, vSpeedUnitsStr);
		}

		Photo currentPhoto = point.getPhoto();
		if (currentPhoto != null)
		{
			addTextPair(result, "details.photofile", currentPhoto.getName());
			addTextPair(result, "details.media.fullpath", currentPhoto.getFullPath());
		}

		AudioClip currentAudio = point.getAudio();
		if (currentAudio != null)
		{
			addTextPair(result, "details.audio.file", currentAudio.getName());
			addTextPair(result, "details.media.fullpath", currentAudio.getFullPath());
		}

		// Additional fields, if any
		FieldList fieldList = point.getFieldList();
		for (int i=0; i<fieldList.getNumFields(); i++)
		{
			Field field = fieldList.getField(i);
			if (!field.isBuiltIn()) {
				addTextPairWithoutTranslation(result, field.getName(), point.getFieldValue(field));
			}
		}
		return result.toString();
	}

	/**
	 * Make the range description text
	 * @param inStats stats object
	 * @param inMaxSpeed maximum speed info
	 * @return string describing range
	 */
	private String makeRangeDescription(RangeStatsWithGradients inStats, SpeedValue inMaxSpeed)
	{
		StringBuilder result = new StringBuilder();
		addTextPair(result, "details.track.points", "" + inStats.getNumPoints());
		addTextPair(result, "details.range.numsegments", "" + inStats.getNumSegments());
		final boolean hasMultipleSegments = (inStats.getNumSegments() > 1);

		UnitSet unitSet = getConfig().getUnitSet();
		final String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
		if (inMaxSpeed.isValid())
		{
			final String maxSpeedStr = DisplayUtils.roundedNumber(inMaxSpeed.getValue()) + " " + speedUnitsStr;
			addTextPair(result, "details.range.maxspeed", maxSpeedStr);
		}

		addHeading(result, "dialog.fullrangedetails.colsegments");
		final Unit distUnit = unitSet.getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnit.getShortnameKey());
		final double movingDist = inStats.getMovingDistance(distUnit);
		addTextPair(result, "fieldname.distance", DisplayUtils.roundedNumber(movingDist),
			distUnitsStr);
		long numSecs = inStats.getMovingDurationInSeconds();
		addTextPair(result, "fieldname.duration", DisplayUtils.buildDurationString(numSecs));

		if (numSecs > 0 && movingDist > 0.0)
		{
			addTextPair(result, "details.range.avespeed", DisplayUtils.roundedNumber(movingDist/numSecs*3600.0),
				speedUnitsStr);
			addTextPair(result, "details.range.pace", DisplayUtils.buildDurationString((long) (numSecs/movingDist)),
				"/ " + distUnitsStr);
		}
		final Unit altUnit = unitSet.getAltitudeUnit();
		final String altUnitsStr = I18nManager.getText(altUnit.getShortnameKey());
		if (inStats.getMovingAltitudeRange().hasRange())
		{
			AltitudeRange altRange = inStats.getMovingAltitudeRange();
			addTextPair(result, "fieldname.altitude", "" + altRange.getMinimum(altUnit) + altUnitsStr + " "
				+ I18nManager.getText("details.altitude.to") + " "
				+ altRange.getMaximum(altUnit) + altUnitsStr);
			addTextPair(result, "details.range.climb", "" + altRange.getClimb(altUnit), altUnitsStr);
			addTextPair(result, "details.range.descent", "" + altRange.getDescent(altUnit), altUnitsStr);
			addTextPair(result, "details.range.gradient", DisplayUtils.formatOneDp(inStats.getMovingGradient()), "%");
			if (numSecs > 0)
			{
				final String vertSpeedUnitsStr = I18nManager.getText(unitSet.getVerticalSpeedUnit().getShortnameKey());
				final String vertSpeedStr = DisplayUtils.roundedNumber(inStats.getMovingVerticalSpeed());
				addTextPair(result, "fieldname.verticalspeed", vertSpeedStr, vertSpeedUnitsStr);
			}
		}

		if (hasMultipleSegments)
		{
			addHeading(result, "dialog.fullrangedetails.coltotal");
			final double totalDist = inStats.getTotalDistance(distUnit);
			addTextPair(result, "fieldname.distance", DisplayUtils.roundedNumber(totalDist), distUnitsStr);
			long totalSecs = inStats.getTotalDurationInSeconds();
			addTextPair(result, "fieldname.duration", DisplayUtils.buildDurationString(totalSecs));
			if (totalSecs > 0 && totalDist > 0.0)
			{
				addTextPair(result, "details.range.avespeed", DisplayUtils.roundedNumber(totalDist/totalSecs*3600.0),
					speedUnitsStr);
				addTextPair(result, "details.range.pace", DisplayUtils.buildDurationString((long) (totalSecs/totalDist)),
					"/ " + distUnitsStr);
			}
			if (inStats.getTotalAltitudeRange().hasRange())
			{
				AltitudeRange altRange = inStats.getTotalAltitudeRange();
				addTextPair(result, "details.range.climb", "" + altRange.getClimb(altUnit), altUnitsStr);
				addTextPair(result, "details.range.descent", "" + altRange.getDescent(altUnit), altUnitsStr);
				addTextPair(result, "details.range.gradient", DisplayUtils.formatOneDp(inStats.getTotalGradient()), "%");
				if (totalSecs > 0)
				{
					final String vertSpeedUnitsStr = I18nManager.getText(unitSet.getVerticalSpeedUnit().getShortnameKey());
					final String vertSpeedStr = DisplayUtils.roundedNumber(inStats.getTotalVerticalSpeed());
					addTextPair(result, "fieldname.verticalspeed", vertSpeedStr, vertSpeedUnitsStr);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Add the label and value to the buffer
	 * @param inBuilder buffer to append to
	 * @param inLabelKey label key
	 * @param inValue value text
	 */
	private static void addTextPair(StringBuilder inBuilder, String inLabelKey, String inValue)
	{
		addTextPair(inBuilder, inLabelKey, inValue, null);
	}

	/**
	 * Add the label and value to the buffer
	 * @param inBuilder buffer to append to
	 * @param inLabel label text without translation
	 * @param inValue value text
	 */
	private static void addTextPairWithoutTranslation(StringBuilder inBuilder, String inLabel, String inValue)
	{
		if (inValue != null && !inValue.equals(""))
		{
			inBuilder.append(inLabel).append(": ").append(inValue);
			inBuilder.append("\n");
		}
	}

	/**
	 * Add the label and value to the buffer
	 * @param inBuilder buffer to append to
	 * @param inLabelKey label key
	 * @param inValue value text
	 * @param inUnits optional units string
	 */
	private static void addTextPair(StringBuilder inBuilder, String inLabelKey, String inValue, String inUnits)
	{
		if (inValue != null && !inValue.equals(""))
		{
			inBuilder.append(I18nManager.getText(inLabelKey));
			inBuilder.append(": ");
			inBuilder.append(inValue);
			if (inUnits != null && !inUnits.equals(""))
			{
				inBuilder.append(' ');
				inBuilder.append(inUnits);
			}
			inBuilder.append("\n");
		}
	}

	/**
	 * Add a heading to the buffer
	 * @param inBuilder buffer to append to
	 * @param inLabelKey key for heading
	 */
	private static void addHeading(StringBuilder inBuilder, String inLabelKey)
	{
		final String heading = I18nManager.getText(inLabelKey);
		inBuilder.append('\n').append(heading).append('\n');
		inBuilder.append("=".repeat(heading.length()));
		inBuilder.append('\n');
	}
}
