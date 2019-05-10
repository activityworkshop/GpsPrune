package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.SourceInfo;
import tim.prune.data.SourceInfo.FILE_TYPE;
import tim.prune.load.babel.BabelFilterPanel;

/**
 * Class to manage the loading of data from a GPS device using GpsBabel
 */
public class BabelLoadFromGps extends BabelLoader
{
	// Text fields for entering device and format
	private JTextField _deviceField = null, _formatField = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 */
	public BabelLoadFromGps(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.loadfromgps";
	}

	/** @return device name as file path */
	protected String getFilePath() {
		return _deviceField.getText();
	}

	/** @return Source info */
	protected SourceInfo getSourceInfo() {
		return new SourceInfo(_deviceField.getText(), FILE_TYPE.GPSBABEL);
	}

	/** @return input format */
	protected String getInputFormat() {
		return _formatField.getText();
	}

	/** @return true if function can be run */
	protected boolean isInputOk() {
		return _waypointCheckbox.isSelected() || _trackCheckbox.isSelected();
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
		gridPanel.setLayout(new GridLayout(0, 2, 10, 3));
		JLabel deviceLabel = new JLabel(I18nManager.getText("dialog.gpsload.device"));
		deviceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(deviceLabel);
		_deviceField = new JTextField(Config.getConfigString(Config.KEY_GPS_DEVICE), 12);
		KeyAdapter escapeListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_deviceField.addKeyListener(escapeListener);
		gridPanel.add(_deviceField);
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.gpsload.format"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gridPanel.add(formatLabel);
		_formatField = new JTextField(Config.getConfigString(Config.KEY_GPS_FORMAT), 12);
		_formatField.addKeyListener(escapeListener);
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
				new Thread(BabelLoadFromGps.this).start();
			}
		};
		_okButton.addActionListener(okListener);
		_deviceField.addActionListener(okListener);
		_formatField.addActionListener(okListener);
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
	 * Save GPS settings in config
	 */
	protected void saveConfigValues()
	{
		final String device = _deviceField.getText().trim();
		final String format = _formatField.getText().trim();
		final String filter = _filterPanel.getFilterString();
		Config.setConfigString(Config.KEY_GPS_DEVICE, device);
		Config.setConfigString(Config.KEY_GPS_FORMAT, format);
		Config.setConfigString(Config.KEY_GPSBABEL_FILTER, filter);
	}
}
