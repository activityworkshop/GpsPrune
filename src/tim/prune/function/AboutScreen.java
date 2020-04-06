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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

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
import tim.prune.GpsPrune;
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
	private JTextArea _aboutTextArea = null;
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
		JLabel titleLabel = new JLabel("GpsPrune");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(titleLabel);
		JLabel versionLabel = new JLabel(I18nManager.getText("dialog.about.version") + ": " + GpsPrune.VERSION_NUMBER);
		versionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(versionLabel);
		JLabel buildLabel = new JLabel(I18nManager.getText("dialog.about.build") + ": " + GpsPrune.BUILD_NUMBER);
		buildLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		aboutPanel.add(buildLabel);
		aboutPanel.add(new JLabel(" "));
		StringBuffer descBuffer = new StringBuffer();
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext1")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext2")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.summarytext3")).append("</p>");
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.languages")).append(" : ")
			.append("afrikaans, \u010de\u0161tina, deutsch, english, espa\u00F1ol, fran\u00E7ais, italiano,<br>" +
				" magyar, nederlands, polski, portugu\u00EAs, rom\u00E2n\u0103, suomi, \u0440\u0443\u0441\u0441\u043a\u0438\u0439 (russian),<br>" +
				" \u4e2d\u6587 (chinese), \u65E5\u672C\u8A9E (japanese), \uD55C\uAD6D\uC5B4/\uC870\uC120\uB9D0 (korean), schwiizerd\u00FC\u00FCtsch</p>");
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
			new JLabel(I18nManager.getText("dialog.about.systeminfo.exiftool") + " : "),
			0, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[1], 1, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.gpsbabel") + " : "),
			0, 4);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[2], 1, 4);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.gnuplot") + " : "),
			0, 5);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[3], 1, 5);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, new JLabel("Xerces : "), 0, 6);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints, _installedLabels[4], 1, 6);
		_tabs.add(I18nManager.getText("dialog.about.systeminfo"), sysInfoPanel);

		// Third pane for credits
		JPanel creditsPanel = new JPanel();
		gridBag = new GridBagLayout();
		creditsPanel.setLayout(gridBag);
		constraints = new GridBagConstraints();
		constraints.weightx = 0.0; constraints.weighty = 0.0;
		constraints.ipady = 3;

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
			new JLabel("Ramon, Miguel, In\u00E9s, Piotr, Petrovsk, Josatoc, Weehal,"),
			1, 3);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(" theYinYeti, Rothermographer, Sam, Rudolph, nazotoko,"),
			1, 4);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(" katpatuka, R\u00E9mi, Marcus, Ali, Javier, Jeroen, prot_d,"),
			1, 5);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(" Gy\u00F6rgy, HooAU, Sergey, P\u00E9ter, serhijdubyk, Peter, Cristian,"),
			1, 6);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(" Roman, Erkki"),
			1, 7);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.translations") + " : "),
			0, 8);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Open Office, Gpsdrive, Babelfish, Leo, Launchpad"),
			1, 8);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.devtools") + " : "),
			0, 9);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Debian Linux, Sun Java, OpenJDK, Eclipse, Svn, Gimp, Inkscape"),
			1, 9);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.othertools") + " : "),
			0, 10);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Openstreetmap, Povray, Exiftool, Gpsbabel, Gnuplot"),
			1, 10);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.thanks") + " : "),
			0, 11);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Friends and loved ones, for encouragement and support"),
			1, 11);
		_tabs.add(I18nManager.getText("dialog.about.credits"), creditsPanel);

		// Read me
		JPanel readmePanel = new JPanel();
		readmePanel.setLayout(new BorderLayout());
		_aboutTextArea = new JTextArea(I18nManager.getText("details.photo.loading"));
		// Set readme text in separate thread so that about screen pops up sooner
		new Thread(new Runnable() {
			public void run() {
				_aboutTextArea.setText(getReadmeText());
			}
		}).start();
		_aboutTextArea.setEditable(false);
		_aboutTextArea.setLineWrap(true); _aboutTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(_aboutTextArea);
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
	 * Helper function to reduce complexity of gui-making code
	 * when adding labels to a GridBagLayout
	 * @param inPanel panel to add to
	 * @param inLayout GridBagLayout object
	 * @param inConstraints GridBagConstraints object
	 * @param inLabel label to add
	 * @param inX grid x
	 * @param inY grid y
	 */
	private static void addToGridBagPanel(JPanel inPanel, GridBagLayout inLayout,
		GridBagConstraints inConstraints, JLabel inLabel, int inX, int inY)
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
		// First, try locally-held readme.txt if available (as it normally should be)
		// Readme file can either be in file system or packed in the same jar as code
		String errorMessage = null;
		String readme = null;
		InputStream in = null;
		try
		{
			// For some reason using ../readme.txt doesn't work, so need absolute path
			in = AboutScreen.class.getResourceAsStream("/tim/prune/readme.txt");
			if (in != null) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				in.close();
				readme = new String(buffer);
			}
		}
		catch (IOException e) {
			errorMessage =  e.getMessage();
		}
		finally {
			try {in.close();} catch (Exception e) {}
		}
		if (readme != null) {return readme;}

		// Locally-held file failed, so try to find gz file installed on system (eg Debian)
		try
		{
			File gzFile = new File("/usr/share/doc/gpsprune/readme.txt.gz");
			if (gzFile.exists())
			{
				// Copy decompressed bytes from gz file into out
				in = new GZIPInputStream(new FileInputStream(gzFile));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[8 * 1024];
				int count = 0;
				do {
					out.write(buffer, 0, count);
					count = in.read(buffer, 0, buffer.length);
				} while (count != -1);
				out.close();
				in.close();
				readme = out.toString();
			}
		}
		catch (IOException e) {
			System.err.println("Exception trying to get readme.gz : " + e.getMessage());
		}
		finally {
			try {in.close();} catch (Exception e) {}
		}
		if (readme != null) {return readme;}
		// Only show first error message if couldn't get readme from gz either
		if (errorMessage != null) {
			System.err.println("Exception trying to get readme: " + errorMessage);
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
		final int[] tools = {ExternalTools.TOOL_EXIFTOOL, ExternalTools.TOOL_GPSBABEL,
			ExternalTools.TOOL_GNUPLOT, ExternalTools.TOOL_XERCES};
		for (int i=0; i<tools.length; i++) {
			_installedLabels[i+1].setText(ExternalTools.isToolInstalled(tools[i])?yesText:noText);
		}
	}
}
