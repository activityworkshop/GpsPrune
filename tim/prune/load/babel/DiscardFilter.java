package tim.prune.load.babel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import tim.prune.I18nManager;
import tim.prune.gui.WholeNumberField;

/**
 * Discard filter for GPSBabel
 */
public class DiscardFilter extends FilterDefinition
{
	/** Constructor */
	public DiscardFilter(AddFilterDialog inFilterDialog)
	{
		super(inFilterDialog);
		makePanelContents();
	}

	private WholeNumberField _hdopField = null;
	private WholeNumberField _vdopField = null;
	private JComboBox<String> _combineDopsCombo = null;
	private WholeNumberField _numSatsField = null;
	private JCheckBox _noFixCheckbox = null;
	private JCheckBox _unknownFixCheckbox = null;


	/** @return filter name */
	protected String getFilterName() {
		return "discard";
	}

	/** Make the panel contents */
	protected void makePanelContents()
	{
		setLayout(new BorderLayout());
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.gpsbabel.filter.discard.intro"));
		topLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(topLabel);
		boxPanel.add(Box.createVerticalStrut(9)); // spacer

		JPanel boxPanel2 = new JPanel();
		boxPanel2.setLayout(new BoxLayout(boxPanel2, BoxLayout.Y_AXIS));
		// Panel for dops
		JPanel dopPanel = new JPanel();
		dopPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		dopPanel.setLayout(new GridLayout(0, 3, 4, 2));
		dopPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.discard.hdop"), SwingConstants.RIGHT));
		_hdopField = new WholeNumberField(2);
		_hdopField.addKeyListener(_paramChangeListener);
		dopPanel.add(_hdopField);
		_combineDopsCombo = new JComboBox<String>(new String[] {I18nManager.getText("logic.and"), I18nManager.getText("logic.or")});
		dopPanel.add(_combineDopsCombo);
		dopPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.discard.vdop"), SwingConstants.RIGHT));
		_vdopField = new WholeNumberField(2);
		_vdopField.addKeyListener(_paramChangeListener);
		dopPanel.add(_vdopField);
		boxPanel2.add(dopPanel);

		// Number of satellites
		JPanel satPanel = new JPanel();
		satPanel.add(new JLabel(I18nManager.getText("dialog.gpsbabel.filter.discard.numsats")));
		_numSatsField = new WholeNumberField(2);
		_numSatsField.addKeyListener(_paramChangeListener);
		satPanel.add(_numSatsField);
		boxPanel2.add(satPanel);

		// Checkboxes for no fix and unknown fix
		_noFixCheckbox = new JCheckBox(I18nManager.getText("dialog.gpsbabel.filter.discard.nofix"));
		boxPanel2.add(_noFixCheckbox);
		_unknownFixCheckbox = new JCheckBox(I18nManager.getText("dialog.gpsbabel.filter.discard.unknownfix"));
		boxPanel2.add(_unknownFixCheckbox);
		boxPanel2.add(Box.createVerticalStrut(9)); // spacer

		boxPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		boxPanel.add(boxPanel2);
		add(boxPanel, BorderLayout.NORTH);
}

	/**
	 * @return true if the filters are valid
	 */
	public boolean isFilterValid()
	{
		// If values are entered, insist that they're positive (0 not valid)
		if (_hdopField.getText() != null && _hdopField.getText().length() > 0 && _hdopField.getValue() <= 0) {return false;}
		if (_vdopField.getText() != null && _vdopField.getText().length() > 0 && _vdopField.getValue() <= 0) {return false;}
		if (_numSatsField.getText() != null && _numSatsField.getText().length() > 0 && _numSatsField.getValue() <= 0) {return false;}
		// Insist that at least one value has been entered
		return _hdopField.getValue() > 0 || _vdopField.getValue() > 0 || _numSatsField.getValue() > 0;
	}

	/**
	 * @return filter parameters as a string, or null
	 */
	protected String getParameters()
	{
		if (!isFilterValid()) return null;
		StringBuilder builder = new StringBuilder();
		// hdop and vdop
		final int hdop = _hdopField.getValue();
		if (hdop > 0) {
			builder.append(",hdop=").append(hdop);
		}
		final int vdop = _vdopField.getValue();
		if (vdop > 0)
		{
			builder.append(",vdop=").append(vdop);
			if (hdop > 0 && _combineDopsCombo.getSelectedIndex() == 0) {
				builder.append(",hdopandvdop");
			}
		}
		// number of satellites
		final int numSats = _numSatsField.getValue();
		if (numSats > 0)
		{
			builder.append(",sat=").append(numSats);
		}
		// checkboxes
		if (_noFixCheckbox.isSelected()) {
			builder.append(",fixnone");
		}
		if (_unknownFixCheckbox.isSelected()) {
			builder.append(",fixunknown");
		}
		return builder.toString();
	}
}
