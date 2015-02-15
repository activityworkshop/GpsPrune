package tim.prune.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.SourceInfo;
import tim.prune.load.babel.BabelFilterPanel;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.XmlHandler;
import tim.prune.save.GpxExporter;

/**
 * Superclass to manage the loading of data using GpsBabel
 * Subclasses handle either from GPS or from file
 */
public abstract class BabelLoader extends GenericFunction implements Runnable
{
	private boolean _gpsBabelChecked = false;
	protected JDialog _dialog = null;
	// Checkboxes for which kinds of points to load
	protected JCheckBox _waypointCheckbox = null, _trackCheckbox = null;
	// Checkbox to save to file or not
	protected JCheckBox _saveCheckbox = null;
	protected JButton _okButton = null;
	protected JProgressBar _progressBar = null;
	protected File _saveFile = null;
	protected boolean _cancelled = false;
	protected BabelFilterPanel _filterPanel = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 */
	public BabelLoader(App inApp)
	{
		super(inApp);
	}

	/**
	 * Open the GUI to select options and start the load
	 */
	public void begin()
	{
		// Check if gpsbabel looks like it's installed
		if (_gpsBabelChecked || ExternalTools.isToolInstalled(ExternalTools.TOOL_GPSBABEL)
			|| JOptionPane.showConfirmDialog(_dialog,
				I18nManager.getText("dialog.gpsload.nogpsbabel"),
				I18nManager.getText(getNameKey()),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
		{
			_gpsBabelChecked = true;
			// Make dialog window
			if (_dialog == null)
			{
				_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
				_dialog.setLocationRelativeTo(_parentFrame);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			// Initialise progress bars, buttons
			enableOkButton();
			setupProgressBar(true);
			initDialog(); // do any subclass-specific init here
			_dialog.setVisible(true);
		}
	}


	/**
	 * @return a panel containing the main dialog components
	 */
	protected abstract JPanel makeDialogComponents();


	/** Do any subclass-specific dialog initialisation necessary */
	protected void initDialog()
	{
		// GPSBabel filter, if any
		_filterPanel.setFilterString(Config.getConfigString(Config.KEY_GPSBABEL_FILTER));
	}

	/**
	 * @param inStart true if the dialog is restarting
	 */
	private void setupProgressBar(boolean inStart)
	{
		// set visibility
		_progressBar.setVisible(!inStart);
		// set indeterminate flags, initial value
		_progressBar.setIndeterminate(false);
		_progressBar.setValue(0);
	}


	/**
	 * Enable or disable the ok button
	 */
	protected void enableOkButton()
	{
		_okButton.setEnabled(isInputOk());
	}

	/**
	 * @return true if input fields of dialog are valid
	 */
	protected abstract boolean isInputOk();

	/**
	 * Run method for performing tasks in separate thread
	 */
	public void run()
	{
		_okButton.setEnabled(false);
		setupProgressBar(false);
		if (isInputOk())
		{
			_progressBar.setIndeterminate(true);
			_saveFile = null;
			try
			{
				callGpsBabel();
			}
			catch (Exception e)
			{
				_app.showErrorMessageNoLookup(getNameKey(), e.getMessage());
				_cancelled = true;
			}
		}
		setupProgressBar(true);
		enableOkButton();

		// Close dialog
		if (!_cancelled) {
			_dialog.dispose();
		}
	}


	/**
	 * Execute the call to gpsbabel and pass the results back to the app
	 */
	private void callGpsBabel() throws Exception
	{
		// Set up command to call gpsbabel
		String[] commands = getCommandArray();
		// Save GPS settings in config
		saveConfigValues();

		String errorMessage = "", errorMessage2 = "";
		XmlHandler handler = null;
		Process process = Runtime.getRuntime().exec(commands);
		String line = null;

		if (_saveFile != null)
		{
			// data is being saved to file, so need to wait for it to finish
			process.waitFor();
			// try to read error message, if any
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while ((line = r.readLine()) != null) {
					errorMessage += line + "\n";
				}
				// Close error stream
				try {
					r.close();
				} catch (Exception e) {}
			}
			catch (Exception e) {} // couldn't get error message

			// Trigger it to be loaded by app
			if (process.exitValue() == 0)
			{
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ArrayList<File> fileList = new ArrayList<File>();
						fileList.add(_saveFile);
						_app.loadDataFiles(fileList);
					}
				});
			}
			else if (errorMessage.length() > 0) {
				throw new Exception(errorMessage);
			}
			else throw new Exception(I18nManager.getText("error.gpsload.unknown"));
		}
		else
		{
			// Pass input stream to try to parse the xml
			try
			{
				XmlFileLoader xmlLoader = new XmlFileLoader(_app);
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				saxParser.parse(process.getInputStream(), xmlLoader);
				handler = xmlLoader.getHandler();
				if (handler == null) {
					errorMessage = "Null handler";
				}
			}
			catch (Exception e) {
				errorMessage = e.getMessage();
			}

			// Read the error stream to see if there's a better error message there
			BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = r.readLine()) != null) {
				errorMessage2 += line + "\n";
			}
			// Close error stream
			try {
				r.close();
			} catch (Exception e) {}

			if (errorMessage2.length() > 0) {errorMessage = errorMessage2;}
			if (errorMessage.length() > 0) {throw new Exception(errorMessage);}

			// Send data back to app
			_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(), null,
				getSourceInfo(), handler.getTrackNameList());
		}
	}


	/**
	 * Get the commands to call
	 * @return String array containing commands
	 */
	private String[] getCommandArray()
	{
		ArrayList<String> commandList = new ArrayList<String>();
		// Firstly the command for gpsbabel itself
		final String command = Config.getConfigString(Config.KEY_GPSBABEL_PATH);
		commandList.add(command);
		// Then whether to load waypoints or track points
		final boolean loadWaypoints = _waypointCheckbox.isSelected();
		final boolean loadTrack = _trackCheckbox.isSelected();
		if (loadWaypoints) {
			commandList.add("-w");
		}
		if (loadTrack) {
			commandList.add("-t");
		}
		// Input format
		commandList.add("-i");
		commandList.add(getInputFormat());
		// File path
		commandList.add("-f");
		commandList.add(getFilePath());
		// Filters, if any
		final String filter = _filterPanel.getFilterString();
		if (filter != null && !filter.equals(""))
		{
			for (String arg : filter.split(" "))
			{
				if (arg.length() > 0) {
					commandList.add(arg);
				}
			}
		}
		// Output format
		commandList.add("-o");
		commandList.add("gpx");
		// Where to
		commandList.add("-F");
		String whereTo = "-";
		// Do we want to save the gpx straight to file?
		if (_saveCheckbox.isSelected())
		{
			// Select file to save to
			_saveFile = GpxExporter.chooseGpxFile(_parentFrame);
			if (_saveFile != null) {
				whereTo = _saveFile.getAbsolutePath();
			}
		}
		commandList.add(whereTo);
		// Convert to string array
		String[] args = new String[] {};
		return commandList.toArray(args);
	}

	/**
	 * @return SourceInfo object corresponding to the load
	 */
	protected abstract SourceInfo getSourceInfo();

	/**
	 * @return complete file path or device path for gpsbabel call
	 */
	protected abstract String getFilePath();

	/**
	 * @return file name or device name
	 */
	protected abstract String getInputFormat();

	/**
	 * Save any config values necessary
	 */
	protected abstract void saveConfigValues();
}
