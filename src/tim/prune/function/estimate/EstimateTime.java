package tim.prune.function.estimate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.RangeStatsWithGradients;
import tim.prune.data.Selection;
import tim.prune.data.Unit;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.DisplayUtils;
import tim.prune.gui.GuiGridLayout;
import tim.prune.tips.TipManager;

/**
 * Class to calculate and show the results of estimating (hike) time for the current range
 */
public class EstimateTime extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Labels for distances */
	private JLabel _distanceLabel = null;
	/** Labels for durations */
	private JLabel _estimatedDurationLabel = null, _actualDurationLabel = null;
	/** Labels for climbs */
	private JLabel _gentleClimbLabel = null, _steepClimbLabel = null;
	/** Labels for descents */
	private JLabel _gentleDescentLabel = null, _steepDescentLabel = null;
	/** Labels and text fields for parameters */
	private JLabel _flatSpeedLabel = null;
	private DecimalNumberField _flatSpeedField = null;
	private JLabel _climbParamLabel = null;
	private DecimalNumberField _gentleClimbField = null, _steepClimbField = null;
	private JLabel _descentParamLabel = null;
	private DecimalNumberField _gentleDescentField = null, _steepDescentField = null;
	/** Range stats */
	private RangeStatsWithGradients _stats = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public EstimateTime(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.estimatetime";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Get the stats on the selection before launching the dialog
		Selection selection = _app.getTrackInfo().getSelection();
		_stats = new RangeStatsWithGradients(_app.getTrackInfo().getTrack(),
			selection.getStart(), selection.getEnd());

		if (_stats.getMovingDistance() < 0.01)
		{
			_app.showErrorMessage(getNameKey(), "dialog.estimatetime.error.nodistance");
			return;
		}
		if (_dialog == null)
		{
			// First time in, check whether params are at default, show tip message if unaltered
			showTip();
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

		// main panel with a box layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Label at top
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.fullrangedetails.intro") + ":");
		introLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		introLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(introLabel);
		mainPanel.add(Box.createVerticalStrut(4));

		// Details panel in a grid
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(0, 4, 6, 2));
		detailsPanel.setBorder(BorderFactory.createTitledBorder(
			I18nManager.getText("dialog.estimatetime.details")));

		// Distance
		JLabel distLabel = new JLabel(I18nManager.getText("fieldname.distance") + ": ");
		distLabel.setHorizontalAlignment(JLabel.RIGHT);
		detailsPanel.add(distLabel);
		_distanceLabel = new JLabel("5 km");
		detailsPanel.add(_distanceLabel);
		detailsPanel.add(new JLabel("")); detailsPanel.add(new JLabel("")); // two blank cells

		detailsPanel.add(new JLabel(""));
		detailsPanel.add(new JLabel(I18nManager.getText("dialog.estimatetime.gentle")));
		detailsPanel.add(new JLabel(I18nManager.getText("dialog.estimatetime.steep")));
		detailsPanel.add(new JLabel("")); // blank cells

		// Climb
		JLabel climbLabel = new JLabel(I18nManager.getText("dialog.estimatetime.climb") + ": ");
		climbLabel.setHorizontalAlignment(JLabel.RIGHT);
		detailsPanel.add(climbLabel);
		_gentleClimbLabel = new JLabel("1500 m");
		detailsPanel.add(_gentleClimbLabel);
		_steepClimbLabel = new JLabel("1500 m");
		detailsPanel.add(_steepClimbLabel);
		detailsPanel.add(new JLabel(""));

		// Descent
		JLabel descentLabel = new JLabel(I18nManager.getText("dialog.estimatetime.descent") + ": ");
		descentLabel.setHorizontalAlignment(JLabel.RIGHT);
		detailsPanel.add(descentLabel);
		_gentleDescentLabel = new JLabel("1500 m");
		detailsPanel.add(_gentleDescentLabel);
		_steepDescentLabel = new JLabel("1500 m");
		detailsPanel.add(_steepDescentLabel);
		detailsPanel.add(new JLabel(""));

		detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(detailsPanel);
		mainPanel.add(Box.createVerticalStrut(4));

		// Parameters panel in a flexible grid
		JPanel paramsPanel = new JPanel();
		GuiGridLayout paramsGrid = new GuiGridLayout(paramsPanel, new double[] {1.5, 0.2, 1.0, 0.2, 0.5},
			new boolean[] {true, false, false, false, false});
		paramsPanel.setBorder(BorderFactory.createTitledBorder(
			I18nManager.getText("dialog.estimatetime.parameters")));
		KeyAdapter paramChangeListener = new KeyAdapter() {
			public void keyTyped(KeyEvent inE) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						calculateEstimatedTime();
					}
				});
			}
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		};

		// Flat speed
		_flatSpeedLabel = new JLabel(I18nManager.getText("dialog.estimatetime.parameters.timefor") + ": "); // (filled in later)
		_flatSpeedLabel.setHorizontalAlignment(JLabel.RIGHT);
		paramsGrid.add(_flatSpeedLabel);
		_flatSpeedField = new DecimalNumberField();
		_flatSpeedField.addKeyListener(paramChangeListener);
		paramsGrid.add(_flatSpeedField);
		JLabel minsLabel = new JLabel(I18nManager.getText("units.minutes"));
		paramsGrid.add(minsLabel);
		paramsGrid.nextRow();
		// Headers for gentle and steep
		paramsGrid.add(new JLabel(""));
		paramsGrid.add(new JLabel(I18nManager.getText("dialog.estimatetime.gentle")));
		paramsGrid.add(new JLabel("")); // blank cell
		paramsGrid.add(new JLabel(I18nManager.getText("dialog.estimatetime.steep")));
		paramsGrid.nextRow();
		// Gentle climb
		_climbParamLabel = new JLabel(I18nManager.getText("dialog.estimatetime.parameters.timefor") + ": "); // (filled in later)
		_climbParamLabel.setHorizontalAlignment(JLabel.RIGHT);
		paramsGrid.add(_climbParamLabel);
		_gentleClimbField = new DecimalNumberField();
		_gentleClimbField.addKeyListener(paramChangeListener);
		paramsGrid.add(_gentleClimbField);
		paramsGrid.add(new JLabel(minsLabel.getText()));
		// Steep climb
		_steepClimbField = new DecimalNumberField();
		_steepClimbField.addKeyListener(paramChangeListener);
		paramsGrid.add(_steepClimbField);
		paramsGrid.add(new JLabel(minsLabel.getText()));

		// Gentle descent
		_descentParamLabel = new JLabel(I18nManager.getText("dialog.estimatetime.parameters.timefor") + ": "); // (filled in later)
		_descentParamLabel.setHorizontalAlignment(JLabel.RIGHT);
		paramsGrid.add(_descentParamLabel);
		_gentleDescentField = new DecimalNumberField(true); // negative numbers allowed
		_gentleDescentField.addKeyListener(paramChangeListener);
		paramsGrid.add(_gentleDescentField);
		paramsGrid.add(new JLabel(minsLabel.getText()));
		// Steep climb
		_steepDescentField = new DecimalNumberField(true); // negative numbers allowed
		_steepDescentField.addKeyListener(paramChangeListener);
		paramsGrid.add(_steepDescentField);
		paramsGrid.add(new JLabel(minsLabel.getText()));

		paramsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(paramsPanel);
		mainPanel.add(Box.createVerticalStrut(12));

		// Results panel
		JPanel resultsPanel = new JPanel();
		resultsPanel.setBorder(BorderFactory.createTitledBorder(
			I18nManager.getText("dialog.estimatetime.results")));
		resultsPanel.setLayout(new GridLayout(0, 2, 3, 3));
		// estimated time
		_estimatedDurationLabel = new JLabel(I18nManager.getText("dialog.estimatetime.results.estimatedtime") + ": "); // filled in later
		Font origFont = _estimatedDurationLabel.getFont();
		_estimatedDurationLabel.setFont(origFont.deriveFont(Font.BOLD, origFont.getSize2D() + 2.0f));

		resultsPanel.add(_estimatedDurationLabel);
		// actual time (if available)
		_actualDurationLabel = new JLabel(I18nManager.getText("dialog.estimatetime.results.actualtime") + ": "); // filled in later
		resultsPanel.add(_actualDurationLabel);

		resultsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(resultsPanel);
		mainPanel.add(Box.createVerticalStrut(4));

		dialogPanel.add(mainPanel, BorderLayout.NORTH);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finishDialog();
			}
		});
		closeButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		});
		buttonPanel.add(closeButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Recalculate the values and update the labels
	 */
	private void updateDetails()
	{
		// Warn if the current track hasn't got any height information
		if (!_stats.getMovingAltitudeRange().hasRange()) {
			_app.showErrorMessage(getNameKey(), "dialog.estimatetime.error.noaltitudes");
		}

		// Distance in current units
		final Unit distUnit = Config.getUnitSet().getDistanceUnit();
		final String distUnitsStr = I18nManager.getText(distUnit.getShortnameKey());
		final double movingDist = _stats.getMovingDistance();
		_distanceLabel.setText(DisplayUtils.roundedNumber(movingDist) + " " + distUnitsStr);

		// Climb and descent values
		final Unit altUnit = Config.getUnitSet().getAltitudeUnit();
		final String altUnitsStr = " " + I18nManager.getText(altUnit.getShortnameKey());
		_gentleClimbLabel.setText(_stats.getGentleAltitudeRange().getClimb(altUnit) + altUnitsStr);
		_steepClimbLabel.setText(_stats.getSteepAltitudeRange().getClimb(altUnit) + altUnitsStr);
		_gentleDescentLabel.setText(_stats.getGentleAltitudeRange().getDescent(altUnit) + altUnitsStr);
		_steepDescentLabel.setText(_stats.getSteepAltitudeRange().getDescent(altUnit) + altUnitsStr);

		// Try to get parameters from config
		EstimationParameters estParams = new EstimationParameters(Config.getConfigString(Config.KEY_ESTIMATION_PARAMS));

		String[] paramValues = estParams.getStrings();
		if (paramValues == null || paramValues.length != 5)
		{
			// TODO: What to do in case of error?
			return;
		}
		// Flat time is either for 5 km, 3 miles or 3 nm
		_flatSpeedLabel.setText(I18nManager.getText("dialog.estimatetime.parameters.timefor") +
			" " + EstimationParameters.getStandardDistance() + ": ");
		_flatSpeedField.setText(paramValues[0]);

		final String heightString = " " + EstimationParameters.getStandardClimb() + ": ";
		_climbParamLabel.setText(I18nManager.getText("dialog.estimatetime.climb") + heightString);
		_gentleClimbField.setText(paramValues[1]);
		_steepClimbField.setText(paramValues[2]);
		_descentParamLabel.setText(I18nManager.getText("dialog.estimatetime.descent") + heightString);
		_gentleDescentField.setText(paramValues[3]);
		_steepDescentField.setText(paramValues[4]);

		// Use the entered parameters to estimate the time
		calculateEstimatedTime();

		// Get the actual time if available, for comparison
		if (_stats.getMovingDurationInSeconds() > 0)
		{
			_actualDurationLabel.setText(I18nManager.getText("dialog.estimatetime.results.actualtime") + ": "
				+ DisplayUtils.buildDurationString(_stats.getMovingDurationInSeconds()));
		}
		else {
			_actualDurationLabel.setText("");
		}
	}


	/**
	 * Use the current parameter and the range stats to calculate the estimated time
	 * and populate the answer in the dialog
	 */
	private void calculateEstimatedTime()
	{
		// Populate an EstimationParameters object from the four strings
		EstimationParameters params = new EstimationParameters();
		params.populateWithStrings(_flatSpeedField.getText(), _gentleClimbField.getText(),
			_steepClimbField.getText(), _gentleDescentField.getText(), _steepDescentField.getText());
		if (!params.isValid()) {
			_estimatedDurationLabel.setText("- - -");
		}
		else
		{
			final long numSeconds = (long) (params.applyToStats(_stats) * 60.0);
			_estimatedDurationLabel.setText(I18nManager.getText("dialog.estimatetime.results.estimatedtime") + ": "
				+ DisplayUtils.buildDurationString(numSeconds));
		}
	}


	/**
	 * Finish with the dialog, by pressing the "Close" button
	 */
	private void finishDialog()
	{
		// Make estimation parameters from entered strings, if valid save to config
		EstimationParameters params = new EstimationParameters();
		params.populateWithStrings(_flatSpeedField.getText(), _gentleClimbField.getText(),
			_steepClimbField.getText(), _gentleDescentField.getText(), _steepDescentField.getText());
		if (params.isValid()) {
			Config.setConfigString(Config.KEY_ESTIMATION_PARAMS, params.toConfigString());
		}
		_dialog.dispose();
	}

	/**
	 * Show a tip to use the learn function, if appropriate
	 */
	private void showTip()
	{
		EstimationParameters currParams = new EstimationParameters(
			Config.getConfigString(Config.KEY_ESTIMATION_PARAMS));
		if (currParams.sameAsDefaults()) {
			_app.showTip(TipManager.Tip_LearnTimeParams);
		}
	}
}
