package tim.prune.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Class to make it easier to use GridBagLayout for a non-equal-width layout
 * Default is two columns but can handle more
 */
public class GuiGridLayout
{
	private GridBagLayout _layout = null;
	private GridBagConstraints _constraints = null;
	private JPanel _panel = null;
	private int _numColumns = 0;
	private double[] _colWeights = null;
	private boolean[] _rightAligns = null;
	private int _x = 0;
	private int _y = 0;

	/**
	 * Constructor
	 * @param inPanel panel using layout
	 */
	public GuiGridLayout(JPanel inPanel)
	{
		// Default is two columns, with more weight to the right-hand one; first column is right-aligned
		this(inPanel, null, null);
	}

	/**
	 * Constructor
	 * @param inPanel panel using layout
	 * @param inColumnWeights array of column weights
	 * @param inAlignRights array of booleans, true for right alignment, false for left
	 */
	public GuiGridLayout(JPanel inPanel, double[] inColumnWeights, boolean[] inAlignRights)
	{
		_panel = inPanel;
		_layout = new GridBagLayout();
		_constraints = new GridBagConstraints();
		_colWeights = inColumnWeights;
		_rightAligns = inAlignRights;
		if (_colWeights == null || _rightAligns == null || _colWeights.length != _rightAligns.length
			|| _colWeights.length < 2)
		{
			_colWeights = new double[] {0.5, 1.0};
			_rightAligns = new boolean[] {true, false};
		}
		_numColumns = _colWeights.length;
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
		_constraints.weightx = _colWeights[_x];
		// set anchor
		_constraints.anchor = (_rightAligns[_x]?GridBagConstraints.LINE_END:GridBagConstraints.LINE_START);
		_layout.setConstraints(inComponent, _constraints);
		// add to panel
		_panel.add(inComponent);
		// work out next position
		_x++;
		if (_x >= _numColumns) {
			nextRow();
		}
	}

	/**
	 * Go to the next row of the grid
	 */
	public void nextRow()
	{
		_x = 0;
		_y++;
	}
}
