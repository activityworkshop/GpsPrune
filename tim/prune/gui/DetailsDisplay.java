package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.IntegerRange;
import tim.prune.data.Selection;
import tim.prune.data.TrackInfo;

/**
 * Class to hold point details and selection details
 * as a visual component
 */
public class DetailsDisplay extends GenericDisplay
{
	// App object to be notified of editing commands
	private App _app = null;

	// Track details
	private JLabel _trackpointsLabel = null;
	private JLabel _filenameLabel = null;
	// Point details
	private JLabel _indexLabel = null;
	private JLabel _latLabel = null, _longLabel = null;
	private JLabel _altLabel = null, _nameLabel = null;
	private JLabel _timeLabel = null, _photoFileLabel = null;
	// Scroll bar
	private JScrollBar _scroller = null;
	private boolean _ignoreScrollEvents = false;
	// Button panel
	private JButton _startRangeButton = null, _endRangeButton = null;
	private JButton _deletePointButton = null, _deleteRangeButton = null;

	// Range details
	private JLabel _rangeLabel = null;
	private JLabel _distanceLabel = null, _durationLabel = null;
	private JLabel _altRangeLabel = null, _updownLabel = null;
	// Photos
	private JList _photoList = null;
	private PhotoListModel _photoListModel = null;
	// Waypoints
	private JList _waypointList = null;
	private WaypointListModel _waypointListModel = null;
	// Units
	private JComboBox _unitsDropdown = null;
	// Formatter
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	// Cached labels
	private static final String LABEL_POINT_SELECTED1 = I18nManager.getText("details.index.selected") + ": ";
	private static final String LABEL_POINT_LATITUDE = I18nManager.getText("fieldname.latitude") + ": ";
	private static final String LABEL_POINT_LONGITUDE = I18nManager.getText("fieldname.longitude") + ": ";
	private static final String LABEL_POINT_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_POINT_TIMESTAMP = I18nManager.getText("fieldname.timestamp") + ": ";
	private static final String LABEL_POINT_WAYPOINTNAME = I18nManager.getText("fieldname.waypointname") + ": ";
	private static final String LABEL_RANGE_SELECTED1 = I18nManager.getText("details.range.selected") + ": ";
	private static final String LABEL_RANGE_DURATION = I18nManager.getText("fieldname.duration") + ": ";
	private static final String LABEL_RANGE_DISTANCE = I18nManager.getText("fieldname.distance") + ": ";
	private static final String LABEL_RANGE_ALTITUDE = I18nManager.getText("fieldname.altitude") + ": ";
	private static final String LABEL_RANGE_CLIMB = I18nManager.getText("details.range.climb") + ": ";
	private static final String LABEL_RANGE_DESCENT = ", " + I18nManager.getText("details.range.descent") + ": ";
	private static String LABEL_POINT_ALTITUDE_UNITS = null;
	private static int LABEL_POINT_ALTITUDE_FORMAT = Altitude.FORMAT_NONE;
	// scrollbar interval
	private static final int SCROLLBAR_INTERVAL = 50;


	/**
	 * Constructor
	 * @param inApp App object for callbacks
	 * @param inTrackInfo Track info object
	 */
	public DetailsDisplay(App inApp, TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		_app = inApp;
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		// Track details panel
		JPanel trackDetailsPanel = new JPanel();
		trackDetailsPanel.setLayout(new BoxLayout(trackDetailsPanel, BoxLayout.Y_AXIS));
		trackDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel trackDetailsLabel = new JLabel(I18nManager.getText("details.trackdetails"));
		Font biggerFont = trackDetailsLabel.getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);
		trackDetailsLabel.setFont(biggerFont);
		trackDetailsPanel.add(trackDetailsLabel);
		_trackpointsLabel = new JLabel(I18nManager.getText("details.notrack"));
		trackDetailsPanel.add(_trackpointsLabel);
		_filenameLabel = new JLabel("");
		trackDetailsPanel.add(_filenameLabel);

		// Point details panel
		JPanel pointDetailsPanel = new JPanel();
		pointDetailsPanel.setLayout(new BoxLayout(pointDetailsPanel, BoxLayout.Y_AXIS));
		pointDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel pointDetailsLabel = new JLabel(I18nManager.getText("details.pointdetails"));
		pointDetailsLabel.setFont(biggerFont);
		pointDetailsPanel.add(pointDetailsLabel);
		_indexLabel = new JLabel(I18nManager.getText("details.nopointselection"));
		pointDetailsPanel.add(_indexLabel);
		_latLabel = new JLabel("");
		pointDetailsPanel.add(_latLabel);
		_longLabel = new JLabel("");
		pointDetailsPanel.add(_longLabel);
		_altLabel = new JLabel("");
		pointDetailsPanel.add(_altLabel);
		_timeLabel = new JLabel("");
		pointDetailsPanel.add(_timeLabel);
		_photoFileLabel = new JLabel("");
		pointDetailsPanel.add(_photoFileLabel);
		_nameLabel = new JLabel("");
		pointDetailsPanel.add(_nameLabel);
		pointDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Scroll bar
		_scroller = new JScrollBar(JScrollBar.HORIZONTAL, 0, SCROLLBAR_INTERVAL, 0, 100);
		_scroller.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				selectPoint(e.getValue());
			}
		});
		_scroller.setEnabled(false);

		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 2, 3, 3));
		_startRangeButton = new JButton(I18nManager.getText("button.startrange"));
		_startRangeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_trackInfo.getSelection().selectRangeStart();
				}
			});
		_startRangeButton.setEnabled(false);
		buttonPanel.add(_startRangeButton);
		_endRangeButton = new JButton(I18nManager.getText("button.endrange"));
		_endRangeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_trackInfo.getSelection().selectRangeEnd();
				}
			});
		_endRangeButton.setEnabled(false);
		buttonPanel.add(_endRangeButton);
		_deletePointButton = new JButton(I18nManager.getText("button.deletepoint"));
		_deletePointButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_app.deleteCurrentPoint();
				}
			});
		_deletePointButton.setEnabled(false);
		buttonPanel.add(_deletePointButton);
		_deleteRangeButton = new JButton(I18nManager.getText("button.deleterange"));
		_deleteRangeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_app.deleteSelectedRange();
				}
			});
		_deleteRangeButton.setEnabled(false);
		buttonPanel.add(_deleteRangeButton);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// range details panel
		JPanel otherDetailsPanel = new JPanel();
		otherDetailsPanel.setLayout(new BoxLayout(otherDetailsPanel, BoxLayout.Y_AXIS));
		otherDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);

		JLabel otherDetailsLabel = new JLabel(I18nManager.getText("details.rangedetails"));
		otherDetailsLabel.setFont(biggerFont);
		otherDetailsPanel.add(otherDetailsLabel);
		_rangeLabel = new JLabel(I18nManager.getText("details.norangeselection"));
		otherDetailsPanel.add(_rangeLabel);
		_distanceLabel = new JLabel("");
		otherDetailsPanel.add(_distanceLabel);
		_durationLabel = new JLabel("");
		otherDetailsPanel.add(_durationLabel);
		_altRangeLabel = new JLabel("");
		otherDetailsPanel.add(_altRangeLabel);
		_updownLabel = new JLabel("");
		otherDetailsPanel.add(_updownLabel);
		otherDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Add tab panel for waypoints / photos
		JPanel waypointsPanel = new JPanel();
		waypointsPanel.setLayout(new BoxLayout(waypointsPanel, BoxLayout.Y_AXIS));
		waypointsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JTabbedPane tabPane = new JTabbedPane();
		_waypointListModel = new WaypointListModel(_trackInfo.getTrack());
		_waypointList = new JList(_waypointListModel);
		_waypointList.setVisibleRowCount(5);
		_waypointList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) selectWaypoint(_waypointList.getSelectedIndex());
			}});
		tabPane.addTab(I18nManager.getText("details.waypointsphotos.waypoints"), new JScrollPane(_waypointList));
		_photoListModel = new PhotoListModel(_trackInfo.getPhotoList());
		_photoList = new JList(_photoListModel);
		_photoList.setVisibleRowCount(5);
		_photoList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) selectPhoto(_photoList.getSelectedIndex());
			}});
		// TODO: Re-add photos list after v2
		// tabPane.addTab(I18nManager.getText("details.waypointsphotos.photos"), new JScrollPane(_photoList));
		tabPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		waypointsPanel.add(tabPane);
		waypointsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// add the slider, point details, and the other details to the main panel
		mainPanel.add(buttonPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(_scroller);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(trackDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(pointDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(otherDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(waypointsPanel);
		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);

		// Add units selection
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		lowerPanel.add(new JLabel(I18nManager.getText("details.distanceunits") + ": "));
		String[] distUnits = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.miles")};
		_unitsDropdown = new JComboBox(distUnits);
		_unitsDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dataUpdated(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_unitsDropdown);
		add(lowerPanel, BorderLayout.SOUTH);
	}


	/**
	 * Select the specified point
	 * @param inValue value to select
	 */
	private void selectPoint(int inValue)
	{
		if (_track != null && !_ignoreScrollEvents)
		{
			_trackInfo.getSelection().selectPoint(inValue);
		}
	}


	/**
	 * Select the specified photo
	 * @param inPhotoIndex index of selected photo
	 */
	private void selectPhoto(int inPhotoIndex)
	{
		if (_photoListModel.getPhoto(inPhotoIndex) != null)
		{
			// TODO: Deselect the photo when another point is selected
			// TODO: show photo thumbnail
			// select associated point, if any
			DataPoint point = _photoListModel.getPhoto(inPhotoIndex).getDataPoint();
			if (point != null)
			{
				_trackInfo.selectPoint(point);
			}
		}
	}


	/**
	 * Select the specified waypoint
	 * @param inWaypointIndex index of selected waypoint
	 */
	private void selectWaypoint(int inWaypointIndex)
	{
		if (inWaypointIndex >= 0)
		{
			_trackInfo.selectPoint(_waypointListModel.getWaypoint(inWaypointIndex));
		}
	}


	/**
	 * Notification that Track has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Update track data
		if (_track == null || _track.getNumPoints() <= 0)
		{
			_trackpointsLabel.setText(I18nManager.getText("details.notrack"));
			_filenameLabel.setText("");
		}
		else
		{
			_trackpointsLabel.setText(I18nManager.getText("details.track.points") + ": "
				+ _track.getNumPoints());
			int numFiles = _trackInfo.getFileInfo().getNumFiles();
			if (numFiles == 1)
			{
				_filenameLabel.setText(I18nManager.getText("details.track.file") + ": "
					+ _trackInfo.getFileInfo().getFilename());
			}
			else if (numFiles > 1)
			{
				_filenameLabel.setText(I18nManager.getText("details.track.numfiles") + ": "
					+ numFiles);
			}
			else _filenameLabel.setText("");
		}

		// Update current point data, if any
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		Selection selection = _trackInfo.getSelection();
		int currentPointIndex = selection.getCurrentPointIndex();
		if (_track == null || currentPoint == null)
		{
			_indexLabel.setText(I18nManager.getText("details.nopointselection"));
			_latLabel.setText("");
			_longLabel.setText("");
			_altLabel.setText("");
			_timeLabel.setText("");
			_photoFileLabel.setText("");
			_nameLabel.setText("");
		}
		else
		{
			_indexLabel.setText(LABEL_POINT_SELECTED1
				+ (currentPointIndex+1) + " " + I18nManager.getText("details.index.of")
				+ " " + _track.getNumPoints());
			_latLabel.setText(LABEL_POINT_LATITUDE + currentPoint.getLatitude().output(Coordinate.FORMAT_NONE));
			_longLabel.setText(LABEL_POINT_LONGITUDE + currentPoint.getLongitude().output(Coordinate.FORMAT_NONE));
			_altLabel.setText(LABEL_POINT_ALTITUDE
				+ (currentPoint.hasAltitude()?
					(currentPoint.getAltitude().getValue() + getAltitudeUnitsLabel(currentPoint.getAltitude().getFormat())):
				""));
			if (currentPoint.getTimestamp().isValid())
				_timeLabel.setText(LABEL_POINT_TIMESTAMP + currentPoint.getTimestamp().getText());
			else
				_timeLabel.setText("");
			if (currentPoint.getPhoto() != null && currentPoint.getPhoto().getFile() != null)
			{
				_photoFileLabel.setText(I18nManager.getText("details.photofile") + ": "
					+ currentPoint.getPhoto().getFile().getName());
			}
			else
				_photoFileLabel.setText("");
			String name = currentPoint.getWaypointName();
			if (name != null && !name.equals(""))
			{
				_nameLabel.setText(LABEL_POINT_WAYPOINTNAME + name);
			}
			else _nameLabel.setText("");
		}

		// Update scroller settings
		_ignoreScrollEvents = true;
		if (_track == null || _track.getNumPoints() < 2)
		{
			// careful to avoid event loops here
			// _scroller.setValue(0);
			_scroller.setEnabled(false);
		}
		else
		{
			_scroller.setMaximum(_track.getNumPoints() + SCROLLBAR_INTERVAL);
			if (currentPointIndex >= 0)
				_scroller.setValue(currentPointIndex);
			_scroller.setEnabled(true);
		}
		_ignoreScrollEvents = false;

		// Update button panel
		boolean hasPoint = (_track != null && currentPointIndex >= 0);
		_startRangeButton.setEnabled(hasPoint);
		_endRangeButton.setEnabled(hasPoint);
		_deletePointButton.setEnabled(hasPoint);
		_deleteRangeButton.setEnabled(selection.hasRangeSelected());

		// Update range details
		if (_track == null || !selection.hasRangeSelected())
		{
			_rangeLabel.setText(I18nManager.getText("details.norangeselection"));
			_distanceLabel.setText("");
			_durationLabel.setText("");
			_altRangeLabel.setText("");
			_updownLabel.setText("");
		}
		else
		{
			_rangeLabel.setText(LABEL_RANGE_SELECTED1
				+ (selection.getStart()+1) + " " + I18nManager.getText("details.range.to")
				+ " " + (selection.getEnd()+1));
			if (_unitsDropdown.getSelectedIndex() == 0)
				_distanceLabel.setText(LABEL_RANGE_DISTANCE + buildDistanceString(
					selection.getDistance(Distance.UNITS_KILOMETRES))
					+ " " + I18nManager.getText("units.kilometres.short"));
			else
				_distanceLabel.setText(LABEL_RANGE_DISTANCE + buildDistanceString(
					selection.getDistance(Distance.UNITS_MILES))
					+ " " + I18nManager.getText("units.miles.short"));
			if (selection.getNumSeconds() > 0)
				_durationLabel.setText(LABEL_RANGE_DURATION + buildDurationString(selection.getNumSeconds()));
			else
				_durationLabel.setText("");
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
		// update waypoints and photos if necessary
		if ((inUpdateType |
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.WAYPOINTS_MODIFIED)) > 0)
		{
			_waypointListModel.fireChanged();
		}
		if ((inUpdateType |
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.PHOTOS_MODIFIED)) > 0)
		{
			_photoListModel.fireChanged();
		}
		// Deselect selected waypoint if selected point has since changed
		if (_waypointList.getSelectedIndex() >= 0)
		{
			if (_trackInfo.getCurrentPoint() == null
			 || !_waypointListModel.getWaypoint(_waypointList.getSelectedIndex()).equals(_trackInfo.getCurrentPoint()))
			{
				// point is selected in list but different from current point - deselect
				_waypointList.clearSelection();
			}
		}
		// Do the same for the photos
		if (_photoList.getSelectedIndex() >= 0)
		{
			if (_trackInfo.getCurrentPoint() == null
				|| !_photoListModel.getPhoto(_photoList.getSelectedIndex()).getDataPoint().equals(_trackInfo.getCurrentPoint()))
			{
				// photo is selected in list but different from current point - deselect
				_photoList.clearSelection();
			}
		}
	}


	/**
	 * Choose the appropriate altitude units label for the specified format
	 * @param inFormat altitude format
	 * @return language-sensitive string
	 */
	private static String getAltitudeUnitsLabel(int inFormat)
	{
		if (inFormat == LABEL_POINT_ALTITUDE_FORMAT && LABEL_POINT_ALTITUDE_UNITS != null)
			return LABEL_POINT_ALTITUDE_UNITS;
		LABEL_POINT_ALTITUDE_FORMAT = inFormat;
		if (inFormat == Altitude.FORMAT_METRES)
			return " " + I18nManager.getText("units.metres.short");
		return " " + I18nManager.getText("units.feet.short");
	}


	/**
	 * Build a String to describe a time duration
	 * @param inNumSecs number of seconds
	 * @return time as a string, days, hours, mins, secs as appropriate
	 */
	private static String buildDurationString(long inNumSecs)
	{
		if (inNumSecs <= 0L) return "";
		if (inNumSecs < 60L) return "" + inNumSecs + I18nManager.getText("display.range.time.secs");
		if (inNumSecs < 3600L) return "" + (inNumSecs / 60) + I18nManager.getText("display.range.time.mins")
			+ " " + (inNumSecs % 60) + I18nManager.getText("display.range.time.secs");
		if (inNumSecs < 86400L) return "" + (inNumSecs / 60 / 60) + I18nManager.getText("display.range.time.hours")
			+ " " + ((inNumSecs / 60) % 60) + I18nManager.getText("display.range.time.mins");
		if (inNumSecs < 8640000L) return "" + (inNumSecs / 86400L) + I18nManager.getText("display.range.time.days");
		return "big";
	}


	/**
	 * Build a String to describe a distance
	 * @param inDist distance
	 * @return formatted String
	 */
	private String buildDistanceString(double inDist)
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
}
