package tim.prune.save;

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
import java.io.OutputStreamWriter;

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

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Class to manage the loading of GPS data using GpsBabel
 */
public class GpsSaver extends GenericFunction implements Runnable
{
	private boolean _gpsBabelChecked = false;
	private JDialog _dialog = null;
	private JTextField _deviceField = null, _formatField = null;
	private JTextField _trackNameField = null;
	private JCheckBox _waypointCheckbox = null, _trackCheckbox = null;
	private boolean _switchedWaypointsOff = false, _switchedTrackpointsOff = false;
	private JButton _okButton = null;
	private JProgressBar _progressBar = null;
	private boolean _cancelled = false;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GpsSaver(App inApp)
	{
		super(inApp);
	}

	/** get name key */
	public String getNameKey() {
		return "function.sendtogps";
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
				_dialog = new JDialog(_parentFrame, I18nManager.getText("function.sendtogps"), true);
				_dialog.setLocationRelativeTo(_parentFrame);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			// Initialise progress bars, buttons
			enableCheckboxes();
			enableOkButton();
			setupProgressBar(true);
			_trackNameField.requestFocus();
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
		gridPanel.add(_deviceField);
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.gpsload.format"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(formatLabel);
		_formatField = new JTextField(Config.getConfigString(Config.KEY_GPS_FORMAT), 12);
		gridPanel.add(_formatField);
		JLabel nameLabel = new JLabel(I18nManager.getText("dialog.gpssend.trackname"));
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(nameLabel);
		_trackNameField = new JTextField("", 12);
		gridPanel.add(_trackNameField);
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 20));
		mainPanel.add(gridPanel);
		// close dialog when escape pressed
		KeyAdapter closer = new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_deviceField.addKeyListener(closer);
		_formatField.addKeyListener(closer);
		_trackNameField.addKeyListener(closer);

		// checkboxes
		ChangeListener checkboxListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				enableOkButton();
			}
		};
		_waypointCheckbox = new JCheckBox(I18nManager.getText("dialog.gpssend.sendwaypoints"), true);
		_waypointCheckbox.addChangeListener(checkboxListener);
		_waypointCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_waypointCheckbox);
		_trackCheckbox = new JCheckBox(I18nManager.getText("dialog.gpssend.sendtracks"), true);
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
				new Thread(GpsSaver.this).start();
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
	 * Enable or disable the waypoints and trackpoints checkboxes
	 */
	private void enableCheckboxes()
	{
		// Enable or disable waypoints checkbox depending on whether data is available
		if (_waypointCheckbox.isSelected())
		{
			if (!_app.getTrackInfo().getTrack().hasWaypoints())
			{
				_waypointCheckbox.setSelected(false);
				_switchedWaypointsOff = true;
			}
			else _switchedWaypointsOff = false;
		}
		else if (_app.getTrackInfo().getTrack().hasWaypoints() && _switchedWaypointsOff)
		{
			_waypointCheckbox.setSelected(true);
			_switchedWaypointsOff = false;
		}
		// ... and the same for track points
		if (_trackCheckbox.isSelected())
		{
			if (!_app.getTrackInfo().getTrack().hasTrackPoints())
			{
				_trackCheckbox.setSelected(false);
				_switchedTrackpointsOff = true;
			}
			else _switchedTrackpointsOff = false;
		}
		else if (_app.getTrackInfo().getTrack().hasTrackPoints() && _switchedTrackpointsOff)
		{
			_trackCheckbox.setSelected(true);
			_switchedTrackpointsOff = false;
		}
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

		_progressBar.setIndeterminate(true);
		try
		{
			callGpsBabel();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(_dialog, e.getMessage(),
				I18nManager.getText("function.sendtogps"), JOptionPane.ERROR_MESSAGE);
			_cancelled = true;
		}

		setupProgressBar(true);
		enableOkButton();

		// Close dialog
		if (!_cancelled) {
			_dialog.dispose();
		}
	}


	/**
	 * Execute the call to gpsbabel
	 */
	private void callGpsBabel() throws Exception
	{
		// Set up command to call gpsbabel
		final String command = Config.getConfigString(Config.KEY_GPSBABEL_PATH);
		String[] commands = null;
		final String device = _deviceField.getText().trim();
		final String format = _formatField.getText().trim();
		if (_waypointCheckbox.isSelected() && _trackCheckbox.isSelected()) {
			// Both waypoints and track points selected
			commands = new String[] {command, "-w", "-t", "-i", "gpx", "-f", "-", "-o", format,
				"-F", device};
		}
		else
		{
			// Only waypoints OR trackpoints selected
			commands = new String[] {command, "-w", "-i", "gpx", "-f", "-", "-o", format,
				"-F", device};
			if (_trackCheckbox.isSelected()) {
				commands[1] = "-t";
			}
		}
		// Save GPS settings in config
		Config.setConfigString(Config.KEY_GPS_DEVICE, device);
		Config.setConfigString(Config.KEY_GPS_FORMAT, format);

		String errorMessage = "";
		Process process = Runtime.getRuntime().exec(commands);

		String trackName = _trackNameField.getText();
		if (trackName == null || trackName.equals("")) {trackName = "gpsprune";}
		// Generate the GPX file and send to the GPS
		OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
		SettingsForExport settings = new SettingsForExport();
		settings.setExportMissingAltitudesAsZero(true);
		GpxExporter.exportData(writer, _app.getTrackInfo(), trackName, null, settings, null);
		writer.close();

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
	}
}
