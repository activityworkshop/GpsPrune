package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;

/**
 * Class to show the popup window for setting the path to disk cache
 */
public class DiskCacheConfig extends GenericFunction
{
	private JDialog _dialog = null;
	private JCheckBox _cacheCheckbox = null;
	private JTextField _cacheDirBox = null;
	private JButton _browseButton = null;
	private JButton _okButton = null;
	private boolean _initialCheckState = false;
	private String _initialCacheDir = null;

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DiskCacheConfig(App inApp)
	{
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey()
	{
		return "function.diskcache";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 5));
		// top panel
		JPanel topPanel = new JPanel();
		_cacheCheckbox = new JCheckBox(I18nManager.getText("dialog.diskcache.save"));
		_cacheCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enableOk();
			}
		});
		topPanel.add(_cacheCheckbox);
		dialogPanel.add(topPanel, BorderLayout.NORTH);
		// dir panel
		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());
		dirPanel.add(new JLabel(I18nManager.getText("dialog.diskcache.dir")), BorderLayout.WEST);
		_cacheDirBox = new JTextField(24);
		_cacheDirBox.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent arg0) {
				super.keyReleased(arg0);
				enableOk();
			}
		});
		dirPanel.add(_cacheDirBox, BorderLayout.CENTER);
		_browseButton = new JButton(I18nManager.getText("button.browse"));
		_browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseDir();
			}
		});
		dirPanel.add(_browseButton, BorderLayout.EAST);
		dialogPanel.add(dirPanel, BorderLayout.CENTER);

		// Cancel button at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		});
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
	 * Enable or disable the ok button according to what's changed
	 */
	private void enableOk()
	{
		boolean checkState = _cacheCheckbox.isSelected();
		_cacheDirBox.setEditable(checkState);
		_browseButton.setEnabled(checkState);
		boolean ok = false;
		// If checkbox has stayed off then disable ok
		if (!_initialCheckState && !checkState) {ok = false;}
		else {
			// If checkbox has been switched off then enable
			if (!checkState) {ok = true;}
			else {
				// checkbox is on, check value
				String path = _cacheDirBox.getText();
				if (path.equals("") || (_initialCacheDir != null && path.equals(_initialCacheDir))) {
					// Value blank or same as before
					ok = false;
				}
				else {
					ok = true;
				}
			}
		}
		_okButton.setEnabled(ok);
	}

	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()));
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Set controls according to current config
		String currPath = Config.getConfigString(Config.KEY_DISK_CACHE);
		_cacheCheckbox.setSelected(currPath != null);
		_cacheDirBox.setText(currPath==null?"":currPath);
		enableOk();
		// Remember current state
		_initialCheckState = _cacheCheckbox.isSelected();
		_dialog.setVisible(true);
	}

	/**
	 * Function activated by the "Browse..." button to select a directory for the cache
	 */
	private void chooseDir()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// Set start path from currently selected dir
		String path = _cacheDirBox.getText();
		if (path.length() > 1) {chooser.setCurrentDirectory(new File(path));}
		if (chooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			_cacheDirBox.setText(chooser.getSelectedFile().getAbsolutePath());
		}
		enableOk();
	}

	/**
	 * OK pressed, save selected settings in Config
	 */
	private void finish()
	{
		String cachePath = (_cacheCheckbox.isSelected()?_cacheDirBox.getText():null);
		// Create dir if it doesn't exist already and creation confirmed
		if (cachePath != null)
		{
			File cacheDir = new File(cachePath);
			if ((!cacheDir.exists() || !cacheDir.isDirectory()) && (JOptionPane.showConfirmDialog(_dialog,
				I18nManager.getText("dialog.diskcache.createdir") + ": " + cacheDir.getAbsolutePath() + " ?",
				I18nManager.getText(getNameKey()), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION || !cacheDir.mkdir()))
			{
				JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.diskcache.nocreate"),
					I18nManager.getText(getNameKey()), JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		Config.setConfigString(Config.KEY_DISK_CACHE, cachePath);
		// inform subscribers so that tiles are wiped from memory and refetched
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}
}
