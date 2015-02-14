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
	private int _maxIndex = 0;

	/**
	 * Constructor giving buttons and size
	 * @param inUpButton up button
	 * @param inDownButton down button
	 * @param inListSize size of list
	 */
	public UpDownToggler(JButton inUpButton, JButton inDownButton, int inListSize)
	{
		_upButton = inUpButton;
		_downButton = inDownButton;
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
