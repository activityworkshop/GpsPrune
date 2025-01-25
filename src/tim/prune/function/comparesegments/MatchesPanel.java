package tim.prune.function.comparesegments;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Distance;
import tim.prune.data.Unit;
import tim.prune.data.UnitSet;
import tim.prune.gui.DisplayUtils;

/** Class to hold the gui elements for the matches panel on the output card */
public class MatchesPanel extends JPanel
{
	private final JLabel _numMatches;
	private final JLabel _distance1;
	private final JLabel _distance2;
	private final JLabel _duration1;
	private final JLabel _duration2;
	private final JLabel _aveSpeed1;
	private final JLabel _aveSpeed2;

	public MatchesPanel()
	{
		setLayout(new GridLayout(0, 3, 6, 2));
		setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.comparesegments.matches")));

		// Number of matches
		JLabel startLabel = new JLabel(I18nManager.getText("dialog.comparesegments.nummatches") + ": ");
		startLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(startLabel);
		_numMatches = new JLabel("1234");
		add(_numMatches);
		add(new JLabel(""));

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

		// Average speeds
		JLabel speedLabel = new JLabel(I18nManager.getText("details.range.avespeed") + ": ");
		speedLabel.setHorizontalAlignment(JLabel.RIGHT);
		add(speedLabel);
		_aveSpeed1 = new JLabel("11.3 km/h");
		add(_aveSpeed1);
		_aveSpeed2 = new JLabel("12.3 km/h");
		add(_aveSpeed2);
	}

	public void setDetails(List<IntersectionResult> inResults, Config inConfig)
	{
		// Check if inResults has at least 2 entries
		_numMatches.setText("" + inResults.size());
		if (inResults.size() < 2) {
			clearDetails();
			return;
		}
		final UnitSet unitSet = inConfig.getUnitSet();
		final Unit distUnits = unitSet.getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnits.getShortnameKey());
		IntersectionResult firstResult = inResults.get(0);
		IntersectionResult lastResult = inResults.get(inResults.size() - 1);

		// distances
		final double dist1Rads = lastResult.getFirstDistanceRadians(firstResult);
		final double dist1Units = Distance.convertRadiansToDistance(dist1Rads, distUnits);
		setDistance(dist1Units, distUnitsStr, _distance1);
		final double dist2Rads = lastResult.getSecondDistanceRadians(firstResult);
		final double dist2Units = Distance.convertRadiansToDistance(dist2Rads, distUnits);
		setDistance(dist2Units, distUnitsStr, _distance2);
		// durations
		final long seconds1 = lastResult.getFirstDurationSeconds(firstResult);
		_duration1.setText(DisplayUtils.buildDurationString(seconds1));
		final long seconds2 = lastResult.getSecondDurationSeconds(firstResult);
		_duration2.setText(DisplayUtils.buildDurationString(seconds2));
		// Average speeds
		String speedUnitsStr = I18nManager.getText(unitSet.getSpeedUnit().getShortnameKey());
		_aveSpeed1.setText(DisplayUtils.roundedNumber(dist1Units / seconds1 * 3600.0) + " " + speedUnitsStr);
		_aveSpeed2.setText(DisplayUtils.roundedNumber(dist2Units / seconds2 * 3600.0) + " " + speedUnitsStr);
	}

	private void clearDetails()
	{
		_distance1.setText("--.--");
		_distance2.setText("--.--");
		_duration1.setText("--.--");
		_duration2.setText("--.--");
		_aveSpeed1.setText("--.--");
		_aveSpeed2.setText("--.--");
	}

	private static void setDistance(double inDist, String inDistUnitsStr, JLabel inLabel) {
		inLabel.setText(DisplayUtils.roundedNumber(inDist) + " " + inDistUnitsStr);
	}
}
