package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.RangeStats;
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


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public FullRangeDetails(App inApp)
	{
		super(inApp);
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
		// Do the calculations with a separate class
		RangeStats stats = new RangeStats(_app.getTrackInfo().getTrack(), selection.getStart(), selection.getEnd());

		// Number of points
		_numPointsLabel.setText("" + stats.getNumPoints());
		// Number of segments
		_numSegsLabel.setText("" + stats.getNumSegments());
		final boolean isMultiSegments = (stats.getNumSegments() > 1);
		// Set visibility of third column accordingly
		_movingDistanceLabel.setVisible(isMultiSegments);
		_movingDurationLabel.setVisible(isMultiSegments || stats.getTimestampsOutOfSequence());
		// FIXME: What to show if timestamps are out of sequence? Warning message?
		_movingClimbLabel.setVisible(isMultiSegments);
		_movingDescentLabel.setVisible(isMultiSegments);
		_movingSpeedLabel.setVisible(isMultiSegments);
		_movingPaceLabel.setVisible(isMultiSegments);
		_movingGradientLabel.setVisible(isMultiSegments);
		_movingVertSpeedLabel.setVisible(isMultiSegments);

		// Total and moving distance in current units
		final Unit distUnit = Config.getUnitSet().getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnit.getShortnameKey());
		_totalDistanceLabel.setText(DisplayUtils.roundedNumber(stats.getTotalDistance()) + " " + distUnitsStr);
		_movingDistanceLabel.setText(DisplayUtils.roundedNumber(stats.getMovingDistance()) + " " + distUnitsStr);

		// Duration
		_totalDurationLabel.setText(DisplayUtils.buildDurationString(stats.getTotalDurationInSeconds()));
		_movingDurationLabel.setText(DisplayUtils.buildDurationString(stats.getMovingDurationInSeconds()));

		// Climb and descent
		final Unit altUnit = Config.getUnitSet().getAltitudeUnit();
		final String altUnitsStr = " " + I18nManager.getText(altUnit.getShortnameKey());
		if (stats.getTotalAltitudeRange().hasRange()) {
			_totalClimbLabel.setText(stats.getTotalAltitudeRange().getClimb(altUnit) + altUnitsStr);
			_totalDescentLabel.setText(stats.getTotalAltitudeRange().getDescent(altUnit) + altUnitsStr);
		}
		else {
			_totalClimbLabel.setText("");
			_totalDescentLabel.setText("");
		}
		if (stats.getMovingAltitudeRange().hasRange()) {
			_movingClimbLabel.setText(stats.getMovingAltitudeRange().getClimb(altUnit) + altUnitsStr);
			_movingDescentLabel.setText(stats.getMovingAltitudeRange().getDescent(altUnit) + altUnitsStr);
		}
		else {
			_movingClimbLabel.setText("");
			_movingDescentLabel.setText("");
		}

		// Overall pace and speed
		final String speedUnitsStr = I18nManager.getText(Config.getUnitSet().getSpeedUnit().getShortnameKey());
		long numSecs = stats.getTotalDurationInSeconds();
		double dist = stats.getTotalDistance();
		if (numSecs > 0 && dist > 0)
		{
			_totalSpeedLabel.setText(DisplayUtils.roundedNumber(dist/numSecs*3600.0) + " " + speedUnitsStr);
			_totalPaceLabel.setText(DisplayUtils.buildDurationString((long) (numSecs/dist))
				+ " / " + distUnitsStr);
		}
		else {
			_totalSpeedLabel.setText("");
			_totalPaceLabel.setText("");
		}
		// and same for within the segments
		numSecs = stats.getMovingDurationInSeconds();
		dist = stats.getMovingDistance();
		if (numSecs > 0 && dist > 0)
		{
			_movingSpeedLabel.setText(DisplayUtils.roundedNumber(dist/numSecs*3600.0) + " " + speedUnitsStr);
			_movingPaceLabel.setText(DisplayUtils.buildDurationString((long) (numSecs/dist))
				+ " / " + distUnitsStr);
		}
		else {
			_movingSpeedLabel.setText("");
			_movingPaceLabel.setText("");
		}

		// Gradient
		if (stats.getTotalAltitudeRange().hasRange()) {
			_totalGradientLabel.setText(DisplayUtils.formatOneDp(stats.getTotalGradient()) + " %");
		}
		else {
			_totalGradientLabel.setText("");
		}
		if (stats.getMovingAltitudeRange().hasRange()) {
			_movingGradientLabel.setText(DisplayUtils.formatOneDp(stats.getMovingGradient()) + " %");
		}
		else {
			_movingGradientLabel.setText("");
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
			_maxSpeedLabel.setText(DisplayUtils.roundedNumber(maxSpeed) + " " + speedUnitsStr);
		}
		else {
			_maxSpeedLabel.setText("");
		}

		// vertical speed
		final String vertSpeedUnitsStr = I18nManager.getText(Config.getUnitSet().getVerticalSpeedUnit().getShortnameKey());
		if (stats.getMovingAltitudeRange().hasRange() && stats.getTotalDurationInSeconds() > 0)
		{
			// got an altitude and time - do totals
			_totalVertSpeedLabel.setText(DisplayUtils.roundedNumber(stats.getTotalVerticalSpeed()) + " " + vertSpeedUnitsStr);
			_movingVertSpeedLabel.setText(DisplayUtils.roundedNumber(stats.getMovingVerticalSpeed()) + " " + vertSpeedUnitsStr);
		}
		else
		{
			// no vertical speed available
			_totalVertSpeedLabel.setText("");
			_movingVertSpeedLabel.setText("");
		}
	}
}
