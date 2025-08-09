package tim.prune.function.compress;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.methods.*;

import javax.swing.*;
import java.awt.*;

/**
 * Class to provide the function for track compression
 */
public class CompressTrackFunction extends MarkAndDeleteFunction implements CompressionDialog
{
	private final Track _track;
	private JDialog _dialog = null;
	private JButton _okButton = null;
	private PanelController _panelController = null;
	private SummaryLabel _summaryLabel = null;
	private JScrollPane _scrollPane = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public CompressTrackFunction(App inApp)
	{
		super(inApp);
		_track = inApp.getTrackInfo().getTrack();
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
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		initDialogFromConfig();
		_scrollPane.getViewport().scrollRectToVisible(new Rectangle(0, 0, 1, 1));
		_dialog.setVisible(true);
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
		mainPanel.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(new JLabel(I18nManager.getText("dialog.compress.desc")));
		topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		JButton addFilterButton = new JButton(I18nManager.getText(I18nManager.getText("button.addnew")));
		addFilterButton.addActionListener(e -> _panelController.addMethod(getConfig()));
		topPanel.add(addFilterButton);
		topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		mainPanel.add(topPanel, BorderLayout.NORTH);

		// Add each of the algorithm components to the panel
		JPanel algosPanel = new JPanel();
		_panelController = new PanelController(_track, this, algosPanel);
		algosPanel.setLayout(new BoxLayout(algosPanel, BoxLayout.Y_AXIS));
		_panelController.addDummyPanels();
		JPanel verticalHolder = new JPanel();
		verticalHolder.setLayout(new BorderLayout());
		verticalHolder.add(algosPanel, BorderLayout.NORTH);
		_scrollPane = new JScrollPane(verticalHolder);
		mainPanel.add(_scrollPane, BorderLayout.CENTER);

		// Summary label below algorithms
		JPanel summaryPanel = new JPanel();
		_summaryLabel = new SummaryLabel(_track);
		summaryPanel.add(_summaryLabel);
		mainPanel.add(summaryPanel, BorderLayout.SOUTH);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.setEnabled(false);
		_okButton.addActionListener((e) -> finish());
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener((e) -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		MarkingData markings = _panelController.recalculateAll();
		_app.getTrackInfo().clearAllMarkers();
		// All flags are now combined in the markings object
		int numMarked = 0;
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			boolean deletePoint = markings.isPointMarkedForDeletion(i) && !point.hasMedia();
			boolean setSegmentFlag = deletePoint && markings.isPointMarkedForSegmentBreak(i);
			_app.getTrackInfo().markPointForDeletion(i, deletePoint, setSegmentFlag);
			if (deletePoint) {
				numMarked++;
			}
		}
		// Save settings
		final String methodString = _panelController.getMethodList().toConfigString();
		getConfig().setConfigString(Config.KEY_COMPRESSION_METHODS, methodString);
		getConfig().setConfigString(Config.KEY_COMPRESSION_SETTINGS, "");

		// Close dialog and inform listeners
		UpdateMessageBroker.informSubscribers();
		_dialog.dispose();
		// Show confirmation dialog with OK button (not status bar message)
		if (numMarked > 0) {
			optionallyDeleteMarkedPoints(numMarked);
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.compress.confirmnone"),
				getName(), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Fill in the dialog according to the saved settings
	 */
	private void initDialogFromConfig()
	{
		// Delete all panels from controller and from scrollpanel
		_panelController.deleteAllPanels();
		Config config = getConfig();
		ParameterValues values = new ParameterValues();
		// Firstly try the new method
		String newConfigString = config.getConfigString(Config.KEY_COMPRESSION_METHODS);
		values.applyNewStyleConfig(newConfigString);
		MethodList methods = MethodList.fromConfigString(newConfigString);
		if (methods.isEmpty())
		{
			// Nothing found with the new method spec, so try the older, fixed method instead
			String oldConfigString = config.getConfigString(Config.KEY_COMPRESSION_SETTINGS);
			values.applyOldStyleConfig(oldConfigString);
			methods.add(0, new DuplicatesMethod());
			methods.add(1, new NearbyFactorMethod(values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR)));
			methods.add(2, new WackyPointsMethod(values.getValue(CompressionMethodType.WACKY_POINTS)));
			methods.add(3, new SingletonsMethod(values.getValue(CompressionMethodType.SINGLETONS)));
			methods.add(4, new DouglasPeuckerMethod(values.getValue(CompressionMethodType.DOUGLAS_PEUCKER)));
		}
		_panelController.setParameterValues(values);
		for (CompressionMethod method : methods) {
			_panelController.addMethod(method, getConfig());
		}
		if (_panelController.getNumPanels() == 0) {
			_panelController.addMethod(getConfig());
		}
		_panelController.refresh();
	}

	public void informNumPointsDeleted(int inNumPoints)
	{
		_summaryLabel.setValue(inNumPoints);
		_okButton.setEnabled(inNumPoints > 0);
	}
}
