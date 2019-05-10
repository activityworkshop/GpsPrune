package tim.prune.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Little status icon for green tick (valid) or red cross (not valid)
 */
public class StatusIcon extends JLabel
{
	/** Current status */
	private Status _currStatus = Status.BLANK;

	private static ImageIcon _blankIcon = IconManager.getImageIcon(IconManager.ENTRY_NONE);
	private static ImageIcon _validIcon = IconManager.getImageIcon(IconManager.ENTRY_VALID);
	private static ImageIcon _invalidIcon = IconManager.getImageIcon(IconManager.ENTRY_INVALID);

	/**
	 * Three possible states for icon
	 */
	private enum Status {
		BLANK,
		VALID,
		INVALID
	}

	/**
	 * Constructor
	 */
	public StatusIcon()
	{
		super(_blankIcon);
		_currStatus = Status.BLANK;
	}

	/**
	 * Set the status to blank
	 */
	public void setStatusBlank()
	{
		if (_currStatus != Status.BLANK) {
			setIcon(_blankIcon);
			_currStatus = Status.BLANK;
		}
	}

	/**
	 * Set the status to valid
	 */
	public void setStatusValid()
	{
		if (_currStatus != Status.VALID) {
			setIcon(_validIcon);
			_currStatus = Status.VALID;
		}
	}

	/**
	 * Set the status to not valid
	 */
	public void setStatusInvalid()
	{
		if (_currStatus != Status.INVALID) {
			setIcon(_invalidIcon);
			_currStatus = Status.INVALID;
		}
	}
}
