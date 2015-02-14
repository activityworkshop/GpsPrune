package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
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
	private JLabel _altLabel = null, _nameLabel = null;
	private JLabel _timeLabel = null;

	// Range details
	private JLabel _rangeLabel = null;
	private JLabel _distanceLabel = null, _durationLabel = null;
	private JLabel _altRangeLabel = null, _updownLabel = null;

	// Photo details
	private JLabel _photoLabel = null;
	private PhotoThumbnail _photoThumbnail = null;

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

		// Point details panel
		JPanel pointDetailsPanel = new JPanel();
		pointDetailsPanel.setLayout(new BoxLayout(pointDetailsPanel, BoxLayout.Y_AXIS));
		pointDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel pointDetailsLabel = new JLabel(I18nManager.getText("details.pointdetails"));
		Font biggerFont = pointDetailsLabel.getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);
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
		_nameLabel = new JLabel("");
		pointDetailsPanel.add(_nameLabel);
		pointDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// range details panel
		JPanel rangeDetailsPanel = new JPanel();
		rangeDetailsPanel.setLayout(new BoxLayout(rangeDetailsPanel, BoxLayout.Y_AXIS));
		rangeDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel rangeDetailsLabel = new JLabel(I18nManager.getText("details.rangedetails"));
		rangeDetailsLabel.setFont(biggerFont);
		rangeDetailsPanel.add(rangeDetailsLabel);
		_rangeLabel = new JLabel(I18nManager.getText("details.norangeselection"));
		rangeDetailsPanel.add(_rangeLabel);
		_distanceLabel = new JLabel("");
		rangeDetailsPanel.add(_distanceLabel);
		_durationLabel = new JLabel("");
		rangeDetailsPanel.add(_durationLabel);
		_altRangeLabel = new JLabel("");
		rangeDetailsPanel.add(_altRangeLabel);
		_updownLabel = new JLabel("");
		rangeDetailsPanel.add(_updownLabel);
		rangeDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// range details panel
		JPanel photoDetailsPanel = new JPanel();
		photoDetailsPanel.setLayout(new BoxLayout(photoDetailsPanel, BoxLayout.Y_AXIS));
		photoDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel photoDetailsLabel = new JLabel(I18nManager.getText("details.photodetails"));
		photoDetailsLabel.setFont(biggerFont);
		photoDetailsPanel.add(photoDetailsLabel);
		_photoLabel = new JLabel(I18nManager.getText("details.nophoto"));
		photoDetailsPanel.add(_photoLabel);
		_photoThumbnail = new PhotoThumbnail();
		_photoThumbnail.setVisible(false);
		_photoThumbnail.setPreferredSize(new Dimension(100, 100));
		photoDetailsPanel.add(_photoThumbnail);

		// add the details panels to the main panel
		mainPanel.add(pointDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(rangeDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(photoDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);

		// Add units selection
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		JLabel unitsLabel = new JLabel(I18nManager.getText("details.distanceunits") + ": ");
		unitsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerPanel.add(unitsLabel);
		String[] distUnits = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.miles")};
		_unitsDropdown = new JComboBox(distUnits);
		_unitsDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dataUpdated(DataSubscriber.UNITS_CHANGED);
			}
		});
		lowerPanel.add(_unitsDropdown);
		_unitsDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(lowerPanel, BorderLayout.SOUTH);
	}


	/**
	 * Notification that Track has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
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
			String name = currentPoint.getWaypointName();
			if (name != null && !name.equals(""))
			{
				_nameLabel.setText(LABEL_POINT_WAYPOINTNAME + name);
			}
			else _nameLabel.setText("");
		}

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
		// show photo details and thumbnail
		Photo currentPhoto = _trackInfo.getPhotoList().getPhoto(_trackInfo.getSelection().getCurrentPhotoIndex());
		if (_track == null || ( (currentPoint == null || currentPoint.getPhoto() == null) && currentPhoto == null))
		{
			// no photo, hide details
			_photoLabel.setText(I18nManager.getText("details.nophoto"));
			_photoThumbnail.setVisible(false);
		}
		else
		{
			if (currentPhoto == null) {currentPhoto = currentPoint.getPhoto();}
			_photoLabel.setText(I18nManager.getText("details.photofile") + ": " + currentPhoto.getFile().getName());
			_photoThumbnail.setVisible(true);
			_photoThumbnail.setPhoto(currentPhoto);
		}
		_photoThumbnail.repaint();
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
