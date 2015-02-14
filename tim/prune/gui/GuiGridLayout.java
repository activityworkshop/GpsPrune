package tim.prune.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Class to make it easier to use GridBagLayout
 * for a two-column, non-equal-width layout
 */
public class GuiGridLayout
{
	private GridBagLayout _layout = null;
	private GridBagConstraints _constraints = null;
	private JPanel _panel = null;
	private boolean _allLeft = false;
	private int _x = 0;
	private int _y = 0;

	/**
	 * Constructor
	 * @param inPanel panel using layout
	 */
	public GuiGridLayout(JPanel inPanel)
	{
		this(inPanel, false);
	}

	/**
	 * Constructor
	 * @param inPanel panel using layout
	 * @param inAllLeft true to align all elements to left
	 */
	public GuiGridLayout(JPanel inPanel, boolean inAllLeft)
	{
		_panel = inPanel;
		_allLeft = inAllLeft;
		_layout = new GridBagLayout();
		_constraints = new GridBagConstraints();
		_constraints.weightx = 1.0;
		_constraints.weighty = 0.0;
		_constraints.ipadx = 10;
		_constraints.ipady = 1;
		_constraints.insets = new Insets(1, 5, 1, 5);
		// Apply layout to panel
		_panel.setLayout(_layout);
	}

	/**
	 * Add the given component to the grid
	 * @param inComponent component to add
	 */
	public void add(JComponent inComponent)
	{
		_constraints.gridx = _x;
		_constraints.gridy = _y;
		_constraints.weightx = (_x==0?0.5:1.0);
		// set anchor
		_constraints.anchor = ((_x == 0 && !_allLeft)?GridBagConstraints.LINE_END:GridBagConstraints.LINE_START);
		_layout.setConstraints(inComponent, _constraints);
		// add to panel
		_panel.add(inComponent);
		// work out next position
		_x++;
		if (_x > 1) {
			_x = 0;
			_y++;
		}
	}
}
