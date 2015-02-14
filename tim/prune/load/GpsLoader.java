package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.XmlHandler;

/**
 * Class to manage the loading of GPS data using GpsBabel
 */
public class GpsLoader implements Runnable
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private boolean _gpsBabelChecked = false;
	private JDialog _dialog = null;
	private JTextField _deviceField = null, _formatField = null;
	private JCheckBox _waypointCheckbox = null, _trackCheckbox = null;
	private JButton _okButton = null;
	private JProgressBar _waypointProgressBar = null, _trackProgressBar = null;
	private boolean _cancelled = false;


	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public GpsLoader(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Open the GUI to select options and start the load
	 */
	public void openDialog()
	{
		// Check if gpsbabel looks like it's installed
		if (_gpsBabelChecked || ExternalTools.isGpsbabelInstalled()
			|| JOptionPane.showConfirmDialog(_dialog,
				I18nManager.getText("dialog.gpsload.nogpsbabel"),
				I18nManager.getText("dialog.gpsload.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
		{
			_gpsBabelChecked = true;
			// Make dialog window
			if (_dialog == null)
			{
				_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.gpsload.title"), true);
				_dialog.setLocationRelativeTo(_parentFrame);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			// Initialise progress bars, buttons
			enableOkButton();
			setupProgressBars(true);
			_dialog.show();
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
		_deviceField = new JTextField(Config.getGpsDevice(), 12);
		gridPanel.add(_deviceField);
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.gpsload.format"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(formatLabel);
		_formatField = new JTextField(Config.getGpsFormat(), 12);
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
		// progress bars (initially invisible)
		_waypointProgressBar = new JProgressBar(0, 10);
		mainPanel.add(_waypointProgressBar);
		_trackProgressBar = new JProgressBar(0, 10);
		mainPanel.add(_trackProgressBar);
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
	private void setupProgressBars(boolean inStart)
	{
		// set visibility
		_waypointProgressBar.setVisible(!inStart && _waypointCheckbox.isSelected());
		_trackProgressBar.setVisible(!inStart && _trackCheckbox.isSelected());
		// set indeterminate flags, initial value
		_waypointProgressBar.setIndeterminate(false);
		_waypointProgressBar.setValue(0);
		_trackProgressBar.setIndeterminate(false);
		_trackProgressBar.setValue(0);
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
		setupProgressBars(false);
		if (_waypointCheckbox.isSelected())
		{
			_waypointProgressBar.setIndeterminate(true);
			_trackProgressBar.setIndeterminate(false);
			try
			{
				callGpsBabel(true);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(_dialog, e.getMessage(),
					I18nManager.getText("dialog.gpsload.title"), JOptionPane.ERROR_MESSAGE);
				_cancelled = true;
			}
		}
		// Exit if cancelled or failed
		if (_cancelled) {
			setupProgressBars(true);
			enableOkButton();
			return;
		}
		if (_trackCheckbox.isSelected())
		{
			_waypointProgressBar.setIndeterminate(false);
			_waypointProgressBar.setValue(10);
			_trackProgressBar.setIndeterminate(true);
			try
			{
				callGpsBabel(false);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(_dialog, e.getMessage(),
					I18nManager.getText("dialog.gpsload.title"), JOptionPane.ERROR_MESSAGE);
				_cancelled = true;
			}
		}
		setupProgressBars(true);
		enableOkButton();

		// Close dialog
		if (!_cancelled) {
			_dialog.dispose();
		}
	}


	/**
	 * Execute the call to gpsbabel and pass the results back to the app
	 * @param inWaypoints true to get waypoints, false to get track data
	 */
	private void callGpsBabel(boolean inWaypoints) throws Exception
	{
		// Set up command to call gpsbabel
		String[] commands = {"gpsbabel", null, "-i", _formatField.getText(), "-f", _deviceField.getText(), "-o", "gpx", "-F", "-"};
		commands[1] = inWaypoints?"-w":"-t";

		String errorMessage = "";
		XmlHandler handler = null;
		Process process = Runtime.getRuntime().exec(commands);

		// Pass input stream to try to parse the xml
		try
		{
			XmlFileLoader xmlLoader = new XmlFileLoader(_app, _parentFrame);
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
		if (errorMessage2.length() > 0) {errorMessage = errorMessage2;}
		if (errorMessage.length() > 0) {throw new Exception(errorMessage);}

		// Send data back to app
		boolean append = _waypointCheckbox.isSelected() && !inWaypoints;
		_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(),
			Altitude.FORMAT_METRES, _deviceField.getText(), append);
	}
}
