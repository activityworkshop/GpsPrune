package tim.prune.function.gpsies;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.function.browser.BrowserLauncher;
import tim.prune.gui.GuiGridLayout;
import tim.prune.save.GpxExporter;
import tim.prune.save.SettingsForExport;

/**
 * Function to upload track information up to Gpsies.com
 */
public class UploadGpsiesFunction extends GenericFunction
{
	/** Dialog object */
	private JDialog _dialog = null;
	/** Edit box for user name */
	private JTextField _usernameField = null;
	/** Edit box for password */
	private JPasswordField _passwordField = null;
	/** Name of track */
	private JTextField _nameField = null;
	/** Description */
	private JTextArea _descField = null;
	/** Private checkbox */
	private JCheckBox _privateCheckbox = null;
	/** Activity checkboxes */
	private JCheckBox[] _activityCheckboxes = null;
	/** Writer object for GPX export */
	private OutputStreamWriter _writer = null;
	/** upload button */
	private JButton _uploadButton = null;

	/** URL to post form to */
	private static final String GPSIES_URL = "http://www.gpsies.com/upload.do";
	/** Keys for describing activities */
	private static final String[] ACTIVITY_KEYS = {"trekking", "walking", "jogging",
		"biking", "motorbiking", "snowshoe", "sailing", "skating"};

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public UploadGpsiesFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.uploadgpsies";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Initialise dialog, show empty list
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Show dialog
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		JPanel gridPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(gridPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.username")));
		_usernameField = new JTextField(15);
		grid.add(_usernameField);
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.password")));
		_passwordField = new JPasswordField(15);
		grid.add(_passwordField);
		// Track name and description
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.column.name")));
		_nameField = new JTextField(15);
		grid.add(_nameField);
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.description")));
		_descField = new JTextArea(5, 15);
		_descField.setLineWrap(true);
		_descField.setWrapStyleWord(true);
		grid.add(new JScrollPane(_descField));
		// Listener on all these text fields to enable/disable the ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				enableOK();
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_usernameField.addKeyListener(keyListener);
		_passwordField.addKeyListener(keyListener);
		_nameField.addKeyListener(keyListener);
		// Listen for tabs on description field, to change focus not enter tabs
		_descField.addKeyListener(new KeyAdapter() {
			/** Key pressed */
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_TAB) {
					inE.consume();
					if (inE.isShiftDown()) {
						_nameField.requestFocusInWindow();
					}
					else {
						_privateCheckbox.requestFocusInWindow();
					}
				}
			}
		});
		// Listen for Ctrl-backspace on password field (delete contents)
		_passwordField.addKeyListener(new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				if (inE.isControlDown() && (inE.getKeyCode() == KeyEvent.VK_BACK_SPACE
					|| inE.getKeyCode() == KeyEvent.VK_DELETE)) {
					_passwordField.setText("");
				}
			}
		});
		// Checkbox for private / public
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.keepprivate")));
		_privateCheckbox = new JCheckBox();
		_privateCheckbox.setSelected(true);
		grid.add(_privateCheckbox);

		// panel for activity type checkboxes
		JPanel activityPanel = new JPanel();
		activityPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		ChangeListener checkListener = new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				enableOK();
			}
		};
		// Why not a simple grid layout here?
		GuiGridLayout actGrid = new GuiGridLayout(activityPanel, new double[] {1.0, 1.0}, new boolean[] {false, false});
		final int numActivities = ACTIVITY_KEYS.length;
		_activityCheckboxes = new JCheckBox[numActivities];
		for (int i=0; i<numActivities; i++)
		{
			_activityCheckboxes[i] = new JCheckBox(I18nManager.getText("dialog.gpsies.activity." + ACTIVITY_KEYS[i]));
			_activityCheckboxes[i].addChangeListener(checkListener);
			actGrid.add(_activityCheckboxes[i]);
		}
		grid.add(new JLabel(I18nManager.getText("dialog.gpsies.activities")));
		grid.add(activityPanel);
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
		midPanel.add(gridPanel);
		dialogPanel.add(midPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_uploadButton = new JButton(I18nManager.getText("button.upload"));
		_uploadButton.setEnabled(false);
		_uploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startUpload();
			}
		});
		buttonPanel.add(_uploadButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * Check the inputs and enable or disable the upload button
	 */
	private void enableOK()
	{
		// Check for lengths of input fields - only username, password and filename are required
		boolean ok = (_usernameField.getText().length() > 0 && _nameField.getText().length() > 0);
		if (ok) {
			// also check password field
			char[] pass = _passwordField.getPassword();
			ok = pass.length > 0;
			for (int i=0; i<pass.length; i++) {pass[i] = '0';} // recommended by javadoc
			if (ok) {
				ok = false;
				for (int i=0; i<_activityCheckboxes.length; i++) {
					ok = ok || _activityCheckboxes[i].isSelected();
				}
			}
		}
		_uploadButton.setEnabled(ok);
	}


	/**
	 * Start the upload process (require separate thread?)
	 */
	private void startUpload()
	{
		BufferedReader reader = null;
		try
		{
			FormPoster poster = new FormPoster(new URL(GPSIES_URL));
			poster.setParameter("device", "Prune");
			poster.setParameter("username", _usernameField.getText());
			poster.setParameter("password", new String(_passwordField.getPassword()));
			boolean hasActivity = false;
			for (int i=0; i<ACTIVITY_KEYS.length; i++)
			{
				if (_activityCheckboxes[i].isSelected()) {
					hasActivity = true;
					poster.setParameter("trackTypes", ACTIVITY_KEYS[i]);
				}
			}
			if (!hasActivity) {poster.setParameter("trackTypes", "walking");} // default if none given
			poster.setParameter("filename", _nameField.getText());
			poster.setParameter("fileDescription", _descField.getText());
			poster.setParameter("startpointCountry", "DE");
			poster.setParameter("endpointCountry", "DE"); // both those will be corrected by gpsies
			poster.setParameter("status", (_privateCheckbox.isSelected()?"3":"1"));
			poster.setParameter("submit", "speichern"); // required
			// Use Pipes to connect the GpxExporter's output with the FormPoster's input
			PipedInputStream iStream = new PipedInputStream();
			PipedOutputStream oStream = new PipedOutputStream(iStream);
			_writer = new OutputStreamWriter(oStream);
			new Thread(new Runnable() {
				public void run() {
					try {
						GpxExporter.exportData(_writer, _app.getTrackInfo(), _nameField.getText(),
							null, new SettingsForExport(), null);
					} catch (IOException e) {}
					finally {
						try {_writer.close();} catch (IOException e) {}
					}
				}
			}).start();
			poster.setParameter("formFile", "filename.gpx", iStream);

			BufferedInputStream answer = new BufferedInputStream(poster.post());
			int response = poster.getResponseCode();
			reader = new BufferedReader(new InputStreamReader(answer));
			String line = reader.readLine();
			// Try to extract gpsies page url from the returned message
			String pageUrl = null;
			if (response == 200 && line.substring(0, 2).toUpperCase().equals("OK"))
			{
				final int bracketPos = line.indexOf('[');
				if (bracketPos > 0 && line.endsWith("]")) {
					pageUrl = line.substring(bracketPos + 1, line.length()-1);
				}
			}
			if (pageUrl != null)
			{
				// OK received and managed to extract a Url from the return message.
				int userChoice = JOptionPane.showConfirmDialog(_app.getFrame(),
					I18nManager.getText("dialog.gpsies.confirmopenpage"),
					I18nManager.getText(getNameKey()), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (userChoice == JOptionPane.OK_OPTION) {
					BrowserLauncher.launchBrowser(pageUrl);
				}
			}
			else {
				_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getText("error.gpsies.uploadnotok")
					+ ": " + line);
			}
		}
		catch (MalformedURLException e) {}
		catch (IOException ioe) {
			_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getText("error.gpsies.uploadfailed") + ": "
				+ ioe.getClass().getName() + " : " + ioe.getMessage());
		}
		finally {
			try {if (reader != null) reader.close();} catch (IOException e) {}
		}
		_dialog.dispose();
	}
}
