package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import tim.prune.data.Photo;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Selection;
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

	private JTextArea _pointTextArea = null;
	private JTextArea _rangeTextArea = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public ShowFullDetails(App inApp)
	{
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
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
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

		JPanel pointPanel = new JPanel();
		pointPanel.setLayout(new BorderLayout());
		_pointTextArea = new JTextArea(I18nManager.getText("details.nopointselection"));
		_pointTextArea.setEditable(false);
		_pointTextArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(_pointTextArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(500, 230));
		pointPanel.add(scrollPane, BorderLayout.CENTER);
		_tabs.add(I18nManager.getText("details.pointdetails"), pointPanel);

		JPanel rangePanel = new JPanel();
		rangePanel.setLayout(new BorderLayout());
		_rangeTextArea = new JTextArea(I18nManager.getText("details.norangeselection"));
		_rangeTextArea.setEditable(false);
		_rangeTextArea.setLineWrap(true);
		JScrollPane scrollPane2 = new JScrollPane(_rangeTextArea);
		scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane2.setPreferredSize(new Dimension(500, 230));
		rangePanel.add(scrollPane2, BorderLayout.CENTER);
		_tabs.add(I18nManager.getText("details.rangedetails"), rangePanel);

		// OK button at the bottom
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		_okButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		okPanel.add(_okButton);
		mainPanel.add(okPanel, BorderLayout.SOUTH);
		return mainPanel;
	}


	/**
	 * Update the labels with the current details
	 */
	private void updateDetails()
	{
		if (_app.getTrackInfo().getCurrentPoint() != null)
		{
			final String pointString = makePointDescription(_app.getTrackInfo().getTrack(),
				_app.getTrackInfo().getSelection().getCurrentPointIndex());
			_pointTextArea.setText(pointString);
			// Select point tab
			_tabs.setSelectedIndex(0);
		}
		else
		{
			_pointTextArea.setText(I18nManager.getText("details.nopointselection"));
			// Select range tab
			_tabs.setSelectedIndex(1);
		}

		Selection selection = _app.getTrackInfo().getSelection();
		if (selection.hasRangeSelected())
		{
			RangeStatsWithGradients stats = new RangeStatsWithGradients(_app.getTrackInfo().getTrack(),
				selection.getStart(), selection.getEnd());
			SpeedValue maxSpeed = calculateMaxSpeed(_app.getTrackInfo().getTrack(),
				selection.getStart(), selection.getEnd());
			_rangeTextArea.setText(makeRangeDescription(stats, maxSpeed));
		}
		else
		{
			_rangeTextArea.setText(I18nManager.getText("details.norangeselection"));
		}
	}

	/**
	 * Calculate the maximum horizontal speed value in the given selection
	 * @param inTrack track object
	 * @param inStartIndex start of selection
	 * @param inEndIndex end of selection
	 * @return max speed, if any
	 */
	private static SpeedValue calculateMaxSpeed(Track inTrack, int inStartIndex, int inEndIndex)
	{
		SpeedValue maxSpeed = new SpeedValue();
		SpeedValue currSpeed = new SpeedValue();
		for (int i=inStartIndex; i<=inEndIndex; i++)
		{
			SpeedCalculator.calculateSpeed(inTrack, i, currSpeed);
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
	private static String makePointDescription(Track inTrack, int inPointIndex)
	{
		DataPoint point = inTrack.getPoint(inPointIndex);
		if (point == null)
		{
			return "";
		}

		final int coordDisplayFormat = Coordinate.getCoordinateFormatForDisplay(
			Config.getConfigInt(Config.KEY_COORD_DISPLAY_FORMAT));
		StringBuffer result = new StringBuffer();
		final String latStr = CoordDisplay.makeCoordinateLabel(point.getLatitude(), coordDisplayFormat);
		final String lonStr = CoordDisplay.makeCoordinateLabel(point.getLongitude(), coordDisplayFormat);
		addTextPair(result, "fieldname.latitude", latStr);
		addTextPair(result, "fieldname.longitude", lonStr);
		addTextPair(result, "fieldname.coordinates", latStr + ", " + lonStr);

		if (point.hasAltitude())
		{
			final Unit altUnit = Config.getUnitSet().getAltitudeUnit();
			addTextPair(result, "fieldname.altitude", "" + point.getAltitude().getValue(altUnit),
				I18nManager.getText(altUnit.getShortnameKey()));
		}
		if (point.hasTimestamp())
		{
			TimeZone timezone = TimezoneHelper.getSelectedTimezone();
			addTextPair(result, "fieldname.date", point.getTimestamp().getDateText(timezone));
			addTextPair(result, "fieldname.timestamp", point.getTimestamp().getTimeText(timezone));
		}

		addTextPair(result, "fieldname.waypointname", point.getWaypointName());

		addTextPair(result, "fieldname.description", point.getFieldValue(Field.DESCRIPTION));

		addTextPair(result, "fieldname.waypointtype", point.getFieldValue(Field.WAYPT_TYPE));

		// Speed can come from either timestamps and distances, or speed values in data
		SpeedValue speedValue = new SpeedValue();
		SpeedCalculator.calculateSpeed(inTrack, inPointIndex, speedValue);
		UnitSet unitSet = Config.getUnitSet();
		if (speedValue.isValid())
		{
			final String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
			String speed = DisplayUtils.roundedNumber(speedValue.getValue());
			addTextPair(result, "fieldname.speed", speed, speedUnitsStr);
		}

		// Now do the vertical speed in the same way
		SpeedCalculator.calculateVerticalSpeed(inTrack, inPointIndex, speedValue);
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

		return result.toString();
	}

	/**
	 * Make the range description text
	 * @param inStats stats object
	 * @param inMaxSpeed maximum speed info
	 * @return string describing range
	 */
	private static String makeRangeDescription(RangeStatsWithGradients inStats, SpeedValue inMaxSpeed)
	{
		StringBuffer result = new StringBuffer();
		addTextPair(result, "details.track.points", "" + inStats.getNumPoints());
		addTextPair(result, "details.range.numsegments", "" + inStats.getNumSegments());
		final boolean hasMultipleSegments = (inStats.getNumSegments() > 1);

		UnitSet unitSet = Config.getUnitSet();
		final String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
		if (inMaxSpeed.isValid())
		{
			final String maxSpeedStr = DisplayUtils.roundedNumber(inMaxSpeed.getValue()) + " " + speedUnitsStr;
			addTextPair(result, "details.range.maxspeed", maxSpeedStr);
		}

		addHeading(result, "dialog.fullrangedetails.colsegments");
		final Unit distUnit = Config.getUnitSet().getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnit.getShortnameKey());
		final double movingDist = inStats.getMovingDistance();
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
			final double totalDist = inStats.getTotalDistance();
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
	 * @param inBuffer buffer to append to
	 * @param inLabelKey label key
	 * @param inValue value text
	 */
	private static void addTextPair(StringBuffer inBuffer, String inLabelKey, String inValue)
	{
		addTextPair(inBuffer, inLabelKey, inValue, null);
	}

	/**
	 * Add the label and value to the buffer
	 * @param inBuffer buffer to append to
	 * @param inLabelKey label key
	 * @param inValue value text
	 * @param inUnits optional units string
	 */
	private static void addTextPair(StringBuffer inBuffer, String inLabelKey, String inValue, String inUnits)
	{
		if (inValue != null && !inValue.equals(""))
		{
			inBuffer.append(I18nManager.getText(inLabelKey));
			inBuffer.append(": ");
			inBuffer.append(inValue);
			if (inUnits != null && !inUnits.equals(""))
			{
				inBuffer.append(' ');
				inBuffer.append(inUnits);
			}
			inBuffer.append("\n");
		}
	}

	/**
	 * Add a heading to the buffer
	 * @param inBuffer buffer to append to
	 * @param inLabelKey key for heading
	 */
	private static void addHeading(StringBuffer inBuffer, String inLabelKey)
	{
		final String heading = I18nManager.getText(inLabelKey);
		inBuffer.append('\n').append(heading).append('\n');
		for (int i=0; i<heading.length(); i++)
		{
			inBuffer.append('=');
		}
		inBuffer.append('\n');
	}
}
