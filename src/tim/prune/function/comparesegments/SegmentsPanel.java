package tim.prune.function.comparesegments;

import java.awt.GridLayout;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.Distance;
import tim.prune.data.Unit;
import tim.prune.gui.DisplayUtils;

/** Class to hold the gui elements for the segments panel on the output card */
public class SegmentsPanel extends JPanel
{
	private JLabel _segStartDate1 = null, _segStartDate2 = null;
	private JLabel _segStartTime1 = null, _segStartTime2 = null;
	private JLabel _numPoints1 = null, _numPoints2 = null;
	private JLabel _distance1 = null, _distance2 = null;
	private JLabel _duration1 = null, _duration2 = null;

	public SegmentsPanel()
	{
		setLayout(new GridLayout(0, 3, 6, 2));
		setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.comparesegments.segments")));

		// Start dates and times
		JLabel startLabel = new JLabel(I18nManager.getText("dialog.comparesegments.startdate") + ": ");
		startLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(startLabel);
		_segStartDate1 = new JLabel("2000-01-01");
		add(_segStartDate1);
		_segStartDate2 = new JLabel("2002-02-02");
		add(_segStartDate2);
		startLabel = new JLabel(I18nManager.getText("dialog.comparesegments.starttime") + ": ");
		startLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(startLabel);
		_segStartTime1 = new JLabel("10:12:14");
		add(_segStartTime1);
		_segStartTime2 = new JLabel("10:12:14");
		add(_segStartTime2);

		// Numbers of points
		JLabel pointsLabel = new JLabel(I18nManager.getText("details.track.points") + ": ");
		pointsLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(pointsLabel);
		_numPoints1 = new JLabel("1234");
		add(_numPoints1);
		_numPoints2 = new JLabel("2341");
		add(_numPoints2);

		// Distances
		JLabel distanceLabel = new JLabel(I18nManager.getText("fieldname.distance") + ": ");
		distanceLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(distanceLabel);
		_distance1 = new JLabel("11.9 km");
		add(_distance1);
		_distance2 = new JLabel("12.9 km");
		add(_distance2);

		// Durations
		JLabel durationLabel = new JLabel(I18nManager.getText("fieldname.duration") + ": ");
		durationLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(durationLabel);
		_duration1 = new JLabel("11m 12s");
		add(_duration1);
		_duration2 = new JLabel("12m 13s");
		add(_duration2);
	}

	public void setDetails(SegmentData inSegment1, SegmentData inSegment2, Config inConfig)
	{
		final TimeZone timezone = TimezoneHelper.getSelectedTimezone(inConfig);
		final PointSequence points1 = inSegment1.getPoints();
		setDate(points1, _segStartDate1, timezone);
		setTime(points1, _segStartTime1, timezone);
		setNumPoints(points1, _numPoints1);
		final PointSequence points2 = inSegment2.getPoints();
		setDate(points2, _segStartDate2, timezone);
		setTime(points2, _segStartTime2, timezone);
		setNumPoints(points2, _numPoints2);
		final Unit distUnits = inConfig.getUnitSet().getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnits.getShortnameKey());
		PointData lastPoint1 = points1.getLastPoint();
		setDistance(lastPoint1, distUnits, distUnitsStr, _distance1);
		PointData lastPoint2 = points2.getLastPoint();
		setDistance(lastPoint2, distUnits, distUnitsStr, _distance2);
		setDuration(points1.getFirstPoint(), lastPoint1, _duration1);
		setDuration(points2.getFirstPoint(), lastPoint2, _duration2);
	}

	private static void setDate(PointSequence inPoints, JLabel inLabel, TimeZone inTimezone) {
		inLabel.setText(inPoints.getFirstTimestamp().getDateText(inTimezone));
	}

	private static void setTime(PointSequence inPoints, JLabel inLabel, TimeZone inTimezone) {
		inLabel.setText(inPoints.getFirstTimestamp().getTimeText(inTimezone));
	}

	private static void setNumPoints(PointSequence inPoints, JLabel inLabel) {
		inLabel.setText("" + inPoints.getNumPoints());
	}

	private static void setDistance(PointData inLastPoint, Unit inDistUnits,
		String inDistUnitsStr, JLabel inLabel)
	{
		final double dist = Distance.convertRadiansToDistance(inLastPoint._distanceToHereRadians, inDistUnits);
		inLabel.setText(DisplayUtils.roundedNumber(dist) + " " + inDistUnitsStr);
	}

	private static void setDuration(PointData inFirstPoint, PointData inLastPoint, JLabel inLabel)
	{
		final long seconds = inLastPoint._point.getTimestamp().getSecondsSince(inFirstPoint._point.getTimestamp());
		inLabel.setText(DisplayUtils.buildDurationString(seconds));
	}
}
