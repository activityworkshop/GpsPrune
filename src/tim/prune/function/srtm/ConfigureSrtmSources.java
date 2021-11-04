package tim.prune.function.srtm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Configure SRTM sources, including authentication for the NASA Earthdata systems
 * @author fperrin, activityworkshop
 */
public class ConfigureSrtmSources extends GenericFunction
{
	private JDialog _dialog = null;
	private JCheckBox _oneSecondCheck = null;
	private boolean _oneSecondOriginallyOn = false;
	private String _authString = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public ConfigureSrtmSources(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.configuresrtmsources";
	}

	/**
	 * Function entry point
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			// Create Gui and show it
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		prefillCurrentAuth();
		_dialog.setVisible(true);
	}

	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		// Make a central panel with boxes for each of 3-second and 1-second sources
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel introPanel = new JPanel();
		introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));
		introPanel.add(new JLabel(I18nManager.getText("dialog.configuresrtm.intro1")));
		introPanel.add(new JLabel(I18nManager.getText("dialog.configuresrtm.intro2")));
		mainPanel.add(introPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		// 3-second source
		JPanel threeSecondPanel = new JPanel();
		threeSecondPanel.setBorder(BorderFactory.createTitledBorder(""));
		threeSecondPanel.setLayout(new BoxLayout(threeSecondPanel, BoxLayout.Y_AXIS));
		JCheckBox threeSecondCheck = new JCheckBox(I18nManager.getText("dialog.configuresrtm.threesecond"));
		threeSecondCheck.setSelected(true);
		threeSecondCheck.setEnabled(false);
		threeSecondPanel.add(threeSecondCheck);
		threeSecondPanel.add(new JLabel(I18nManager.getText("dialog.configuresrtm.threesecond.desc")));

		mainPanel.add(threeSecondPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		// 1-second source
		JPanel oneSecondPanel = new JPanel();
		oneSecondPanel.setBorder(BorderFactory.createTitledBorder(""));
		oneSecondPanel.setLayout(new BoxLayout(oneSecondPanel, BoxLayout.Y_AXIS));

		_oneSecondCheck = new JCheckBox(I18nManager.getText("dialog.configuresrtm.onesecond"));
		_oneSecondCheck.setSelected(true);
		oneSecondPanel.add(_oneSecondCheck);
		_oneSecondCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent inEvent) {
				if (_oneSecondCheck.isSelected()) {
					setupEarthdataAuth();
				}
				_okButton.setEnabled(_oneSecondCheck.isSelected() != _oneSecondOriginallyOn);
			}
		});
		oneSecondPanel.add(new JLabel(I18nManager.getText("dialog.configuresrtm.onesecond.desc1")));
		oneSecondPanel.add(new JLabel(I18nManager.getText("dialog.configuresrtm.onesecond.desc2")));

		mainPanel.add(oneSecondPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// ok / cancel buttons at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		});
		buttonPanel.add(_okButton);
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
	 * User has activated the 1-second checkbox, so we need to ask for username / password details
	 * and save the result
	 */
	private void setupEarthdataAuth()
	{
		Object[] buttonTexts = {I18nManager.getText("button.yes"), I18nManager.getText("button.no")};
		int showWebsiteAnswer = JOptionPane.showOptionDialog(_parentFrame,
			I18nManager.getText("dialog.configuresrtm.showregistrationwebsite"),
			I18nManager.getText(getNameKey()), JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, buttonTexts, buttonTexts[1]);
		if (showWebsiteAnswer == JOptionPane.YES_OPTION) {
			BrowserLauncher.launchBrowser("https://urs.earthdata.nasa.gov/users/new");
		}
		// Now get the registered user id
		Object userId = JOptionPane.showInputDialog(_app.getFrame(),
			I18nManager.getText("dialog.configuresrtm.userid"),
			I18nManager.getText(getNameKey()),
			JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (userId == null || userId.equals(""))
		{
			_oneSecondCheck.setSelected(false);
			return;
		}
		Object password = JOptionPane.showInputDialog(_app.getFrame(),
			I18nManager.getText("dialog.configuresrtm.password"),
			I18nManager.getText(getNameKey()),
			JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (password == null || password.equals(""))
		{
			_oneSecondCheck.setSelected(false);
			return;
		}
		String authString = Base64.getEncoder().encodeToString((userId.toString() + ":" + password.toString()).getBytes());
		if (isAuthValid(authString)) {
			_authString = authString;
		}
		else
		{
			JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.configuresrtm.loginfailed"),
				I18nManager.getText(getNameKey()), JOptionPane.ERROR_MESSAGE);
			_oneSecondCheck.setSelected(false);
		}
	}

	/**
	 * React to 'OK' being pressed, to save the config
	 */
	private void finish()
	{
		if (_oneSecondCheck.isSelected())
		{
			if (_authString != null) {
				Config.setConfigString(Config.KEY_EARTHDATA_AUTH, _authString);
			}
		}
		else {
			Config.setConfigString(Config.KEY_EARTHDATA_AUTH, null);
		}
		_dialog.dispose();
	}

	/**
	 * Init the dialog according to the saved authentication config
	 */
	private void prefillCurrentAuth()
	{
		_oneSecondCheck.setSelected(isAuthValid(Config.getConfigString(Config.KEY_EARTHDATA_AUTH)));
		_authString = null;
		_oneSecondOriginallyOn = _oneSecondCheck.isSelected();
		_okButton.setEnabled(false);
	}

	/**
	 * Check the given config string for successful authentication using the NASA server
	 */
	private boolean isAuthValid(String inConfigString)
	{
		// TODO: Use this string to login to NASA server and check success or failure
		return inConfigString != null && !inConfigString.isEmpty();
	}
}
