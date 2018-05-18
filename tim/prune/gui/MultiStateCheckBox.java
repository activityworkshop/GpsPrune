package tim.prune.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

/**
 * Class to represent a checkbox with multiple states, through which it cycles.
 * Instead of calling isChecked, callers need to use getCurrentState which will
 * return 0 up to (n-1) for n states.
 */
public class MultiStateCheckBox extends JCheckBox implements ItemListener
{
	/** Array of icons to be used */
	private ImageIcon[] _icons = null;
	/** Current state 0 to n-1 */
	private int _currState = 0;
	/** Number of states n */
	private final int _numStates;


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

	/**
	 * Constructor
	 * @param inNumStates number of states to cycle through
	 */
	public MultiStateCheckBox(int inNumStates)
	{
		_numStates = (inNumStates > 0) ? inNumStates : 1;
		_icons = new ImageIcon[_numStates];
		addItemListener(this);
	}

	/**
	 * @param inState state to set
	 */
	public void setCurrentState(int inState)
	{
		_currState = inState % _numStates;
		setIcon(_icons[_currState]);
		setSelected(false);
		setSelectedIcon(_icons[(_currState+1) % _numStates]);
	}

	/**
	 * @return current state 0 to n-1
	 */
	public int getCurrentState()
	{
		return _currState;
	}

	/**
	 * Set the icon to use for the given index
	 * @param inIndex index 0 to n-1
	 * @param inIcon icon to use for that state
	 */
	public void setIcon(int inIndex, ImageIcon inIcon)
	{
		_icons[inIndex % _numStates] = inIcon;
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
