package tim.prune.gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BoxPanel extends JPanel
{
	private BoxPanel() {
	}

	public static BoxPanel create()
	{
		BoxPanel panel = new BoxPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		return panel;
	}

	public void add(JComponent inComponent)
	{
		inComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
		super.add(inComponent);
	}
}
