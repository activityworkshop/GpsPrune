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
	private Track _track = null;


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
		setEnabled(true);
	}

	/**
	 * Clear value
	 */
	public void clearValue()
	{
		setText(makeString(0));
		setEnabled(false);
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
