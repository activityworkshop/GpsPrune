package tim.prune.save;

import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Class to enable and disable a pair of up and down buttons
 */
public class UpDownToggler implements ListSelectionListener
{
	private JButton _upButton = null;
	private JButton _downButton = null;
	private int _maxIndex = 2;

	/**
	 * Constructor giving buttons to enable/disable
	 * @param inUpButton up button
	 * @param inDownButton down button
	 */
	public UpDownToggler(JButton inUpButton, JButton inDownButton)
	{
		_upButton = inUpButton;
		_downButton = inDownButton;
	}

	/**
	 * Set the list size
	 * @param inListSize number of items in list
	 */
	public void setListSize(int inListSize)
	{
		_maxIndex = inListSize - 1;
	}

	/**
	 * list selection has changed
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty())
		{
			// no rows are selected
			_upButton.setEnabled(false);
			_downButton.setEnabled(false);
		}
		else
		{
			// single row is selected
			int row = lsm.getMinSelectionIndex();
			_upButton.setEnabled(row > 0);
			_downButton.setEnabled(row >= 0 && row < _maxIndex);
		}
	}
}
