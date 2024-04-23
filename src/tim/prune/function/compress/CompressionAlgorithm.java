package tim.prune.function.compress;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Abstract class to act as an algorithm for track compression
 */
public abstract class CompressionAlgorithm
{
	protected SummaryLabel _summaryLabel = null;
	protected final Track _track;
	protected final TrackDetails _trackDetails;
	private final ActionListener _listener;
	protected JCheckBox _activateCheckBox = null;


	/**
	 * Constructor giving Track
	 * @param inTrack track object to use for compression
	 * @param inDetails track details object
	 * @param inListener listener to be informed of activation clicks
	 */
	public CompressionAlgorithm(Track inTrack, TrackDetails inDetails,
		ActionListener inListener)
	{
		_track = inTrack;
		_trackDetails = inDetails;
		_listener = inListener;
	}


	/**
	 * @return true if this algorithm has been activated
	 */
	public boolean isActivated()
	{
		return _activateCheckBox != null
			&& _activateCheckBox.isSelected();
	}


	/**
	 * @return JPanel containing gui components
	 */
	public JPanel getGuiComponents()
	{
		_activateCheckBox = new JCheckBox(I18nManager.getText(getTitleTextKey()));
		_activateCheckBox.setSelected(false);
		_activateCheckBox.addActionListener(_listener);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(""));
		panel.setLayout(new BorderLayout());
		panel.add(_activateCheckBox, BorderLayout.NORTH);
		Component specifics = getSpecificGuiComponents();
		if (specifics != null) {
			panel.add(specifics, BorderLayout.CENTER);
		}
		// Add label at bottom
		_summaryLabel = new SummaryLabel(_track);
		panel.add(_summaryLabel, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Preview the algorithm by counting the number of points deleted
	 * @param inFlags array of deletion flags from previous algorithms
	 * @return number of points to be deleted by this algorithm
	 */
	public int preview(boolean[] inFlags)
	{
		int numDeleted = 0;
		if (isActivated())
		{
			// Run the compression and set the deletion flags
			_trackDetails.initialise();
			numDeleted = compress(inFlags);
			_summaryLabel.setValue(numDeleted);
		}
		else {
			_summaryLabel.clearValue();
		}
		return numDeleted;
	}


	/**
	 * @return key to use for title text of algorithm
	 */
	protected abstract String getTitleTextKey();

	/**
	 * @return gui components controlling algorithm (if any)
	 */
	protected abstract Component getSpecificGuiComponents();

	/**
	 * Perform the compression and set the results in the given array
	 * @param inFlags deletion flags from previous algorithms
	 * @return number of points deleted by this algorithm
	 */
	protected abstract int compress(boolean[] inFlags);

	/**
	 * @return String to save in Settings
	 */
	public String getSettingsString() {
		return _activateCheckBox.isSelected() ? "1" : "";
	}

	/**
	 * @param inSettings settings string from Config
	 */
	public void applySettingsString(String inSettings) {
		_activateCheckBox.setSelected(inSettings != null && !inSettings.isEmpty());
	}
}
