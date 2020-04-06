package tim.prune.function.estimate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.estimate.jama.Matrix;
import tim.prune.gui.ProgressDialog;

/**
 * Function to learn the estimation parameters from the current track
 */
public class LearnParameters extends GenericFunction implements Runnable
{
	/** Progress dialog */
	ProgressDialog _progress = null;
	/** Results dialog */
	JDialog _dialog = null;
	/** Calculated parameters */
	private ParametersPanel _calculatedParamPanel = null;
	private EstimationParameters _calculatedParams = null;
	/** Slider for weighted average */
	private JScrollBar _weightSlider = null;
	/** Label to describe position of slider */
	private JLabel _sliderDescLabel = null;
	/** Combined parameters */
	private ParametersPanel _combinedParamPanel = null;
	/** Combine button */
	private JButton _combineButton = null;


	/**
	 * Inner class used to hold the results of the matrix solving
	 */
	static class MatrixResults
	{
		public EstimationParameters _parameters = null;
		public double _averageErrorPc = 0.0; // percentage
	}


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public LearnParameters(App inApp)
	{
		super(inApp);
	}

	/** @return key for function name */
	public String getNameKey() {
		return "function.learnestimationparams";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Show progress bar
		if (_progress == null) {
			_progress = new ProgressDialog(_parentFrame, getNameKey());
		}
		_progress.show();
		// Start new thread for the calculations
		new Thread(this).start();
	}

	/**
	 * Run method in separate thread
	 */
	public void run()
	{
		_progress.setMaximum(100);
		// Go through the track and collect the range stats for each sample
		ArrayList<RangeStatsWithGradients> statsList = new ArrayList<RangeStatsWithGradients>(20);
		Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		final int sampleSize = numPoints / 30;
		int prevStartIndex = -1;
		for (int i=0; i<30; i++)
		{
			int startIndex = i * sampleSize;
			RangeStatsWithGradients stats = getRangeStats(track, startIndex, startIndex + sampleSize, prevStartIndex);
			if (stats != null && stats.getMovingDistanceKilometres() > 1.0
				&& !stats.getTimestampsIncomplete() && !stats.getTimestampsOutOfSequence()
				&& stats.getTotalDurationInSeconds() > 100
				&& startIndex > prevStartIndex)
			{
				// System.out.println("Got stats for " + stats.getStartIndex() + " to " + stats.getEndIndex());
				statsList.add(stats);
				prevStartIndex = startIndex;
			}
			_progress.setValue(i);
		}

		// Check if we've got enough samples
		// System.out.println("Got a total of " + statsList.size() + " samples");
		if (statsList.size() < 10)
		{
			_progress.dispose();
			// Show error message, not enough samples
			_app.showErrorMessage(getNameKey(), "error.learnestimationparams.failed");
			return;
		}
		// Loop around, solving the matrices and removing the highest-error sample
		MatrixResults results = reduceSamples(statsList);
		if (results == null)
		{
			_progress.dispose();
			_app.showErrorMessage(getNameKey(), "error.learnestimationparams.failed");
			return;
		}

		_progress.dispose();

		// Create the dialog if necessary
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			// Create Gui and show it
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}

		// Populate the values in the dialog
		populateCalculatedValues(results);
		updateCombinedLabels(calculateCombinedParameters());
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		// main panel with a box layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Label at top
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.learnestimationparams.intro") + ":");
		introLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		introLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(introLabel);

		// Panel for the calculated results
		_calculatedParamPanel = new ParametersPanel("dialog.estimatetime.results", true);
		_calculatedParamPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(_calculatedParamPanel);
		mainPanel.add(Box.createVerticalStrut(14));

		mainPanel.add(new JLabel(I18nManager.getText("dialog.learnestimationparams.combine") + ":"));
		mainPanel.add(Box.createVerticalStrut(4));
		_weightSlider = new JScrollBar(JScrollBar.HORIZONTAL, 5, 1, 0, 11);
		_weightSlider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent inEvent)
			{
				if (!inEvent.getValueIsAdjusting()) {
					updateCombinedLabels(calculateCombinedParameters());
				}
			}
		});
		mainPanel.add(_weightSlider);
		_sliderDescLabel = new JLabel(" ");
		_sliderDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(_sliderDescLabel);
		mainPanel.add(Box.createVerticalStrut(12));

		// Results panel
		_combinedParamPanel = new ParametersPanel("dialog.learnestimationparams.combinedresults");
		_combinedParamPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(_combinedParamPanel);

		dialogPanel.add(mainPanel, BorderLayout.NORTH);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// Combine
		_combineButton = new JButton(I18nManager.getText("button.combine"));
		_combineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				combineAndFinish();
			}
		});
		buttonPanel.add(_combineButton);

		// Cancel
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		KeyAdapter escapeListener = new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		};
		_combineButton.addKeyListener(escapeListener);
		cancelButton.addKeyListener(escapeListener);
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Construct a rangestats object for the selected range
	 * @param inTrack track object
	 * @param inStartIndex start index
	 * @param inEndIndex end index
	 * @param inPreviousStartIndex the previously used start index, or -1
	 * @return range stats object or null if required information missing from this bit of the track
	 */
	private RangeStatsWithGradients getRangeStats(Track inTrack, int inStartIndex,
		int inEndIndex, int inPreviousStartIndex)
	{
		// Check parameters
		if (inTrack == null || inStartIndex < 0 || inEndIndex <= inStartIndex || inStartIndex > inTrack.getNumPoints()) {
			return null;
		}
		final int numPoints = inTrack.getNumPoints();
		int start = inStartIndex;

		// Search forward until a decent track point found for the start
		DataPoint p = inTrack.getPoint(start);
		while (start < numPoints && (p == null || p.isWaypoint() || !p.hasTimestamp() || !p.hasAltitude()))
		{
			start++;
			p = inTrack.getPoint(start);
		}
		if (inPreviousStartIndex >= 0 && start <= (inPreviousStartIndex + 10) // overlapping too much with previous range
			|| (start >= (numPoints - 10))) // starting too late in the track
		{
			return null;
		}

		// Search forward (counting the radians) until a decent end point found
		double movingRads = 0.0;
		final double minimumRads = Distance.convertDistanceToRadians(1.0, UnitSetLibrary.UNITS_KILOMETRES);
		DataPoint prevPoint = inTrack.getPoint(start);
		int endIndex = start;
		boolean shouldStop = false;
		do
		{
			endIndex++;
			p = inTrack.getPoint(endIndex);
			if (p != null && !p.isWaypoint())
			{
				if (!p.hasAltitude() || !p.hasTimestamp()) {return null;} // abort if no time/altitude
				if (prevPoint != null && !p.getSegmentStart()) {
					movingRads += DataPoint.calculateRadiansBetween(prevPoint, p);
				}
			}
			prevPoint = p;
			if (endIndex >= numPoints) {
				shouldStop = true; // reached the end of the track
			}
			else if (movingRads >= minimumRads && endIndex >= inEndIndex) {
				shouldStop = true; // got at least a kilometre
			}
		}
		while (!shouldStop);

		// Check moving distance
		if (movingRads >= minimumRads) {
			return new RangeStatsWithGradients(inTrack, start, endIndex);
		}
		return null;
	}

	/**
	 * Build an A matrix for the given list of RangeStats objects
	 * @param inStatsList list of (non-null) RangeStats objects
	 * @return A matrix with n rows and 5 columns
	 */
	private static Matrix buildAMatrix(ArrayList<RangeStatsWithGradients> inStatsList)
	{
		final Unit METRES = UnitSetLibrary.UNITS_METRES;
		Matrix result = new Matrix(inStatsList.size(), 5);
		int row = 0;
		for (RangeStatsWithGradients stats : inStatsList)
		{
			result.setValue(row, 0, stats.getMovingDistanceKilometres());
			result.setValue(row, 1, stats.getGentleAltitudeRange().getClimb(METRES));
			result.setValue(row, 2, stats.getSteepAltitudeRange().getClimb(METRES));
			result.setValue(row, 3, stats.getGentleAltitudeRange().getDescent(METRES));
			result.setValue(row, 4, stats.getSteepAltitudeRange().getDescent(METRES));
			row++;
		}
		return result;
	}

	/**
	 * Build a B matrix containing the observations (moving times)
	 * @param inStatsList list of (non-null) RangeStats objects
	 * @return B matrix with single column of n rows
	 */
	private static Matrix buildBMatrix(ArrayList<RangeStatsWithGradients> inStatsList)
	{
		Matrix result = new Matrix(inStatsList.size(), 1);
		int row = 0;
		for (RangeStatsWithGradients stats : inStatsList)
		{
			result.setValue(row, 0, stats.getMovingDurationInSeconds() / 60.0); // convert seconds to minutes
			row++;
		}
		return result;
	}

	/**
	 * Look for the maximum absolute value in the given column matrix
	 * @param inMatrix matrix with only one column
	 * @return row index of cell with greatest absolute value, or -1 if not valid
	 */
	private static int getIndexOfMaxValue(Matrix inMatrix)
	{
		if (inMatrix == null || inMatrix.getNumColumns() > 1) {
			return -1;
		}
		int index = 0;
		double currValue = 0.0, maxValue = 0.0;
		// Loop over the first column looking for the maximum absolute value
		for (int i=0; i<inMatrix.getNumRows(); i++)
		{
			currValue = Math.abs(inMatrix.get(i, 0));
			if (currValue > maxValue)
			{
				maxValue = currValue;
				index = i;
			}
		}
		return index;
	}

	/**
	 * See if the given set of samples is sufficient for getting a descent solution (at least 3 nonzero values)
	 * @param inRangeSet list of RangeStats objects
	 * @param inRowToIgnore row index to ignore, or -1 to use them all
	 * @return true if the samples look ok
	 */
	private static boolean isRangeSetSufficient(ArrayList<RangeStatsWithGradients> inRangeSet, int inRowToIgnore)
	{
		// number of samples with gentle/steep climb/descent values > 0
		int numGC = 0, numSC = 0, numGD = 0, numSD = 0;
		final Unit METRES = UnitSetLibrary.UNITS_METRES;
		int i = 0;
		for (RangeStatsWithGradients stats : inRangeSet)
		{
			if (i != inRowToIgnore)
			{
				if (stats.getGentleAltitudeRange().getClimb(METRES) > 0) {numGC++;}
				if (stats.getSteepAltitudeRange().getClimb(METRES) > 0)  {numSC++;}
				if (stats.getGentleAltitudeRange().getDescent(METRES) > 0) {numGD++;}
				if (stats.getSteepAltitudeRange().getDescent(METRES) > 0)  {numSD++;}
			}
			i++;
		}
		return numGC > 3 && numSC > 3 && numGD > 3 && numSD > 3;
	}

	/**
	 * Reduce the number of samples in the given list by eliminating the ones with highest errors
	 * @param inStatsList list of stats
	 * @return results in an object
	 */
	private MatrixResults reduceSamples(ArrayList<RangeStatsWithGradients> inStatsList)
	{
		int statsIndexToRemove = -1;
		Matrix answer = null;
		boolean finished = false;
		double averageErrorPc = 0.0;
		while (!finished)
		{
			// Remove the marked stats object, if any
			if (statsIndexToRemove >= 0) {
				inStatsList.remove(statsIndexToRemove);
			}

			// Build up the matrices
			Matrix A = buildAMatrix(inStatsList);
			Matrix B = buildBMatrix(inStatsList);
			// System.out.println("Times in minutes are:\n" + B.toString());

			// Solve (if possible)
			try
			{
				answer = A.solve(B);
				// System.out.println("Solved matrix with " + A.getNumRows() + " rows:\n" + answer.toString());
				// Work out the percentage error for each estimate
				Matrix estimates = A.times(answer);
				Matrix errors = estimates.minus(B).divideEach(B);
				// System.out.println("Errors: " + errors.toString());
				averageErrorPc = errors.getAverageAbsValue();
				// find biggest percentage error, remove it from list
				statsIndexToRemove = getIndexOfMaxValue(errors);
				if (statsIndexToRemove < 0)
				{
					System.err.println("Something wrong - index is " + statsIndexToRemove);
					throw new Exception();
				}
				// Check whether removing this element would make the range set insufficient
				finished = inStatsList.size() <= 25 || !isRangeSetSufficient(inStatsList, statsIndexToRemove);
			}
			catch (Exception e)
			{
				// Couldn't solve at all
				System.out.println("Failed to reduce: " + e.getClass().getName() + " - " + e.getMessage());
				return null;
			}
			_progress.setValue(20 + 80 * (30 - inStatsList.size())/5); // Counting from 30 to 25
		}
		// Copy results to an EstimationParameters object
		MatrixResults result = new MatrixResults();
		result._parameters = new EstimationParameters();
		result._parameters.populateWithMetrics(answer.get(0, 0) * 5, // convert from 1km to 5km
			answer.get(1, 0) * 100.0, answer.get(2, 0) * 100.0,      // convert from m to 100m
			answer.get(3, 0) * 100.0, answer.get(4, 0) * 100.0);
		result._averageErrorPc = averageErrorPc;
		return result;
	}


	/**
	 * Populate the dialog's labels with the calculated values
	 * @param inResults results of the calculations
	 */
	private void populateCalculatedValues(MatrixResults inResults)
	{
		if (inResults == null || inResults._parameters == null)
		{
			_calculatedParams = null;
			_calculatedParamPanel.updateParameters(null, 0.0);
		}
		else
		{
			_calculatedParams = inResults._parameters;
			_calculatedParamPanel.updateParameters(_calculatedParams, inResults._averageErrorPc);
		}
	}

	/**
	 * Combine the calculated parameters with the existing ones
	 * according to the value of the slider
	 * @return combined parameters
	 */
	private EstimationParameters calculateCombinedParameters()
	{
		final double fraction1 = 1 - 0.1 * _weightSlider.getValue(); // slider left = value 0 = fraction 1 = keep current
		EstimationParameters oldParams = new EstimationParameters(Config.getConfigString(Config.KEY_ESTIMATION_PARAMS));
		return oldParams.combine(_calculatedParams, fraction1);
	}

	/**
	 * Update the labels to show the combined parameters
	 * @param inCombinedParams combined estimation parameters
	 */
	private void updateCombinedLabels(EstimationParameters inCombinedParams)
	{
		// Update the slider description label
		String sliderDesc = null;
		final int sliderVal = _weightSlider.getValue();
		switch (sliderVal)
		{
			case 0:  sliderDesc = I18nManager.getText("dialog.learnestimationparams.weight.100pccurrent"); break;
			case 5:  sliderDesc = I18nManager.getText("dialog.learnestimationparams.weight.50pc"); break;
			case 10: sliderDesc = I18nManager.getText("dialog.learnestimationparams.weight.100pccalculated"); break;
			default:
				final int currTenths = 10 - sliderVal, calcTenths = sliderVal;
				sliderDesc = "" + currTenths + "0% " + I18nManager.getText("dialog.learnestimationparams.weight.current")
					+ " + " + calcTenths + "0% " + I18nManager.getText("dialog.learnestimationparams.weight.calculated");
		}
		_sliderDescLabel.setText(sliderDesc);
		// And update all the combined params labels
		_combinedParamPanel.updateParameters(inCombinedParams);
		_combineButton.setEnabled(sliderVal > 0);
	}

	/**
	 * React to the combine button, by saving the combined parameters in the config
	 */
	private void combineAndFinish()
	{
		EstimationParameters params = calculateCombinedParameters();
		Config.setConfigString(Config.KEY_ESTIMATION_PARAMS, params.toConfigString());
		_dialog.dispose();
	}
}
