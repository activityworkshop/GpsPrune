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
import java.io.InputStreamReader;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.XmlHandler;

/**
 * Class to manage the loading of GPS data using GpsBabel
 */
public class GpsLoader extends GenericFunction implements Runnable
{
	private boolean _gpsBabelChecked = false;
	private JDialog _dialog = null;
	private JTextField _deviceField = null, _formatField = null;
	private JCheckBox _waypointCheckbox = null, _trackCheckbox = null;
	private JButton _okButton = null;
	private JProgressBar _progressBar = null;
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
			try
			{
				callGpsBabel(_waypointCheckbox.isSelected(), _trackCheckbox.isSelected());
			}
			catch (Exception e)
			{
				// System.err.println("Error: " + e.getClass().getName());
				// System.err.println("Error: " + e.getMessage());
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
	 * @param inWaypoints true to load waypoints
	 * @param inTracks true to load track points
	 */
	private void callGpsBabel(boolean inWaypoints, boolean inTracks) throws Exception
	{
		// Set up command to call gpsbabel
		String[] commands = null;
		final String device = _deviceField.getText().trim();
		final String format = _formatField.getText().trim();
		final String command = Config.getConfigString(Config.KEY_GPSBABEL_PATH);
		if (inWaypoints && inTracks) {
			// Both waypoints and track points selected
			commands = new String[] {command, "-w", "-t", "-i", format,
				"-f", device, "-o", "gpx", "-F", "-"};
		}
		else
		{
			// Only waypoints OR track points selected
			commands = new String[] {command, "-w", "-i", format,
				"-f", device, "-o", "gpx", "-F", "-"};
			if (inTracks) {
				commands[1] = "-t";
			}
		}
		// Save GPS settings in config
		Config.setConfigString(Config.KEY_GPS_DEVICE, device);
		Config.setConfigString(Config.KEY_GPS_FORMAT, format);

		String errorMessage = "";
		XmlHandler handler = null;
		Process process = Runtime.getRuntime().exec(commands);

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
		String line = null;
		String errorMessage2 = "";
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
		_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(),
			Altitude.Format.METRES, _deviceField.getText());
	}
}
