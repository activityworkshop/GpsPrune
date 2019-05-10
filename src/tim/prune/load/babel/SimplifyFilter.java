package tim.prune.load.babel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tim.prune.I18nManager;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.WholeNumberField;

/**
 * Simplify filter for GPSBabel
 */
public class SimplifyFilter extends FilterDefinition
{
	/** Constructor */
	public SimplifyFilter(AddFilterDialog inFilterDialog)
	{
		super(inFilterDialog);
		makePanelContents();
	}

	private WholeNumberField _maxPointsField = null;
	private DecimalNumberField _distField = null;
	private JComboBox<String> _distUnitsCombo = null;
	private JRadioButton _crossTrackRadio = null;
	private JRadioButton _lengthRadio = null;
	private JRadioButton _relativeRadio = null;


	/** @return filter name */
	protected String getFilterName() {
		return "simplify";
	}

	/** Make the panel contents */
	protected void makePanelContents()
	{
		setLayout(new BorderLayout());
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		add(boxPanel, BorderLayout.NORTH);
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.gpsbabel.filter.simplify.intro"));
		topLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(topLabel);
		boxPanel.add(Box.createVerticalStrut(18)); // spacer
		// Main three-column grid
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 3, 4, 4));
		gridPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.simplify.maxpoints")));
		_maxPointsField = new WholeNumberField(6);
		_maxPointsField.addKeyListener(_paramChangeListener);
		gridPanel.add(_maxPointsField);
		gridPanel.add(new JLabel(" "));
		gridPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.simplify.maxerror")));
		_distField = new DecimalNumberField();
		_distField.addKeyListener(_paramChangeListener);
		gridPanel.add(_distField);
		_distUnitsCombo = new JComboBox<String>(new String[] {
			I18nManager.getText(UnitSetLibrary.UNITS_KILOMETRES.getNameKey()),
			I18nManager.getText(UnitSetLibrary.UNITS_MILES.getNameKey())
		});
		gridPanel.add(_distUnitsCombo);
		// radio buttons
		_crossTrackRadio = new JRadioButton(I18nManager.getText("dialog.gpsbabel.filter.simplify.crosstrack"));
		_crossTrackRadio.setSelected(true);
		_lengthRadio     = new JRadioButton(I18nManager.getText("dialog.gpsbabel.filter.simplify.length"));
		_relativeRadio   = new JRadioButton(I18nManager.getText("dialog.gpsbabel.filter.simplify.relative"));
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(_crossTrackRadio);
		radioGroup.add(_lengthRadio);
		radioGroup.add(_relativeRadio);
		gridPanel.add(_crossTrackRadio);
		gridPanel.add(_lengthRadio);
		gridPanel.add(_relativeRadio);
		gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(gridPanel);
	}

	/**
	 * @return true if the filters are valid
	 */
	public boolean isFilterValid()
	{
		final boolean countGiven = _maxPointsField.getText() != null && _maxPointsField.getText().trim().length() > 0;
		final boolean distGiven = _distField.getText() != null && _distField.getText().trim().length() > 0;
		if ((!countGiven && !distGiven) || (countGiven && distGiven)) {
			return false; // only one or the other allowed
		}
		if (countGiven && _maxPointsField.getValue() <= 1) {
			return false; // must have a decent max points
		}
		if (distGiven && _distField.getValue() <= 0.001) {
			return false; // no zero or negative distances allowed
		}
		// must be ok
		return true;
	}

	/**
	 * @return filter parameters as a string, or null
	 */
	protected String getParameters()
	{
		if (!isFilterValid()) return null;
		StringBuilder builder = new StringBuilder();
		// type
		final boolean countGiven = _maxPointsField.getText() != null && _maxPointsField.getText().trim().length() > 0;
		final boolean distGiven = _distField.getText() != null && _distField.getText().trim().length() > 0;
		if (countGiven) {
			builder.append(",count=").append(_maxPointsField.getValue());
		}
		else if (distGiven)
		{
			double dValue = 1.0;
			try {
				dValue = Double.parseDouble(_distField.getText());
			}
			catch (Exception e) {} // shouldn't happen, otherwise validation would have failed
			builder.append(",error=").append(dValue);
			// units of distance (miles by default)
			if (_distUnitsCombo.getSelectedIndex() == 0) {
				builder.append("k"); // nothing for miles
			}
		}
		// three options
		if (_crossTrackRadio.isSelected()) {
			builder.append(",crosstrack"); // default, could not pass it
		}
		else if (_lengthRadio.isSelected()) {
			builder.append(",length");
		}
		else if (_relativeRadio.isSelected()) {
			builder.append(",relative");
		}
		return builder.toString();
	}
}
