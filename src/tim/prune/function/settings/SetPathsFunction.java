package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Function to set the paths for the external programs (eg gnuplot)
 */
public class SetPathsFunction extends GenericFunction
{
	/** dialog object, cached */
	private JDialog _dialog = null;
	/** edit boxes */
	private JTextField[] _editFields = null;
	/** yes/no labels */
	private JLabel[] _installedLabels = null;
	/** Config keys */
	private static final String[] CONFIG_KEYS = {Config.KEY_GPSBABEL_PATH, Config.KEY_GNUPLOT_PATH, Config.KEY_EXIFTOOL_PATH};
	/** Label keys */
	private static final String[] LABEL_KEYS = {"gpsbabel", "gnuplot", "exiftool"};
	/** Number of entries */
	private static final int NUM_KEYS = CONFIG_KEYS.length;

	/**
	 * Constructor from superclass
	 * @param inApp app object
	 */
	public SetPathsFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * @return key for function name
	 */
	public String getNameKey()
	{
		return "function.setpaths";
	}

	/**
	 * Show the dialog
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
			_dialog.pack();
		}
		checkPaths();
		// Show dialog
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components
	 * @return panel containing gui elements
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.setpaths.intro")), BorderLayout.NORTH);

		// Main panel with edit boxes for paths
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(NUM_KEYS+1, 3, 10, 1));
		mainPanel.add(new JLabel(" "));
		mainPanel.add(new JLabel(" "));
		mainPanel.add(new JLabel(I18nManager.getText("dialog.setpaths.found")));
		_editFields = new JTextField[NUM_KEYS];
		_installedLabels = new JLabel[NUM_KEYS];
		for (int i=0; i<NUM_KEYS; i++)
		{
			JLabel label = new JLabel(I18nManager.getText("dialog.paths.prune." + LABEL_KEYS[i] + "path"));
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			mainPanel.add(label);
			String configVal = Config.getConfigString(CONFIG_KEYS[i]);
			if (configVal == null) {configVal = "";}
			_editFields[i] = new JTextField(configVal);
			mainPanel.add(_editFields[i]);
			_installedLabels[i] = new JLabel("...");
			mainPanel.add(_installedLabels[i]);
		}
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton checkButton = new JButton(I18nManager.getText("button.check"));
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				checkPaths();
			}
		});
		buttonPanel.add(checkButton);
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		okButton.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Check all the programs to see if they're installed
	 */
	private void checkPaths()
	{
		String yesText = I18nManager.getText("dialog.about.yes");
		String noText = I18nManager.getText("dialog.about.no");
		for (int i=0; i<NUM_KEYS; i++)
		{
			String command = _editFields[i].getText();
			_installedLabels[i].setText("   " + (ExternalTools.isToolInstalled(i, command)?yesText:noText));
		}
	}

	/**
	 * Set the given paths in the configuration and exit
	 */
	private void finish()
	{
		for (int i=0; i<NUM_KEYS; i++)
		{
			String val = _editFields[i].getText();
			// TODO: Check path values?
			Config.setConfigString(CONFIG_KEYS[i], val);
		}
		_dialog.dispose();
	}
}
