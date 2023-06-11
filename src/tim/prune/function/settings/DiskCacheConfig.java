package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.function.cache.ManageCacheFunction;

/**
 * Class to show the popup window for setting the path to disk cache
 */
public class DiskCacheConfig extends GenericFunction
{
	private JDialog _dialog = null;
	private JCheckBox _cacheCheckbox = null;
	private JTextField _cacheDirBox = null;
	private JButton _browseButton = null;
	private JButton _okButton = null, _manageButton = null;
	private boolean _initialCheckState = false;
	private String _initialCacheDir = null;

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DiskCacheConfig(App inApp) {
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey() {
		return "function.diskcache";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		dialogPanel.setLayout(new BorderLayout(0, 5));
		// top panel
		JPanel topPanel = new JPanel();
		_cacheCheckbox = new JCheckBox(I18nManager.getText("dialog.diskcache.save"));
		_cacheCheckbox.addActionListener(e -> enableButtons());
		topPanel.add(_cacheCheckbox);
		dialogPanel.add(topPanel, BorderLayout.NORTH);
		// dir panel
		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());
		dirPanel.add(new JLabel(I18nManager.getText("dialog.diskcache.dir") + " : "), BorderLayout.WEST);
		_cacheDirBox = new JTextField(24);
		_cacheDirBox.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent arg0) {
				super.keyReleased(arg0);
				enableButtons();
			}
		});
		dirPanel.add(_cacheDirBox, BorderLayout.CENTER);
		_browseButton = new JButton(I18nManager.getText("button.browse"));
		_browseButton.addActionListener(e -> chooseDir());
		dirPanel.add(_browseButton, BorderLayout.EAST);
		// holder panel so it doesn't expand vertically
		JPanel dirHolderPanel = new JPanel();
		dirHolderPanel.setLayout(new BorderLayout());
		dirHolderPanel.add(dirPanel, BorderLayout.NORTH);
		dialogPanel.add(dirHolderPanel, BorderLayout.CENTER);

		// OK, Cancel buttons at the bottom right
		JPanel buttonPanelr = new JPanel();
		buttonPanelr.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> {
			finish();
			_dialog.dispose();
		});
		buttonPanelr.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanelr.add(cancelButton);

		// Manage button at the bottom left
		JPanel buttonPanell = new JPanel();
		buttonPanell.setLayout(new FlowLayout(FlowLayout.LEFT));
		_manageButton = new JButton(I18nManager.getText("button.manage"));
		_manageButton.addActionListener(e -> {
			finish();
			new ManageCacheFunction(_app).begin();
		});
		buttonPanell.add(_manageButton);
		// Put them together
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(buttonPanelr, BorderLayout.EAST);
		buttonPanel.add(buttonPanell, BorderLayout.WEST);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Enable or disable the buttons according to what's changed
	 */
	private void enableButtons()
	{
		final boolean checkState = _cacheCheckbox.isSelected();
		final String path = _cacheDirBox.getText();
		_cacheDirBox.setEditable(checkState);
		_browseButton.setEnabled(checkState);
		boolean ok = false;
		// If checkbox has stayed off then disable ok
		if (!_initialCheckState && !checkState) {
			ok = false;
		}
		else
		{
			// If checkbox has been switched off then enable
			ok = true;
			if (checkState)
			{
				// checkbox is on, check value
				if (path.equals("") || path.equals(_initialCacheDir)) {
					// Value blank or same as before
					ok = false;
				}
			}
		}
		_okButton.setEnabled(ok);
		// Manage button needs a valid cache
		boolean cacheDirGood = false;
		if (checkState && !path.equals(""))
		{
			File dir = new File(path);
			cacheDirGood = dir.exists() && dir.canRead() && dir.isDirectory();
		}
		_manageButton.setEnabled(cacheDirGood);
	}

	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName());
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Set controls according to current config
		final String currPath = Config.getConfigString(Config.KEY_DISK_CACHE);
		_cacheCheckbox.setSelected(currPath != null);
		_cacheDirBox.setText(currPath==null?"":currPath);
		_initialCacheDir = currPath;
		enableButtons();
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
		if (path.length() > 1) {
			chooser.setCurrentDirectory(new File(path));
		}
		if (chooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION) {
			_cacheDirBox.setText(chooser.getSelectedFile().getAbsolutePath());
		}
		enableButtons();
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
				getName(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION || !cacheDir.mkdir()))
			{
				JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.diskcache.nocreate"),
					getName(), JOptionPane.WARNING_MESSAGE);
				return;
			}
			// Check that the cache path is writable too, and give warning if not
			if (cacheDir.exists() && cacheDir.isDirectory() && !cacheDir.canWrite())
			{
				JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.diskcache.cannotwrite"),
					getName(), JOptionPane.WARNING_MESSAGE);
			}
		}
		Config.setConfigString(Config.KEY_DISK_CACHE, cachePath);
		// inform subscribers so that tiles are wiped from memory and refetched
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
	}
}
