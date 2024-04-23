package tim.prune.gui;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/** Simple button group for radio buttons */
public class RadioButtonGroup extends ButtonGroup
{
	public RadioButtonGroup(JRadioButton ... buttons)
	{
		for (JRadioButton button : buttons)
		{
			if (button != null) {
				add(button);
			}
		}
	}
}
