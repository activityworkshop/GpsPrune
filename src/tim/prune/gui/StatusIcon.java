package tim.prune.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Little status icon for either tick (valid) or cross (not valid)
 */
public class StatusIcon extends JLabel
{
	/** Current status */
	private Status _currStatus = Status.BLANK;

	private final ImageIcon _blankIcon;
	private final ImageIcon _validIcon;
	private final ImageIcon _invalidIcon;

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
	 * @param inIconManager icon manager to provide icons
	 */
	public StatusIcon(IconManager inIconManager)
	{
		super(inIconManager.getImageIcon(IconManager.ENTRY_NONE));
		_currStatus = Status.BLANK;
		_blankIcon = inIconManager.getImageIcon(IconManager.ENTRY_NONE);
		_validIcon = inIconManager.getImageIcon(IconManager.ENTRY_VALID);
		_invalidIcon = inIconManager.getImageIcon(IconManager.ENTRY_INVALID);
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
