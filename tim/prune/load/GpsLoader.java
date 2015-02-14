package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.SourceInfo;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.XmlHandler;
import tim.prune.save.GpxExporter;

/**
 * Class to manage the loading of GPS data using GpsBabel
 */
public class GpsLoader extends GenericFunction implements Runnable
{
	private boolean _gpsBabelChecked = false;
	private JDialog _dialog = null;
	private JTextField _deviceField = null, _formatField = null;
	private JCheckBox _waypointCheckbox = null, _trackCheckbox = null;
	private JCheckBox _saveCheckbox = null;
	private JButton _okButton = null;
	private JProgressBar _progressBar = null;
	private File _saveFile = null;
	private boolean _cancelled = false;


	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 */
	public GpsLoader(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.loadfromgps";
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
			_dialog.setVisible(true);
		}
	}


	/**
	 * @return a panel containing the main dialog components
	 */
	private JPanel makeDialogComponents()
	{
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		// Main panel with options etc
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// text fields for options
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 2, 10, 3));
		JLabel deviceLabel = new JLabel(I18nManager.getText("dialog.gpsload.device"));
		deviceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(deviceLabel);
		_deviceField = new JTextField(Config.getConfigString(Config.KEY_GPS_DEVICE), 12);
		_deviceField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		});
		gridPanel.add(_deviceField);
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.gpsload.format"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(formatLabel);
		_formatField = new JTextField(Config.getConfigString(Config.KEY_GPS_FORMAT), 12);
		gridPanel.add(_formatField);
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

		// progress bar (initially invisible)
		_progressBar = new JProgressBar(0, 10);
		mainPanel.add(_progressBar);
		outerPanel.add(mainPanel, BorderLayout.NORTH);

		// Lower panel with ok and cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// start thread to call gpsbabel
				_cancelled = false;
				new Thread(GpsLoader.this).start();
			}
		});
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
	private void enableOkButton()
	{
		_okButton.setEnabled(_waypointCheckbox.isSelected() || _trackCheckbox.isSelected());
	}


	/**
	 * Run method for performing tasks in separate thread
	 */
	public void run()
	{
		_okButton.setEnabled(false);
		setupProgressBar(false);
		if (_waypointCheckbox.isSelected() || _trackCheckbox.isSelected())
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
		final String device = _deviceField.getText().trim();
		final String format = _formatField.getText().trim();
		String[] commands = getCommandArray(device, format);
		// Save GPS settings in config
		Config.setConfigString(Config.KEY_GPS_DEVICE, device);
		Config.setConfigString(Config.KEY_GPS_FORMAT, format);

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
			_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(), Altitude.Format.METRES,
				new SourceInfo(_deviceField.getText(), SourceInfo.FILE_TYPE.GPSBABEL),
				handler.getTrackNameList());
		}
	}


	/**
	 * Get the commands to call
	 * @param inDevice device name to use
	 * @param inFormat format to use
	 * @return String array containing commands
	 */
	private String[] getCommandArray(String inDevice, String inFormat)
	{
		String[] commands = null;
		final String command = Config.getConfigString(Config.KEY_GPSBABEL_PATH);
		final boolean loadWaypoints = _waypointCheckbox.isSelected();
		final boolean loadTrack = _trackCheckbox.isSelected();
		if (loadWaypoints && loadTrack) {
			// Both waypoints and track points selected
			commands = new String[] {command, "-w", "-t", "-i", inFormat,
				"-f", inDevice, "-o", "gpx", "-F", "-"};
		}
		else
		{
			// Only waypoints OR track points selected
			commands = new String[] {command, "-w", "-i", inFormat,
				"-f", inDevice, "-o", "gpx", "-F", "-"};
			if (loadTrack) {
				commands[1] = "-t";
			}
		}
		// Do we want to save the gpx straight to file?
		if (_saveCheckbox.isSelected()) {
			// Select file to save to
			_saveFile = GpxExporter.chooseGpxFile(_parentFrame);
			if (_saveFile != null) {
				commands[commands.length-1] = _saveFile.getAbsolutePath();
			}
		}
		return commands;
	}
}
