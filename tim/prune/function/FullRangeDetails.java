package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.AltitudeRange;
import tim.prune.data.DataPoint;
import tim.prune.data.Selection;
import tim.prune.data.Unit;
import tim.prune.gui.DisplayUtils;
import tim.prune.gui.profile.SpeedData;

/**
 * Class to show the full range details in a separate popup
 */
public class FullRangeDetails extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Label for number of points */
	private JLabel _numPointsLabel = null;
	/** Label for number of segments */
	private JLabel _numSegsLabel = null;
	/** Label for the maximum speed */
	private JLabel _maxSpeedLabel = null;

	/** Label for heading of "total" column */
	private JLabel _colTotalLabel = null;
	/** Label for heading of "segments" column */
	private JLabel _colSegmentsLabel = null;
	/** Labels for distances */
	private JLabel _totalDistanceLabel = null, _movingDistanceLabel = null;
	/** Labels for durations */
	private JLabel _totalDurationLabel = null, _movingDurationLabel = null;
	/** Labels for climbs */
	private JLabel _totalClimbLabel = null, _movingClimbLabel = null;
	/** Labels for descents */
	private JLabel _totalDescentLabel = null, _movingDescentLabel = null;
	/** Labels for pace */
	private JLabel _totalPaceLabel = null, _movingPaceLabel = null;
	/** Labels for gradient */
	private JLabel _totalGradientLabel = null, _movingGradientLabel = null;
	/** Labels for speed */
	private JLabel _totalSpeedLabel, _movingSpeedLabel = null;
	/** Labels for vertical speed */
	private JLabel _totalVertSpeedLabel, _movingVertSpeedLabel = null;

	/** Number formatter for one decimal place */
	private static final NumberFormat FORMAT_ONE_DP = NumberFormat.getNumberInstance();
	/** Flexible number formatter for different decimal places */
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public FullRangeDetails(App inApp)
	{
		super(inApp);
		FORMAT_ONE_DP.setMaximumFractionDigits(1);
		FORMAT_ONE_DP.setMinimumFractionDigits(1);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.fullrangedetails";
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
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(5, 5));
		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.fullrangedetails.intro") + ":");
		topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialogPanel.add(topLabel, BorderLayout.NORTH);

		// Details panel in middle
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new GridLayout(0, 3, 6, 2));
		midPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		// Number of points
		JLabel pointsLabel = new JLabel(I18nManager.getText("details.track.points") + ": ");
		pointsLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(pointsLabel);
		_numPointsLabel = new JLabel("100");
		midPanel.add(_numPointsLabel);
		midPanel.add(new JLabel(" "));
		// Number of segments
		JLabel segLabel = new JLabel(I18nManager.getText("details.range.numsegments") + ": ");
		segLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(segLabel);
		_numSegsLabel = new JLabel("100");
		midPanel.add(_numSegsLabel);
		midPanel.add(new JLabel(" "));
		// Maximum speed
		JLabel maxSpeedLabel = new JLabel(I18nManager.getText("details.range.maxspeed") + ": ");
		maxSpeedLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(maxSpeedLabel);
		_maxSpeedLabel = new JLabel("10 km/h");
		midPanel.add(_maxSpeedLabel);
		midPanel.add(new JLabel(" "));

		// blank row
		for (int i=0; i<3; i++) midPanel.add(new JLabel(" "));

		// Row for column headings
		midPanel.add(new JLabel(" "));
		_colTotalLabel = new JLabel(I18nManager.getText("dialog.fullrangedetails.coltotal"));
		midPanel.add(_colTotalLabel);
		_colSegmentsLabel = new JLabel(I18nManager.getText("dialog.fullrangedetails.colsegments"));
		midPanel.add(_colSegmentsLabel);

		// Distance
		JLabel distLabel = new JLabel(I18nManager.getText("fieldname.distance") + ": ");
		distLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(distLabel);
		_totalDistanceLabel = new JLabel("5 km");
		midPanel.add(_totalDistanceLabel);
		_movingDistanceLabel = new JLabel("5 km");
		midPanel.add(_movingDistanceLabel);

		// Duration
		JLabel durationLabel = new JLabel(I18nManager.getText("fieldname.duration") + ": ");
		durationLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(durationLabel);
		_totalDurationLabel = new JLabel("15 min");
		midPanel.add(_totalDurationLabel);
		_movingDurationLabel = new JLabel("15 min");
		midPanel.add(_movingDurationLabel);

		// Speed
		JLabel speedLabel = new JLabel(I18nManager.getText("details.range.avespeed") + ": ");
		speedLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(speedLabel);
		_totalSpeedLabel = new JLabel("5.5 km/h");
		midPanel.add(_totalSpeedLabel);
		_movingSpeedLabel = new JLabel("5.5 km/h");
		midPanel.add(_movingSpeedLabel);

		// Pace
		JLabel paceLabel = new JLabel(I18nManager.getText("details.range.pace") + ": ");
		paceLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(paceLabel);
		_totalPaceLabel = new JLabel("8 min/km");
		midPanel.add(_totalPaceLabel);
		_movingPaceLabel = new JLabel("8 min/km");
		midPanel.add(_movingPaceLabel);

		// Climb
		JLabel climbLabel = new JLabel(I18nManager.getText("details.range.climb") + ": ");
		climbLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(climbLabel);
		_totalClimbLabel = new JLabel("1000 m");
		midPanel.add(_totalClimbLabel);
		_movingClimbLabel = new JLabel("1000 m");
		midPanel.add(_movingClimbLabel);
		// Descent
		JLabel descentLabel = new JLabel(I18nManager.getText("details.range.descent") + ": ");
		descentLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(descentLabel);
		_totalDescentLabel = new JLabel("1000 m");
		midPanel.add(_totalDescentLabel);
		_movingDescentLabel = new JLabel("1000 m");
		midPanel.add(_movingDescentLabel);

		// Gradient
		JLabel gradientLabel = new JLabel(I18nManager.getText("details.range.gradient") + ": ");
		gradientLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(gradientLabel);
		_totalGradientLabel = new JLabel("10 %");
		midPanel.add(_totalGradientLabel);
		_movingGradientLabel = new JLabel("10 %");
		midPanel.add(_movingGradientLabel);

		// Vertical speed
		JLabel vSpeedLabel = new JLabel(I18nManager.getText("fieldname.verticalspeed") + ": ");
		vSpeedLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(vSpeedLabel);
		_totalVertSpeedLabel = new JLabel("1 m/s");
		midPanel.add(_totalVertSpeedLabel);
		_movingVertSpeedLabel = new JLabel("1 m/s");
		midPanel.add(_movingVertSpeedLabel);

		dialogPanel.add(midPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		closeButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
				super.keyPressed(inE);
			}
		});
		buttonPanel.add(closeButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Update the labels with the current details
	 */
	private void updateDetails()
	{
		Selection selection = _app.getTrackInfo().getSelection();
		// Number of points
		_numPointsLabel.setText("" + (selection.getEnd()-selection.getStart()+1));
		// Number of segments
		_numSegsLabel.setText("" + selection.getNumSegments());
		final boolean isMultiSegments = (selection.getNumSegments() > 1);
		// Set visibility of third column accordingly
		_movingDistanceLabel.setVisible(isMultiSegments);
		_movingDurationLabel.setVisible(isMultiSegments);
		_movingClimbLabel.setVisible(isMultiSegments);
		_movingDescentLabel.setVisible(isMultiSegments);
		_movingSpeedLabel.setVisible(isMultiSegments);
		_movingPaceLabel.setVisible(isMultiSegments);
		_movingGradientLabel.setVisible(isMultiSegments);
		_movingVertSpeedLabel.setVisible(isMultiSegments);

		// Distance in current units
		final Unit distUnit = Config.getUnitSet().getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnit.getShortnameKey());
		final double selectionDistance = selection.getDistance();
		_totalDistanceLabel.setText(roundedNumber(selectionDistance) + " " + distUnitsStr);

		// Duration
		long numSecs = selection.getNumSeconds();
		_totalDurationLabel.setText(DisplayUtils.buildDurationString(numSecs));
		// Climb and descent
		final Unit altUnit = Config.getUnitSet().getAltitudeUnit();
		final String altUnitsStr = " " + I18nManager.getText(altUnit.getShortnameKey());
		if (selection.getAltitudeRange().hasRange()) {
			_totalClimbLabel.setText(selection.getAltitudeRange().getClimb(altUnit) + altUnitsStr);
			_totalDescentLabel.setText(selection.getAltitudeRange().getDescent(altUnit) + altUnitsStr);
		}
		else {
			_totalClimbLabel.setText("");
			_totalDescentLabel.setText("");
		}

		// Overall pace and speed
		final String speedUnitsStr = I18nManager.getText(Config.getUnitSet().getSpeedUnit().getShortnameKey());
		if (numSecs > 0 && selectionDistance > 0)
		{
			_totalPaceLabel.setText(
				DisplayUtils.buildDurationString((long) (numSecs/selectionDistance))
				+ " / " + distUnitsStr);
			_totalSpeedLabel.setText(roundedNumber(selectionDistance/numSecs*3600.0)
				+ " " + speedUnitsStr);
		}
		else {
			_totalPaceLabel.setText("");
			_totalSpeedLabel.setText("");
		}

		// Moving distance
		double movingDist = selection.getMovingDistance();
		_movingDistanceLabel.setText(roundedNumber(movingDist) + " " + distUnitsStr);
		// Moving average speed
		long numMovingSecs = selection.getMovingSeconds();
		if (numMovingSecs > 0)
		{
			_movingDurationLabel.setText(DisplayUtils.buildDurationString(numMovingSecs));
			_movingSpeedLabel.setText(roundedNumber(movingDist/numMovingSecs*3600.0)
				+ " " + speedUnitsStr);
			_movingPaceLabel.setText(
				DisplayUtils.buildDurationString((long) (numMovingSecs/movingDist))
				+ " / " + distUnitsStr);
		}
		else
		{
			_movingDurationLabel.setText("");
			_movingSpeedLabel.setText("");
			_movingPaceLabel.setText("");
		}

		// Moving gradient and moving climb/descent
		Altitude firstAlt = null, lastAlt = null;
		Altitude veryFirstAlt = null, veryLastAlt = null;
		AltitudeRange altRange = new AltitudeRange();
		double movingHeightDiff = 0.0;
		if (movingDist > 0.0)
		{
			for (int pNum = selection.getStart(); pNum <= selection.getEnd(); pNum++)
			{
				DataPoint p = _app.getTrackInfo().getTrack().getPoint(pNum);
				if (p != null && !p.isWaypoint())
				{
					// If we're starting a new segment, calculate the height diff of the previous one
					if (p.getSegmentStart())
					{
						if (firstAlt != null && firstAlt.isValid() && lastAlt != null && lastAlt.isValid())
							movingHeightDiff = movingHeightDiff + lastAlt.getMetricValue() - firstAlt.getMetricValue();
						firstAlt = null; lastAlt = null;
					}
					Altitude alt = p.getAltitude();
					if (alt != null && alt.isValid())
					{
						if (firstAlt == null) firstAlt = alt;
						else lastAlt = alt;
						if (veryFirstAlt == null) veryFirstAlt = alt;
						else veryLastAlt = alt;
					}
					// Keep track of climb and descent too
					if (p.getSegmentStart())
						altRange.ignoreValue(alt);
					else
						altRange.addValue(alt);
				}
			}
			// deal with last segment
			if (firstAlt != null && firstAlt.isValid() && lastAlt != null && lastAlt.isValid())
				movingHeightDiff = movingHeightDiff + lastAlt.getMetricValue() - firstAlt.getMetricValue();
			final double metricMovingDist = movingDist / distUnit.getMultFactorFromStd(); // convert back to metres
			final double gradient = movingHeightDiff * 100.0 / metricMovingDist;
			_movingGradientLabel.setText(FORMAT_ONE_DP.format(gradient) + " %");
		}
		if (!altRange.hasRange()) {
			_movingGradientLabel.setText("");
		}
		final boolean hasAltitudes = veryFirstAlt != null && veryFirstAlt.isValid() && veryLastAlt != null && veryLastAlt.isValid();

		// Total gradient
		final double metreDist = selection.getDistance() / distUnit.getMultFactorFromStd(); // convert back to metres
		if (hasAltitudes && metreDist > 0.0)
		{
			// got an altitude and range
			int altDiffInMetres = veryLastAlt.getValue(Altitude.Format.METRES) - veryFirstAlt.getValue(Altitude.Format.METRES);
			double gradient = altDiffInMetres * 100.0 / metreDist;
			_totalGradientLabel.setText(FORMAT_ONE_DP.format(gradient) + " %");
		}
		else {
			// no altitude given
			_totalGradientLabel.setText("");
		}

		// Moving climb/descent
		if (altRange.hasRange()) {
			_movingClimbLabel.setText(altRange.getClimb(altUnit) + altUnitsStr);
			_movingDescentLabel.setText(altRange.getDescent(altUnit) + altUnitsStr);
		}
		else {
			_movingClimbLabel.setText("");
			_movingDescentLabel.setText("");
		}
		// Maximum speed
		SpeedData speeds = new SpeedData(_app.getTrackInfo().getTrack());
		speeds.init(Config.getUnitSet());
		double maxSpeed = 0.0;
		for (int i=selection.getStart(); i<=selection.getEnd(); i++)
		{
			if (speeds.hasData(i) && (speeds.getData(i) > maxSpeed)) {
				maxSpeed = speeds.getData(i);
			}
		}
		if (maxSpeed > 0.0) {
			_maxSpeedLabel.setText(roundedNumber(maxSpeed) + " " + speedUnitsStr);
		}
		else {
			_maxSpeedLabel.setText("");
		}

		// vertical speed
		final String vertSpeedUnitsStr = I18nManager.getText(Config.getUnitSet().getVerticalSpeedUnit().getShortnameKey());
		if (hasAltitudes && metreDist > 0.0 && numSecs > 0)
		{
			// got an altitude and time - do total
			final int altDiffInMetres = veryLastAlt.getValue(Altitude.Format.METRES) - veryFirstAlt.getValue(Altitude.Format.METRES);
			final double altDiff = altDiffInMetres * altUnit.getMultFactorFromStd();
			_totalVertSpeedLabel.setText(roundedNumber(altDiff/numSecs) + " " + vertSpeedUnitsStr);
			// and moving
			_movingVertSpeedLabel.setText(roundedNumber(movingHeightDiff * altUnit.getMultFactorFromStd() / numMovingSecs) + " " + vertSpeedUnitsStr);
		}
		else {
			// no vertical speed available
			_totalVertSpeedLabel.setText("");
			_movingVertSpeedLabel.setText("");
		}
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
}
