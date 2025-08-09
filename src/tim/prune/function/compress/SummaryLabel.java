package tim.prune.function.compress;

import javax.swing.JLabel;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Summary label for compression
 */
public class SummaryLabel extends JLabel
{
	/** Track object */
	private final Track _track;


	/**
	 * Constructor
	 * @param inTrack track object to work out percentages
	 */
	public SummaryLabel(Track inTrack)
	{
		super("...........................");
		setEnabled(false);
		_track = inTrack;
	}

	/**
	 * Set value
	 * @param inNumToDelete number of points to delete
	 */
	public void setValue(int inNumToDelete)
	{
		setText(makeString(inNumToDelete));
		setEnabled(inNumToDelete > 0);
	}

	/**
	 * Clear value
	 */
	public void clearValue() {
		setValue(0);
	}

	/**
	 * Make a string describing the deletion
	 * @param inNumToDelete number of points to delete
	 * @return String describing number and %age
	 */
	private String makeString(int inNumToDelete)
	{
		String desc = I18nManager.getText("dialog.compress.summarylabel") + ": " + inNumToDelete;
		if (inNumToDelete > 0)
		{
			long percent = Math.round(100.0 * inNumToDelete / _track.getNumPoints());
			desc += " (" + percent + "%)";
		}
		return desc;
	}
}
