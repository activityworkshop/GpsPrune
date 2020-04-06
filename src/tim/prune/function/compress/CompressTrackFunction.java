package tim.prune.function.compress;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to provide the function for track compression
 */
public class CompressTrackFunction extends MarkAndDeleteFunction
{
	private Track _track = null;
	private JDialog _dialog = null;
	private JButton _okButton = null;
	private CompressionAlgorithm[] _algorithms = null;
	private SummaryLabel _summaryLabel = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public CompressTrackFunction(App inApp)
	{
		super(inApp);
		_track = inApp.getTrackInfo().getTrack();
		makeAlgorithms();
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.compress";
	}

	/**
	 * Show the dialog to select compression parameters
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		preview();
		_dialog.setVisible(true);
	}

	/**
	 * Preview the compression by calling each algorithm in turn
	 * @return array of delete flags
	 */
	private boolean[] preview()
	{
		int numToDelete = 0;
		boolean[] deleteFlags = new boolean[_track.getNumPoints()];
		for (int i=0; i<_algorithms.length; i++)
		{
			numToDelete += _algorithms[i].preview(deleteFlags);
		}
		_summaryLabel.setValue(numToDelete);
		_okButton.setEnabled(numToDelete > 0);
		return deleteFlags;
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		// Make a central panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// Add each of the algorithm components to the panel
		for (int i=0; i<_algorithms.length; i++)
		{
			mainPanel.add(_algorithms[i].getGuiComponents());
			mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		}
		// Summary label below algorithms
		JPanel summaryPanel = new JPanel();
		_summaryLabel = new SummaryLabel(_track);
		summaryPanel.add(_summaryLabel);
		mainPanel.add(summaryPanel);
		dialogPanel.add(mainPanel, BorderLayout.NORTH);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.setEnabled(false);
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		_okButton.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * Initialise all the algorithms to use
	 */
	private void makeAlgorithms()
	{
		// make listener to be informed of algorithm activation
		ActionListener changeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				preview();
			}
		};
		// construct track details to be used by all algorithms
		TrackDetails details = new TrackDetails(_track);
		// make array of algorithm objects
		_algorithms = new CompressionAlgorithm[] {
			new DuplicatePointAlgorithm(_track, details, changeListener),
			new ClosePointsAlgorithm(_track, details, changeListener),
			new WackyPointAlgorithm(_track, details, changeListener),
			new SingletonAlgorithm(_track, details, changeListener),
			new DouglasPeuckerAlgorithm(_track, details, changeListener)
		};
	}


	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		boolean[] deleteFlags = preview();
		// All flags are now combined in deleteFlags array
		int numMarked = 0;
		for (int i=0; i<deleteFlags.length; i++)
		{
			DataPoint point = _track.getPoint(i);
			boolean deletePoint = deleteFlags[i] && !point.hasMedia();
			point.setMarkedForDeletion(deletePoint);
			if (deletePoint) numMarked++;
		}

		// Close dialog and inform listeners
		UpdateMessageBroker.informSubscribers();
		_dialog.dispose();
		// Show confirmation dialog with OK button (not status bar message)
		if (numMarked > 0)
		{
			optionallyDeleteMarkedPoints(numMarked);
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.compress.confirmnone"),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
