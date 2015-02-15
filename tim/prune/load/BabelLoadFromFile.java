package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.SourceInfo;
import tim.prune.data.SourceInfo.FILE_TYPE;
import tim.prune.gui.GuiGridLayout;
import tim.prune.load.babel.BabelFilterPanel;


/**
 * Class to manage the loading of data from a file using GpsBabel.
 * This allows the use of Gpsbabel's importing functions to convert to gpx.
 */
public class BabelLoadFromFile extends BabelLoader
{
	// file chooser
	private JFileChooser _fileChooser = null;
	// Input file
	private File _inputFile = null;
	// Label for filename
	private JLabel _inputFileLabel = null;
	// Dropdown for format of file
	private JComboBox<String> _formatDropdown = null;
	// Last used file suffix
	private String _lastSuffix = null;

	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 */
	public BabelLoadFromFile(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.importwithgpsbabel";
	}

	/** @return complete input file path for gpsbabel call */
	protected String getFilePath() {
		return _inputFile.getAbsolutePath();
	}

	/** @return Source info */
	protected SourceInfo getSourceInfo() {
		return new SourceInfo(_inputFile, FILE_TYPE.GPSBABEL);
	}

	/** @return input format */
	protected String getInputFormat() {
		return BabelFileFormats.getFormat(_formatDropdown.getSelectedIndex());
	}

	/** @return true if function can be run */
	protected boolean isInputOk() {
		return _inputFile.exists() && _inputFile.canRead();
	}

	/**
	 * Override the begin method to specify input file first
	 */
	public void begin()
	{
		// Construct file chooser if necessary
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			// start from directory in config if already set
			String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
			if (configDir == null) {configDir = Config.getConfigString(Config.KEY_PHOTO_DIR);}
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
			_fileChooser.setMultiSelectionEnabled(false); // Single files only
		}
		// Show the open dialog
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			_inputFile = _fileChooser.getSelectedFile();
			if (_inputFile != null && isInputOk()) {
				super.begin();
			}
		}
	}

	/**
	 * Begin the load function with a previously-specified file
	 * @param inFile file to load
	 */
	public void beginWithFile(File inFile)
	{
		_inputFile = inFile;
		super.begin();
	}

	/**
	 * @return a panel containing the main dialog components
	 */
	protected JPanel makeDialogComponents()
	{
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		// Main panel with options etc
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// text fields for options
		JPanel gridPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(gridPanel);
		JLabel nameLabel = new JLabel(I18nManager.getText("details.track.file"));
		grid.add(nameLabel);
		_inputFileLabel = new JLabel("------------");
		grid.add(_inputFileLabel);
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.gpsload.format"));
		grid.add(formatLabel);
		_formatDropdown = new JComboBox<String>(BabelFileFormats.getDescriptions());
		grid.add(_formatDropdown);
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 20));
		mainPanel.add(gridPanel);

		// checkboxes
		ChangeListener checkboxListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				enableOkButton();
			}
		};
		_waypointCheckbox = new JCheckBox(I18nManager.getText("dialog.gpsload.getwaypoints"), true);
		_waypointCheckbox.addChangeListener(checkboxListener);
		_waypointCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_waypointCheckbox);
		_trackCheckbox = new JCheckBox(I18nManager.getText("dialog.gpsload.gettracks"), true);
		_trackCheckbox.addChangeListener(checkboxListener);
		_trackCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_trackCheckbox);
		// Checkbox for immediately saving to file
		_saveCheckbox = new JCheckBox(I18nManager.getText("dialog.gpsload.save"));
		_saveCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_saveCheckbox);

		// Filter panel
		_filterPanel = new BabelFilterPanel(_parentFrame);
		// Give filter panel the contents of the config
		String filter = Config.getConfigString(Config.KEY_GPSBABEL_FILTER);
		if (filter != null) {
			_filterPanel.setFilterString(filter);
		}
		mainPanel.add(_filterPanel);

		// progress bar (initially invisible)
		_progressBar = new JProgressBar(0, 10);
		mainPanel.add(_progressBar);
		outerPanel.add(mainPanel, BorderLayout.NORTH);

		// Lower panel with ok and cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// start thread to call gpsbabel
				_cancelled = false;
				new Thread(BabelLoadFromFile.this).start();
			}
		};
		_okButton.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_cancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		outerPanel.add(buttonPanel, BorderLayout.SOUTH);
		return outerPanel;
	}

	/**
	 * @return the suffix of the selected filename
	 */
	private String getSelectedSuffix()
	{
		String filename = _inputFile.getName();
		if (filename == null) {return "";}
		int dotPos = filename.lastIndexOf('.');
		return (dotPos > 0 ? filename.substring(dotPos) : "");
	}

	/**
	 * Initialise dialog
	 */
	protected void initDialog()
	{
		_inputFileLabel.setText(_inputFile.getName());
		// Get suffix of filename and compare with previous one
		String suffix = getSelectedSuffix();
		if (_lastSuffix == null || !suffix.equalsIgnoreCase(_lastSuffix))
		{
			// New suffix has been chosen, so select first appropriate format (if any)
			int selIndex = BabelFileFormats.getIndexForFileSuffix(suffix);
			if (selIndex < 0)
			{
				// Use the previous one from the Config (if any)
				selIndex = Config.getConfigInt(Config.KEY_IMPORT_FILE_FORMAT);
			}
			if (selIndex >= 0) {
				_formatDropdown.setSelectedIndex(selIndex);
			}
		}
		_lastSuffix = suffix;
	}

	/**
	 * Save settings in config
	 */
	protected void saveConfigValues()
	{
		// Save the filter string, clear it if it's now blank
		final String filter = _filterPanel.getFilterString();
		Config.setConfigString(Config.KEY_GPSBABEL_FILTER, filter);

		// Check if there is a standard file type for the selected suffix
		int selIndex = BabelFileFormats.getIndexForFileSuffix(getSelectedSuffix());
		// If there is none, then get the index which the user chose and set in the Config
		if (selIndex < 0) {
			Config.setConfigInt(Config.KEY_IMPORT_FILE_FORMAT, _formatDropdown.getSelectedIndex());
		}
	}
}
