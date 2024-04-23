package tim.prune.function.estimate;

import java.awt.Component;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Unit;
import tim.prune.gui.DisplayUtils;

/**
 * Display panel for showing estimation parameters
 * in a standard grid form
 */
public class ParametersPanel extends JPanel
{
	private final Config _config;
	/** Flag for whether average error should be shown */
	private final boolean _showAverageError;
	/** Labels for calculated parameters */
	private JLabel _fsUnitsLabel = null, _flatSpeedLabel = null;
	private JLabel _climbUnitsLabel = null;
	private JLabel _gentleClimbLabel = null, _steepClimbLabel = null;
	private JLabel _descentUnitsLabel = null;
	private JLabel _gentleDescentLabel = null, _steepDescentLabel = null;
	private JLabel _averageErrorLabel = null;
	private final NumberFormat _numberFormatter;

	/**
	 * Constructor
	 * @param inTitleKey key to use for title of panel
	 * @param inConfig config object
	 */
	public ParametersPanel(String inTitleKey, Config inConfig) {
		this(inTitleKey, false, inConfig);
	}

	/**
	 * Constructor
	 * @param inTitleKey key to use for title of panel
	 * @param inShowAvgError true to show average error line
	 * @param inConfig config object
	 */
	public ParametersPanel(String inTitleKey, boolean inShowAvgError, Config inConfig)
	{
		super();
		_config = inConfig;
		_numberFormatter = NumberFormat.getNumberInstance();
		if (_numberFormatter instanceof DecimalFormat) {
			((DecimalFormat) _numberFormatter).applyPattern("0.00");
		}
		_showAverageError = inShowAvgError;
		if (inTitleKey != null) {
			setBorder(BorderFactory.createTitledBorder(I18nManager.getText(inTitleKey)));
		}
		setLayout(new GridLayout(0, 3, 3, 3));
		addLabels();
	}


	private void addLabels()
	{
		// flat speed
		_fsUnitsLabel = new JLabel(I18nManager.getText("dialog.estimatetime.parameters.timefor") + " 5km : ");
		_fsUnitsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		add(_fsUnitsLabel);
		_flatSpeedLabel = new JLabel("60 minutes"); // (filled in later)
		add(_flatSpeedLabel);
		add(new JLabel(""));
		// Headers for gentle and steep
		add(new JLabel(""));
		JLabel gentleLabel = new JLabel(I18nManager.getText("dialog.estimatetime.gentle"));
		add(gentleLabel);
		JLabel steepLabel = new JLabel(I18nManager.getText("dialog.estimatetime.steep"));
		add(steepLabel);
		// Climb
		_climbUnitsLabel = new JLabel("Climb 100m: ");
		_climbUnitsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		add(_climbUnitsLabel);
		_gentleClimbLabel = new JLabel("22 minutes"); // (filled in later)
		add(_gentleClimbLabel);
		_steepClimbLabel = new JLabel("22 minutes"); // (filled in later)
		add(_steepClimbLabel);
		// Descent
		_descentUnitsLabel = new JLabel(I18nManager.getText("dialog.estimatetime.parameters.timefor") + ": ");
		_descentUnitsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		add(_descentUnitsLabel);
		_gentleDescentLabel = new JLabel("22 minutes"); // (filled in later)
		add(_gentleDescentLabel);
		_steepDescentLabel = new JLabel("22 minutes"); // (filled in later)
		add(_steepDescentLabel);
		// Average error
		if (_showAverageError)
		{
			JLabel errorLabel = new JLabel(I18nManager.getText("dialog.learnestimationparams.averageerror") + ": ");
			errorLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			add(errorLabel);
			_averageErrorLabel = new JLabel("22 minutes"); // (filled in later)
			add(_averageErrorLabel);
		}
	}

	/**
	 * Update the labels using the given parameters
	 * @param inParams the parameters used or calculated
	 * @param inAverageError average error as percentage
	 * @param inShowError true to show this error value, false otherwise
	 */
	private void updateParameters(EstimationParameters inParams, double inAverageError, boolean inShowError)
	{
		if (inParams == null)
		{
			_flatSpeedLabel.setText("");
			_gentleClimbLabel.setText(""); _steepClimbLabel.setText("");
			_gentleDescentLabel.setText(""); _steepDescentLabel.setText("");
		}
		else
		{
			final Unit distUnit = _config.getUnitSet().getDistanceUnit();
			final String minsText = " " + I18nManager.getText("units.minutes");
			_fsUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.parameters.timefor") +
				" " + EstimationParameters.getStandardDistance(distUnit) + ": ");
			_flatSpeedLabel.setText(formatMinutes(inParams.getFlatMinutesLocal(distUnit)) + minsText);
			final Unit altUnit = _config.getUnitSet().getAltitudeUnit();
			final String heightString = " " + EstimationParameters.getStandardClimb(altUnit) + ": ";
			_climbUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.climb") + heightString);
			_gentleClimbLabel.setText(formatMinutes(inParams.getGentleClimbMinutesLocal(altUnit)) + minsText);
			_steepClimbLabel.setText(formatMinutes(inParams.getSteepClimbMinutesLocal(altUnit)) + minsText);
			_descentUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.descent") + heightString);
			_gentleDescentLabel.setText(formatMinutes(inParams.getGentleDescentMinutesLocal(altUnit)) + minsText);
			_steepDescentLabel.setText(formatMinutes(inParams.getSteepDescentMinutesLocal(altUnit)) + minsText);
		}
		// Average error
		if (_averageErrorLabel != null)
		{
			if (inParams == null || !inShowError) {
				_averageErrorLabel.setText("");
			}
			else {
				_averageErrorLabel.setText(DisplayUtils.formatOneDp(inAverageError) + " %");
			}
		}
	}

	/** @return the number formatted using the current locale (and 2 decimal places) */
	private String formatMinutes(double inMinutes) {
		return _numberFormatter.format(inMinutes);
	}

	/**
	 * Just show the parameters, with no average error
	 * @param inParams parameters to show
	 */
	public void updateParameters(EstimationParameters inParams) {
		updateParameters(inParams, 0.0, false);
	}

	/**
	 * Show the parameters and the average error
	 * @param inParams parameters to show
	 * @param inAverageError average error as percentage
	 */
	public void updateParameters(EstimationParameters inParams, double inAverageError) {
		updateParameters(inParams, inAverageError, true);
	}
}
