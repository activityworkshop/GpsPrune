package tim.prune.function.estimate;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.gui.DisplayUtils;

/**
 * Display panel for showing estimation parameters
 * in a standard grid form
 */
public class ParametersPanel extends JPanel
{
	/** Flag for whether average error should be shown */
	private boolean _showAverageError = false;
	/** Labels for calculated parameters */
	private JLabel _fsUnitsLabel = null, _flatSpeedLabel = null;
	private JLabel _climbUnitsLabel = null;
	private JLabel _gentleClimbLabel = null, _steepClimbLabel = null;
	private JLabel _descentUnitsLabel = null;
	private JLabel _gentleDescentLabel = null, _steepDescentLabel = null;
	private JLabel _averageErrorLabel = null;


	/**
	 * Constructor
	 * @param inTitleKey key to use for title of panel
	 */
	public ParametersPanel(String inTitleKey)
	{
		this(inTitleKey, false);
	}

	/**
	 * Constructor
	 * @param inTitleKey key to use for title of panel
	 * @param inShowAvgError true to show average error line
	 */
	public ParametersPanel(String inTitleKey, boolean inShowAvgError)
	{
		super();
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
		if (inParams == null || !inParams.isValid())
		{
			_flatSpeedLabel.setText("");
			_gentleClimbLabel.setText(""); _steepClimbLabel.setText("");
			_gentleDescentLabel.setText(""); _steepDescentLabel.setText("");
		}
		else
		{
			final String minsText = " " + I18nManager.getText("units.minutes");
			String[] values = inParams.getStrings(); // these strings are already formatted locally
			_fsUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.parameters.timefor") +
				" " + EstimationParameters.getStandardDistance() + ": ");
			_flatSpeedLabel.setText(values[0] + minsText);
			final String heightString = " " + EstimationParameters.getStandardClimb() + ": ";
			_climbUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.climb") + heightString);
			_gentleClimbLabel.setText(values[1] + minsText);
			_steepClimbLabel.setText(values[2] + minsText);
			_descentUnitsLabel.setText(I18nManager.getText("dialog.estimatetime.descent") + heightString);
			_gentleDescentLabel.setText(values[3] + minsText);
			_steepDescentLabel.setText(values[4] + minsText);
		}
		// Average error
		if (_averageErrorLabel != null)
		{
			if (inParams == null || !inParams.isValid() || !inShowError)
			{
				_averageErrorLabel.setText("");
			}
			else
			{
				_averageErrorLabel.setText(DisplayUtils.formatOneDp(inAverageError) + " %");
			}
		}
	}

	/**
	 * Just show the parameters, with no average error
	 * @param inParams parameters to show
	 */
	public void updateParameters(EstimationParameters inParams)
	{
		updateParameters(inParams, 0.0, false);
	}

	/**
	 * Show the parameters and the average error
	 * @param inParams parameters to show
	 * @param inAverageError average error as percentage
	 */
	public void updateParameters(EstimationParameters inParams, double inAverageError)
	{
		updateParameters(inParams, inAverageError, true);
	}
}
