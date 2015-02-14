package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import tim.prune.data.Distance;
import tim.prune.data.Selection;
import tim.prune.gui.DisplayUtils;
import tim.prune.gui.profile.SpeedData;

/**
 * Class to show the full range details in a separate popup
 */
public class FullRangeDetails extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Label for number of segments */
	private JLabel _numSegsLabel = null;
	/** Label for pace */
	private JLabel _paceLabel = null;
	/** Label for gradient */
	private JLabel _gradientLabel = null;
	/** Moving distance, speed */
	private JLabel _movingDistanceLabel = null, _aveMovingSpeedLabel = null;
	private JLabel _maxSpeedLabel = null;
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
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.fullrangedetails.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialogPanel.add(topLabel, BorderLayout.NORTH);

		// Details panel in middle
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new GridLayout(0, 2, 6, 2));
		midPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		// Number of segments
		JLabel segLabel = new JLabel(I18nManager.getText("details.range.numsegments") + ": ");
		segLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(segLabel);
		_numSegsLabel = new JLabel("100");
		midPanel.add(_numSegsLabel);
		// Pace
		JLabel paceLabel = new JLabel(I18nManager.getText("details.range.pace") + ": ");
		paceLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(paceLabel);
		_paceLabel = new JLabel("8 min/km");
		midPanel.add(_paceLabel);
		// Gradient
		JLabel gradientLabel = new JLabel(I18nManager.getText("details.range.gradient") + ": ");
		gradientLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(gradientLabel);
		_gradientLabel = new JLabel("10 %");
		midPanel.add(_gradientLabel);
		// Moving distance
		JLabel movingDistLabel = new JLabel(I18nManager.getText("fieldname.movingdistance") + ": ");
		movingDistLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(movingDistLabel);
		_movingDistanceLabel = new JLabel("5 km");
		midPanel.add(_movingDistanceLabel);
		// Moving speed
		JLabel movingSpeedLabel = new JLabel(I18nManager.getText("details.range.avemovingspeed") + ": ");
		movingSpeedLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(movingSpeedLabel);
		_aveMovingSpeedLabel = new JLabel("5 km/h");
		midPanel.add(_aveMovingSpeedLabel);
		// Maximum speed
		JLabel maxSpeedLabel = new JLabel(I18nManager.getText("details.range.maxspeed") + ": ");
		maxSpeedLabel.setHorizontalAlignment(JLabel.RIGHT);
		midPanel.add(maxSpeedLabel);
		_maxSpeedLabel = new JLabel("10 km/h");
		midPanel.add(_maxSpeedLabel);

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
		// Number of segments
		_numSegsLabel.setText("" + selection.getNumSegments());
		// Pace value
		if (selection.getNumSeconds() > 0)
		{
			boolean useMetric = Config.getConfigBoolean(Config.KEY_METRIC_UNITS);
			Distance.Units distUnits = useMetric?Distance.Units.KILOMETRES:Distance.Units.MILES;
			String distUnitsStr = I18nManager.getText(useMetric?"units.kilometres.short":"units.miles.short");
			_paceLabel.setText(DisplayUtils.buildDurationString(
					(long) (selection.getNumSeconds()/selection.getDistance(distUnits)))
				+ " / " + distUnitsStr);
		}
		else {
			_paceLabel.setText("");
		}
		// Gradient
		Altitude firstAlt = _app.getTrackInfo().getTrack().getPoint(selection.getStart()).getAltitude();
		Altitude lastAlt = _app.getTrackInfo().getTrack().getPoint(selection.getEnd()).getAltitude();
		double metreDist = selection.getDistance(Distance.Units.METRES);
		if (firstAlt.isValid() && lastAlt.isValid() && metreDist > 0.0)
		{
			// got an altitude and range
			int altDiffInMetres = lastAlt.getValue(Altitude.Format.METRES) - firstAlt.getValue(Altitude.Format.METRES);
			double gradient = altDiffInMetres * 100.0 / metreDist;
			_gradientLabel.setText(FORMAT_ONE_DP.format(gradient) + " %");
		}
		else {
			// no altitude given
			_gradientLabel.setText("");
		}

		// Show moving distance and average even when number of segments is 1
		final boolean isMetric = Config.getConfigBoolean(Config.KEY_METRIC_UNITS);
		final Distance.Units distUnits = isMetric?Distance.Units.KILOMETRES:Distance.Units.MILES;
		final String distUnitsStr = I18nManager.getText(isMetric?"units.kilometres.short":"units.miles.short");
		final String speedUnitsStr = I18nManager.getText(isMetric?"units.kmh":"units.mph");
		// Moving distance
		_movingDistanceLabel.setText(roundedNumber(selection.getMovingDistance(distUnits)) + " " + distUnitsStr);
		// Moving average speed
		long numSecs = selection.getMovingSeconds();
		if (numSecs > 0) {
			_aveMovingSpeedLabel.setText(roundedNumber(selection.getMovingDistance(distUnits)/numSecs*3600.0)
				+ " " + speedUnitsStr);
		}
		else {
			_aveMovingSpeedLabel.setText("");
		}

		// Maximum speed
		SpeedData speeds = new SpeedData(_app.getTrackInfo().getTrack());
		speeds.init();
		double maxSpeed = 0.0;
		for (int i=selection.getStart(); i<=selection.getEnd(); i++) {
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
