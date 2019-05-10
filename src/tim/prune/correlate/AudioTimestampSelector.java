package tim.prune.correlate;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import tim.prune.I18nManager;

/**
 * GUI element to allow the selection of timestamp options
 * for audio clip correlation
 */
public class AudioTimestampSelector extends JPanel
{
	/** Array of radio buttons */
	private JRadioButton[] _radios = new JRadioButton[3];


	/**
	 * Constructor
	 * @param inTopLabelKey key for description label at top
	 * @param inLowerLabelKey key for description label at bottom, if any
	 */
	public AudioTimestampSelector(String inTopLabelKey, String inLowerLabelKey)
	{
		createComponents(inTopLabelKey, inLowerLabelKey);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
	}

	/**
	 * Create the GUI components
	 * @param inTopLabelKey key for description label at top
	 * @param inLowerLabelKey key for description label at bottom, if any
	 */
	private void createComponents(String inTopLabelKey, String inLowerLabelKey)
	{
		setLayout(new BorderLayout());
		add(new JLabel(I18nManager.getText(inTopLabelKey)), BorderLayout.NORTH);
		// panel for the radio buttons
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 3, 15, 3));
		final String[] keys = {"beginning", "middle", "end"};
		ButtonGroup group = new ButtonGroup();
		for (int i=0; i<3; i++)
		{
			_radios[i] = new JRadioButton(I18nManager.getText("dialog.correlate.timestamp." + keys[i]));
			group.add(_radios[i]);
			gridPanel.add(_radios[i]);
		}
		_radios[0].setSelected(true);
		add(gridPanel, BorderLayout.CENTER);
		if (inLowerLabelKey != null) {
			add(new JLabel(I18nManager.getText(inLowerLabelKey)), BorderLayout.SOUTH);
		}
	}

	/**
	 * Get the option selected by the user
	 * @return 0 for beginning, 1 for middle or 2 for end
	 */
	public int getSelectedOption()
	{
		for (int i=0; i<_radios.length; i++)
			if (_radios[i].isSelected()) {return i;}
		return 0;
	}
}
