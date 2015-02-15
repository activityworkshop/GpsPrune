package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Class to provide the function to save the config settings
 */
public class SaveConfig extends GenericFunction
{
	private JDialog _dialog = null;

	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public SaveConfig(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.saveconfig";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Make new dialog window (don't reuse it)
		_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
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
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		JLabel descLabel = new JLabel(I18nManager.getText("dialog.saveconfig.desc"));
		dialogPanel.add(descLabel, BorderLayout.NORTH);

		// Grid panel in centre
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2, 15, 2));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		Properties conf = Config.getAllConfig();
		Enumeration<Object> keys = conf.keys();
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement().toString();
			String keyLabel = I18nManager.getText("dialog.saveconfig." + key);
			if (!keyLabel.equals("dialog.saveconfig." + key))
			{
				mainPanel.add(new JLabel(keyLabel));
				String val = conf.getProperty(key);
				if (Config.isKeyBoolean(key)) {
					val = Config.getConfigBoolean(key)?I18nManager.getText("dialog.about.yes"):I18nManager.getText("dialog.about.no");
				}
				else if (val != null && val.length() > 30) {
					val = val.substring(0, 30) + " ...";
				}
				mainPanel.add(new JLabel(val));
			}
		}
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
				_dialog = null;
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		File configFile = Config.getConfigFile();
		if (configFile == null) {configFile = new File(".pruneconfig");}
		JFileChooser chooser = new JFileChooser(configFile.getAbsoluteFile().getParent());
		chooser.setSelectedFile(configFile);
		int response = chooser.showSaveDialog(_parentFrame);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File saveFile = chooser.getSelectedFile();
			FileOutputStream outStream = null;
			try
			{
				outStream = new FileOutputStream(saveFile);
				Config.getAllConfig().store(outStream, "Prune config file");
			}
			catch (IOException ioe) {
				_app.showErrorMessageNoLookup(getNameKey(),
					I18nManager.getText("error.save.failed") + " : " + ioe.getMessage());
			}
			finally {
				try {outStream.close();} catch (Exception e) {}
			}
		}
		_dialog.dispose();
		_dialog = null;
	}
}
