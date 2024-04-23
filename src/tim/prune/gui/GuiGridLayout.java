package tim.prune.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class to make it easier to use GridBagLayout for a non-equal-width layout
 * Default is two columns but can handle more
 */
public class GuiGridLayout
{
	private final GridBagLayout _layout;
	private final GridBagConstraints _constraints;
	private final JPanel _panel;
	private final int _numColumns;
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
	 * @param inPadding y padding of grid cells (default 1)
	 */
	public void setYPadding(int inPadding) {
		_constraints.ipady = inPadding;
	}

	/**
	 * Add the given component to the grid
	 * @param inComponent component to add
	 */
	public void add(JComponent inComponent) {
		add(inComponent, 1, false);
	}

	/**
	 * Add the given component to the grid
	 * @param inComponent component to add
	 * @param inFillWidth true to fill width of column, default false
	 */
	public void add(JComponent inComponent, boolean inFillWidth) {
		add(inComponent, 1, inFillWidth);
	}

	/**
	 * Add the given component to the grid
	 * @param inComponent component to add
	 * @param inColspan number of columns to span (normally 1)
	 * @param inFillWidth true to fill width of column, default false
	 */
	public void add(JComponent inComponent, int inColspan, boolean inFillWidth)
	{
		_constraints.gridx = _x;
		_constraints.gridy = _y;
		_constraints.weightx = _colWeights[_x];
		_constraints.gridwidth = inColspan;
		// set anchor
		_constraints.anchor = (_rightAligns[_x]?GridBagConstraints.LINE_END:GridBagConstraints.LINE_START);
		_constraints.fill = (inFillWidth ? GridBagConstraints.HORIZONTAL: GridBagConstraints.NONE);
		_layout.setConstraints(inComponent, _constraints);
		// add to panel
		_panel.add(inComponent);
		// work out next position
		_x += inColspan;
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

	public void addVerticalGap(int inPixels)
	{
		JLabel invisibleLabel = new JLabel();
		invisibleLabel.setPreferredSize(new Dimension(10, inPixels));
		add(invisibleLabel);
		nextRow();
	}
}
