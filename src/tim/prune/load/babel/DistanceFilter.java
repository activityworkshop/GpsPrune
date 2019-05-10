package tim.prune.load.babel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.WholeNumberField;

/**
 * Distance filter for GPSBabel (compress by distance, or nearby points)
 */
public class DistanceFilter extends FilterDefinition
{
	/** Constructor */
	public DistanceFilter(AddFilterDialog inFilterDialog)
	{
		super(inFilterDialog);
		makePanelContents();
	}

	private DecimalNumberField _distField = null;
	private JComboBox<String> _distUnitsCombo = null;
	private WholeNumberField _secondsField = null;


	/** @return filter name */
	protected String getFilterName() {
		return "position";
	}

	/** Make the panel contents */
	protected void makePanelContents()
	{
		setLayout(new BorderLayout());
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		add(boxPanel, BorderLayout.NORTH);
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.gpsbabel.filter.distance.intro"));
		topLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(topLabel);
		boxPanel.add(Box.createVerticalStrut(18)); // spacer
		// Main three-column grid
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 3, 4, 4));
		gridPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.distance.distance")));
		_distField = new DecimalNumberField();
		_distField.addKeyListener(_paramChangeListener);
		gridPanel.add(_distField);
		_distUnitsCombo = new JComboBox<String>(new String[] {I18nManager.getText("units.metres"), I18nManager.getText("units.feet")});
		gridPanel.add(_distUnitsCombo);
		gridPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.distance.time")));
		_secondsField = new WholeNumberField(4);
		_secondsField.addKeyListener(_paramChangeListener);
		gridPanel.add(_secondsField);
		gridPanel.add(new JLabel(I18nManager.getText("units.seconds")));
		gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(gridPanel);
	}

	/**
	 * @return true if the filters are valid
	 */
	public boolean isFilterValid()
	{
		if (_distField.getText() == null || _distField.getText().trim().equals("")) {
			return false; // no distance given
		}
		final boolean timeGiven = _secondsField.getText() != null && _secondsField.getText().trim().length() > 0;
		if (timeGiven && _secondsField.getValue() <= 1) {
			return false; // must have a decent number of seconds
		}
		// check the distance
		return (_distField.getValue() > 0.001); // no zero or negative distances allowed
	}

	/**
	 * @return filter parameters as a string, or null
	 */
	protected String getParameters()
	{
		if (!isFilterValid()) return null;
		StringBuilder builder = new StringBuilder();
		// Get the distance
		double dValue = _distField.getValue();
		builder.append(",distance=").append(dValue);
		// units of distance (miles by default)
		builder.append(_distUnitsCombo.getSelectedIndex() == 0 ? "m" : "f"); // metres or feet

		// is there a time as well?
		final boolean timeGiven = _secondsField.getText() != null && _secondsField.getText().trim().length() > 0;
		if (timeGiven) {
			builder.append(",time=").append(_secondsField.getValue()); // no s at the end
		}
		return builder.toString();
	}
}
