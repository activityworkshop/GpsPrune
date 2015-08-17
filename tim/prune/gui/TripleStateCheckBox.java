package tim.prune.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

/**
 * Class to represent a checkbox with three states, through which it cycles
 * Instead of calling isChecked, need to use getCurrentState which will
 * return 0, 1 or 2
 */
public class TripleStateCheckBox extends JCheckBox implements ItemListener
{
	/** Array of icons to be used */
	private ImageIcon[] _icons = new ImageIcon[3];
	/** Current state 0, 1 or 2 */
	private int _currState = 0;

	/** Inner class to proxy the listening events */
	private class ProxyListener implements ItemListener
	{
		/** Listener onto which some of the events will be passed */
		private ItemListener _listener = null;
		/** Constructor */
		ProxyListener(ItemListener inListener) {_listener = inListener;}
		/** React to events, and only pass on the selected ones */
		public void itemStateChanged(ItemEvent arg0) {
			if (arg0.getStateChange() == ItemEvent.SELECTED) {
				_listener.itemStateChanged(arg0);
			}
		}
	}

	/** Constructor */
	public TripleStateCheckBox()
	{
		addItemListener(this);
	}

	/** Set the current state */
	public void setCurrentState(int inState)
	{
		_currState = inState % 3;
		setIcon(_icons[_currState]);
		setSelected(false);
		setSelectedIcon(_icons[(_currState+1)%3]);
	}

	/** @return current state 0, 1 or 2 */
	public int getCurrentState()
	{
		return _currState;
	}

	/**
	 * Set the icon to use for the given index
	 * @param inIndex index 0, 1 or 2
	 * @param inIcon icon to use for that state
	 */
	public void setIcon(int inIndex, ImageIcon inIcon)
	{
		_icons[inIndex % 3] = inIcon;
	}

	@Override
	/** Intercept listener adding by putting a proxy inbetween */
	public void addItemListener(ItemListener inListener) {
		super.addItemListener(new ProxyListener(inListener));
	}

	/** React to a selection event by advancing the state */
	public void itemStateChanged(ItemEvent inEvent)
	{
		setCurrentState(_currState + 1);
	}
}
