package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.GpsPruner;
import tim.prune.I18nManager;
import tim.prune.threedee.WindowFactory;

/**
 * Class to represent the "About" popup window
 */
public class AboutScreen extends GenericFunction
{
	private JDialog _dialog = null;
	private JTabbedPane _tabs = null;
	private JButton _okButton = null;
	/** Labels for whether tools installed or not */
	private JLabel[] _installedLabels = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public AboutScreen(App inApp)
	{
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey()
	{
		return "function.about";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		_tabs = new JTabbedPane();
		mainPanel.add(_tabs, BorderLayout.CENTER);

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel titleLabel = new JLabel("Prune");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(titleLabel);
		JLabel versionLabel = new JLabel(I18nManager.getText("dialog.about.version") + ": " + GpsPruner.VERSION_NUMBER);
		versionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(versionLabel);
		JLabel buildLabel = new JLabel(I18nManager.getText("dialog.about.build") + ": " + GpsPruner.BUILD_NUMBER);
		buildLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(buildLabel);
		aboutPanel.add(new JLabel(" "));
		StringBuffer descBuffer = new StringBuffer();
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext1")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext2")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext3")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.languages")).append(" : ")
			.append("deutsch, english, español, français, italiano, polski,<br>" +
				"schwiizerdüütsch, português, bahasa indonesia, română").append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.translatedby")).append("</p>");
		JEditorPane descPane = new JEditorPane("text/html", descBuffer.toString());
		descPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		descPane.setEditable(false);
		descPane.setOpaque(false);
		descPane.setAlignmentX(JEditorPane.CENTER_ALIGNMENT);

		aboutPanel.add(descPane);
		aboutPanel.add(new JLabel(" "));
		_tabs.add(I18nManager.getText("function.about"), aboutPanel);

		// Second pane for system info
		JPanel sysInfoPanel = new JPanel();
		GridBagLayout gridBag = new GridBagLayout();
		sysInfoPanel.setLayout(gridBag);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 0.0; constraints.weighty = 0.0;
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.os") + " : "),
			0, 0);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(System.getProperty("os.name")),
			1, 0);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.java") + " : "),
			0, 1);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(System.getProperty("java.runtime.version")),
			1, 1);
		// Create install labels to be populated later
		final int NUM_INSTALL_CHECKS = 5;
		_installedLabels = new JLabel[NUM_INSTALL_CHECKS];
		for (int i=0; i<NUM_INSTALL_CHECKS; i++) {
			_installedLabels[i] = new JLabel("...");
		}
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.java3d") + " : "),
			0, 2);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[0], 1, 2);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.povray") + " : "),
			0, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[1], 1, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.exiftool") + " : "),
			0, 4);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[2], 1, 4);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.gpsbabel") + " : "),
			0, 5);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[3], 1, 5);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.gnuplot") + " : "),
			0, 6);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[4], 1, 6);
		_tabs.add(I18nManager.getText("dialog.about.systeminfo"), sysInfoPanel);

		// Third pane for credits
		JPanel creditsPanel = new JPanel();
		gridBag = new GridBagLayout();
		creditsPanel.setLayout(gridBag);
		constraints = new GridBagConstraints();
		constraints.weightx = 0.0; constraints.weighty = 0.0;

		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.code") + " : "),
			0, 0);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("activityworkshop.net"),
			1, 0);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.exifcode") + " : "),
			0, 1);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Drew Noakes"),
			1, 1);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.icons") + " : "),
			0, 2);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Eclipse"),
			1, 2);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.translators") + " : "),
			0, 3);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Ramon, Miguel, Inés, Piotr, Petrovsk, Josatoc, Weehal,"),
			1, 3);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(" theYinYeti, Rothermographer"),
			1, 4);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.translations") + " : "),
			0, 5);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Open Office, Gpsdrive, Babelfish, Leo, Launchpad"),
			1, 5);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.devtools") + " : "),
			0, 6);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Mandriva Linux, Sun Java, Eclipse, Svn, Gimp"),
			1, 6);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.othertools") + " : "),
			0, 7);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Kate, Povray, Exiftool, Inkscape, Google Earth, Gpsbabel, Gnuplot"),
			1, 7);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.thanks") + " : "),
			0, 8);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Friends and loved ones, for encouragement and support"),
			1, 8);
		_tabs.add(I18nManager.getText("dialog.about.credits"), creditsPanel);

		// Read me
		JPanel readmePanel = new JPanel();
		readmePanel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea(getReadmeText());
		textArea.setEditable(false);
		textArea.setLineWrap(true); textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(600, 130));
		readmePanel.add(scrollPane, BorderLayout.CENTER);
		_tabs.add(I18nManager.getText("dialog.about.readme"), readmePanel);

		// OK button at the bottom
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		_okButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		okPanel.add(_okButton);
		mainPanel.add(okPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * Helper function to reduce complexity of gui making code
	 * when adding labels to a GridBagLayout
	 * @param inPanel panel to add to
	 * @param inLayout GridBagLayout object
	 * @param inConstraints GridBagConstraints object
	 * @param inLabel label to add
	 * @param inX grid x
	 * @param inY grid y
	 */
	private static void addToGridBagPanel(JPanel inPanel, GridBagLayout inLayout, GridBagConstraints inConstraints,
		JLabel inLabel, int inX, int inY)
	{
		// set x and y in constraints
		inConstraints.gridx = inX;
		inConstraints.gridy = inY;
		// set anchor
		inConstraints.anchor = (inX == 0?GridBagConstraints.EAST:GridBagConstraints.WEST);
		// set constraints to label
		inLayout.setConstraints(inLabel, inConstraints);
		// add label to panel
		inPanel.add(inLabel);
	}

	/**
	 * @return text from the readme file
	 */
	private String getReadmeText()
	{
		try
		{
			// For some reason using ../readme.txt doesn't work, so need absolute path
			InputStream in = AboutScreen.class.getResourceAsStream("/tim/prune/readme.txt");
			if (in != null) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				return new String(buffer);
			}
		}
		catch (java.io.IOException e) {
			System.err.println("Exception trying to get readme : " + e.getMessage());
		}
		return I18nManager.getText("error.readme.notfound");
	}

	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()));
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		_tabs.setSelectedIndex(0);
		checkInstalls();
		_dialog.setVisible(true);
		_okButton.requestFocus();
	}

	/**
	 * Check the installed components and set the label texts accordingly
	 */
	private void checkInstalls()
	{
		String yesText = I18nManager.getText("dialog.about.yes");
		String noText = I18nManager.getText("dialog.about.no");
		_installedLabels[0].setText(WindowFactory.isJava3dEnabled()?yesText:noText);
		_installedLabels[1].setText(ExternalTools.isPovrayInstalled()?yesText:noText);
		_installedLabels[2].setText(ExternalTools.isExiftoolInstalled()?yesText:noText);
		_installedLabels[3].setText(ExternalTools.isGpsbabelInstalled()?yesText:noText);
		_installedLabels[4].setText(ExternalTools.isGnuplotInstalled()?yesText:noText);
	}
}
