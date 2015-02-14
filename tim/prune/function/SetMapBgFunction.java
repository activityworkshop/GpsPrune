package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;

/**
 * Function to set the tile server for the map backgrounds
 */
public class SetMapBgFunction extends GenericFunction
{
	private JDialog _dialog = null;
	private JButton _okButton = null;
	private JRadioButton[] _serverRadios = null;
	private JTextField _serverUrl = null;
	/** Index of 'other' server with freeform url */
	private static final int OTHER_SERVER_NUM = 3;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetMapBgFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.setmapbg";
	}

	/**
	 * Begin the function
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
			initValues();
			_dialog.pack();
		}
		enableOK();
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
		// Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		_serverRadios = new JRadioButton[4];
		ButtonGroup serverRadioGroup = new ButtonGroup();
		String[] serverKeys = {"dialog.setmapbg.mapnik", "dialog.setmapbg.osma",
			"dialog.setmapbg.cyclemap", "dialog.setmapbg.other"};
		// action listener for radios
		ActionListener changeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enableOK();
			}
		};
		// Create four radio buttons
		for (int i=0; i<4; i++)
		{
			_serverRadios[i] = new JRadioButton(I18nManager.getText(serverKeys[i]));
			_serverRadios[i].addActionListener(changeListener);
			serverRadioGroup.add(_serverRadios[i]);
			mainPanel.add(_serverRadios[i]);
		}
		// entry field for other server urls
		mainPanel.add(new JLabel(I18nManager.getText("dialog.setmapbg.server")));
		_serverUrl = new JTextField("", 12);
		_serverUrl.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				enableOK();
			}
		});
		mainPanel.add(_serverUrl);
		dialogPanel.add(mainPanel, BorderLayout.NORTH);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
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
		return dialogPanel;
	}


	/**
	 * Get the initial values from the Config and set gui values accordingly
	 */
	private void initValues()
	{
		// Get values from config
		try {
			_serverRadios[Config.getMapServerIndex()].setSelected(true);
		}
		catch (ArrayIndexOutOfBoundsException e) {} // ignore
		String url = Config.getMapServerUrl();
		if (url != null) {_serverUrl.setText(url);}
		// Choose default if none selected
		if (getSelectedServer() < 0) {
			_serverRadios[0].setSelected(true);
		}
	}

	/**
	 * @return index of selected radio button, or -1 if none
	 */
	private int getSelectedServer()
	{
		// Loop over all four radios
		for (int i=0; i<4; i++) {
			if (_serverRadios[i].isSelected()) {return i;}
		}
		// None selected
		return -1;
	}

	/**
	 * Enable or disable the OK button according to the selection
	 */
	private void enableOK()
	{
		int serverNum = getSelectedServer();
		_okButton.setEnabled(inputOK());
		_serverUrl.setEnabled(serverNum == OTHER_SERVER_NUM);
	}

	/**
	 * @return true if inputs are ok
	 */
	private boolean inputOK()
	{
		int serverNum = getSelectedServer();
		return serverNum >= 0 && (serverNum != OTHER_SERVER_NUM || _serverUrl.getText().length() > 4);
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		int serverNum = getSelectedServer();
		if (!inputOK()) {serverNum = 0;}
		Config.setMapServerIndex(serverNum);
		Config.setMapServerUrl(_serverUrl.getText());
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}
}
