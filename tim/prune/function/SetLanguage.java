package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.load.GenericFileFilter;

/**
 * Class to show the popup window for setting the language code / file
 */
public class SetLanguage extends GenericFunction
{
	private JDialog _dialog = null;
	private JComboBox _languageDropDown = null;
	private JTextField _langFileBox = null;
	private int _startIndex = 0;

	/** Names of languages for display in dropdown (not translated) */
	private static final String[] LANGUAGE_NAMES = {"\u010de\u0161tina", "deutsch", "english",
		"espa\u00F1ol", "fran\u00E7ais", "italiano", "magyar", "nederlands", "polski",
		"portugu\u00EAs", "\u4e2d\u6587 (chinese)", "\u65E5\u672C\u8A9E (japanese)",
		"\uD55C\uAD6D\uC5B4/\uC870\uC120\uB9D0 (korean)", "schwiizerd\u00FC\u00FCtsch", "t\u00FCrk\u00E7e",
		"rom\u00E2n\u0103", "afrikaans", "bahasa indonesia", "farsi"
	};
	/** Associated language codes (must be in same order as names!) */
	private static final String[] LANGUAGE_CODES = {"cz", "de", "en", "es", "fr", "it", "hu",
		"nl", "pl", "pt", "zh", "ja", "ko", "de_ch", "tr", "ro", "af", "in", "fa"
	};


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetLanguage(App inApp)
	{
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey()
	{
		return "function.setlanguage";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 5));
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
		midPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		// description labels
		JLabel label1 = new JLabel("<html>" + I18nManager.getText("dialog.setlanguage.firstintro") + "</html>");
		label1.setAlignmentX(Component.LEFT_ALIGNMENT);
		label1.setHorizontalAlignment(SwingConstants.LEFT);
		midPanel.add(label1);
		JLabel label2 = new JLabel("<html>" + I18nManager.getText("dialog.setlanguage.secondintro") + "</html>");
		label2.setAlignmentX(Component.LEFT_ALIGNMENT);
		label2.setHorizontalAlignment(SwingConstants.LEFT);
		midPanel.add(label2);
		midPanel.add(Box.createVerticalStrut(10));

		// built-in languages
		JPanel builtinPanel = new JPanel();
		builtinPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		builtinPanel.setLayout(new BoxLayout(builtinPanel, BoxLayout.X_AXIS));
		builtinPanel.add(new JLabel(I18nManager.getText("dialog.setlanguage.language") + " : "));
		// Language dropdown
		_languageDropDown = new JComboBox(LANGUAGE_NAMES);
		builtinPanel.add(_languageDropDown);
		builtinPanel.add(Box.createHorizontalGlue());
		JButton selectLangButton = new JButton(I18nManager.getText("button.select"));
		selectLangButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLanguage();
			}
		});
		builtinPanel.add(selectLangButton);
		builtinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		midPanel.add(builtinPanel);
		midPanel.add(Box.createVerticalStrut(4));

		// external language file
		JPanel extraPanel = new JPanel();
		extraPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.X_AXIS));
		extraPanel.add(new JLabel(I18nManager.getText("dialog.setlanguage.languagefile") + " : "));
		_langFileBox = new JTextField("some_long_example_file_path.txt");
		extraPanel.add(_langFileBox);
		// browse button
		JButton browseButton = new JButton(I18nManager.getText("button.browse"));
		extraPanel.add(browseButton);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseFile();
			}
		});
		extraPanel.add(Box.createHorizontalStrut(5));
		JButton selectFileButton = new JButton(I18nManager.getText("button.select"));
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLanguageFile();
			}
		});
		extraPanel.add(selectFileButton);
		extraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		midPanel.add(Box.createVerticalStrut(5));
		midPanel.add(extraPanel);
		midPanel.add(Box.createVerticalGlue());
		mainPanel.add(midPanel, BorderLayout.CENTER);

		// Cancel button at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
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
		// Try to use code from config
		String code = Config.getConfigString(Config.KEY_LANGUAGE_CODE);
		int index = getLanguageIndex(code);
		// If it's not present, then use system locale settings
		if (index < 0) {
			Locale locale = Locale.getDefault();
			index = getLanguageIndex(locale.getLanguage() + "_" + locale.getCountry());
			if (index < 0) {
				index = getLanguageIndex(locale.getLanguage());
			}
		}
		// Select appropriate language from dropdown
		if (index >= 0) {
			_languageDropDown.setSelectedIndex(index);
		}
		_startIndex = _languageDropDown.getSelectedIndex();
		// Get language file from config
		String langfile = Config.getConfigString(Config.KEY_LANGUAGE_FILE);
		_langFileBox.setText(langfile==null?"":langfile);
		_dialog.setVisible(true);
	}

	/**
	 * Find the index of the given language code
	 * @param inCode code to look for
	 * @return index if found or -1
	 */
	private static int getLanguageIndex(String inCode)
	{
		int idx = -1;
		if (inCode != null && !inCode.equals("")) {
			for (int i=0; i<LANGUAGE_CODES.length; i++) {
				if (LANGUAGE_CODES[i].equalsIgnoreCase(inCode)) {
					idx = i;
				}
			}
		}
		return idx;
	}

	/**
	 * Set the currently selected language in the Config
	 */
	private void selectLanguage()
	{
		int index = _languageDropDown.getSelectedIndex();
		// If index hasn't changed then don't dismiss dialog
		if (index >= 0 && index != _startIndex)
		{
			String code = LANGUAGE_CODES[index];
			// Set code and langfile in config
			Config.setConfigString(Config.KEY_LANGUAGE_CODE, code);
			Config.setConfigString(Config.KEY_LANGUAGE_FILE, null);
			_dialog.dispose();
			showEndMessage();
		}
	}

	/**
	 * Select the currently selected language file to use for texts
	 */
	private void selectLanguageFile()
	{
		final String oldPath = Config.getConfigString(Config.KEY_LANGUAGE_FILE);
		String filename = _langFileBox.getText();
		// Check there is an entry in the box
		if (filename != null && !filename.equals(""))
		{
			// Check the file exists and is readable
			File textsFile = new File(filename);
			if (!textsFile.exists() || !textsFile.canRead())
			{
				_app.showErrorMessage(getNameKey(), "error.load.noread");
			}
			else if (!languageFileLooksOk(textsFile))
			{
				_app.showErrorMessage(getNameKey(), "error.language.wrongfile");
			}
			else if (oldPath == null || !textsFile.getAbsolutePath().equalsIgnoreCase(oldPath))
			{
				// Set in Config
				Config.setConfigString(Config.KEY_LANGUAGE_FILE, textsFile.getAbsolutePath());
				_dialog.dispose();
				showEndMessage();
			}
		}
		else {
			// if file was previously selected, and now it's blank, then reset Config
			if (oldPath != null && oldPath.length() > 0)
			{
				Config.setConfigString(Config.KEY_LANGUAGE_FILE, null);
				_dialog.dispose();
				showEndMessage();
			}
		}
	}

	/**
	 * Check the given file to see if it looks like a language file
	 * @param inFile File object to load
	 * @return true if file looks ok
	 */
	private static boolean languageFileLooksOk(File inFile)
	{
		boolean ok = false;
		boolean wrong = false;
		BufferedReader reader = null;
		try
		{
			// Read through text file looking for lines with the right start
			reader = new BufferedReader(new FileReader(inFile));
			String currLine = reader.readLine();
			while (currLine != null && !ok && !wrong)
			{
				if (currLine.trim().length() > 0 && currLine.matches("[a-z.]+=.+")) {
					ok = true;
				}
				if (currLine.indexOf('\0', 0) >= 0) {
					wrong = true;
				}
				currLine = reader.readLine();
			}
		}
		catch (Exception e) {} // ignore exceptions, flag remains as set
		finally {
			try {reader.close();} catch (Exception e2) {}
		}
		return ok && !wrong;
	}

	/**
	 * Function activated by the "Browse..." button to select a file
	 */
	private void chooseFile()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(
			new GenericFileFilter("filetype.txt", new String[] {"txt", "text"}));
		chooser.setAcceptAllFileFilterUsed(true);
		// Set start path from currently selected file
		String currPath = _langFileBox.getText();
		if (currPath != null && currPath.length() > 1) {
			chooser.setSelectedFile(new File(currPath));
		}
		if (chooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			_langFileBox.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Show message to remind about saving settings and restarting
	 */
	private void showEndMessage()
	{
		JOptionPane.showMessageDialog(_parentFrame,
			I18nManager.getText("dialog.setlanguage.endmessage"),
			I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
	}
}
