package tim.prune.function.filesleuth;

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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Function to start looking for track files meeting certain criteria.
 * This function just determines the start path and whether to search recursively,
 * before passing control to the FindFilesFunction.
 */
public class StartFindFilesFunction extends GenericFunction
{
	private JDialog _dialog = null;
	private JTextField _searchDirBox = null;
	private JCheckBox _subdirsCheckbox = null;
	private JButton _okButton = null;

	public StartFindFilesFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "menu.file.findfile";
	}

	@Override
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName());
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Set directory according to current config
		final String currPath = getConfig().getConfigString(Config.KEY_TRACK_DIR);
		_searchDirBox.setText(currPath == null ? "" : currPath);
		enableButtons();
		_dialog.setVisible(true);
	}

	/**
	 * Enable or disable the OK button
	 */
	private void enableButtons()
	{
		final String path = _searchDirBox.getText();
		boolean dirGood = false;
		if (!path.equals(""))
		{
			File dir = new File(path);
			dirGood = dir.exists() && dir.canRead() && dir.isDirectory();
		}
		_okButton.setEnabled(dirGood);
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
		// dir panel
		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());
		dirPanel.add(new JLabel(I18nManager.getText("dialog.findfile.dir") + " : "), BorderLayout.WEST);
		_searchDirBox = new JTextField(24);
		_searchDirBox.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent arg0) {
				super.keyReleased(arg0);
				enableButtons();
			}
		});
		_searchDirBox.addActionListener(e -> finish());
		dirPanel.add(_searchDirBox, BorderLayout.CENTER);
		JButton browseButton = new JButton(I18nManager.getText("button.browse"));
		browseButton.addActionListener(e -> chooseDir());
		dirPanel.add(browseButton, BorderLayout.EAST);
		_subdirsCheckbox = new JCheckBox(I18nManager.getText("dialog.open.includesubdirectories"), true);
		dirPanel.add(_subdirsCheckbox, BorderLayout.SOUTH);
		// holder panel so it doesn't expand vertically
		JPanel dirHolderPanel = new JPanel();
		dirHolderPanel.setLayout(new BorderLayout());
		dirHolderPanel.add(dirPanel, BorderLayout.NORTH);
		dialogPanel.add(dirHolderPanel, BorderLayout.CENTER);

		// OK, Cancel buttons at the bottom right
		JPanel buttonPanelr = new JPanel();
		buttonPanelr.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> finish());
		buttonPanelr.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanelr.add(cancelButton);

		// Put them together
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(buttonPanelr, BorderLayout.EAST);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Function activated by the "Browse..." button to select a directory
	 */
	private void chooseDir()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// Set start path from currently selected dir
		String path = _searchDirBox.getText();
		if (path.length() > 1) {
			chooser.setCurrentDirectory(new File(path));
		}
		if (chooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION) {
			_searchDirBox.setText(chooser.getSelectedFile().getAbsolutePath());
		}
		enableButtons();
	}

	/**
	 * OK pressed, start the search
	 */
	private void finish()
	{
		if (!_okButton.isEnabled()) {
			return;
		}
		File startDir = new File(_searchDirBox.getText());
		if (hasAnyFiles(startDir, _subdirsCheckbox.isSelected()))
		{
			new FindFilesFunction(_app, startDir, _subdirsCheckbox.isSelected()).begin();
			_dialog.dispose();
		}
		else {
			_app.showErrorMessage(getNameKey(), "error.findfile.nofilesfound");
		}
	}

	/**
	 * Check if the given path has any files to search through
	 */
	private boolean hasAnyFiles(File inPath, boolean inSubdirs)
	{
		if (inPath == null || !inPath.exists() || !inPath.canRead() || !inPath.isDirectory()) {
			return false;
		}
		File[] files = inPath.listFiles();
		if (files == null) {
			return false;
		}
		// Look through files in this directory
		for (File file : files)
		{
			if (file != null && file.isFile() && file.canRead()) {
				return true;
			}
		}
		if (inSubdirs)
		{
			// Also look in subdirectories
			for (File dir : files)
			{
				if (dir != null && dir.isDirectory() && dir.canRead() && hasAnyFiles(dir, true)) {
					return true;
				}
			}
		}
		// Nothing found
		return false;
	}
}
