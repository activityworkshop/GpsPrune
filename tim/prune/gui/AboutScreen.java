package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tim.prune.ExternalTools;
import tim.prune.GpsPruner;
import tim.prune.I18nManager;
import tim.prune.threedee.WindowFactory;

/**
 * Class to represent the "About" popup window
 */
public class AboutScreen extends JDialog
{
	JButton _okButton = null;

	/**
	 * Constructor
	 * @param inParent parent frame
	 */
	public AboutScreen(JFrame inParent)
	{
		super(inParent, I18nManager.getText("dialog.about.title"));
		getContentPane().add(makeContents());
	}


	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JTabbedPane tabPane = new JTabbedPane();
		mainPanel.add(tabPane, BorderLayout.CENTER);

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
		descBuffer.append("<p>").append(I18nManager.getText("dialog.about.translatedby")).append("</p>");
		JEditorPane descPane = new JEditorPane("text/html", descBuffer.toString());
		descPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		descPane.setEditable(false);
		descPane.setOpaque(false);
		descPane.setAlignmentX(JEditorPane.CENTER_ALIGNMENT);

		aboutPanel.add(descPane);
		aboutPanel.add(new JLabel(" "));
		tabPane.add(I18nManager.getText("dialog.about.title"), aboutPanel);

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
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.java3d") + " : "),
			0, 2);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText(WindowFactory.isJava3dEnabled()?"dialog.about.yes":"dialog.about.no")),
			1, 2);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.povray") + " : "),
			0, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText(ExternalTools.isPovrayInstalled()?"dialog.about.yes":"dialog.about.no")),
			1, 3);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.systeminfo.exiftool") + " : "),
			0, 4);
		addToGridBagPanel(sysInfoPanel, gridBag, constraints,
			new JLabel(I18nManager.getText(ExternalTools.isExiftoolInstalled()?"dialog.about.yes":"dialog.about.no")),
			1, 4);
		tabPane.add(I18nManager.getText("dialog.about.systeminfo"), sysInfoPanel);

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
			new JLabel("Ramon, Miguel, Inés, Piotr"),
			1, 3);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.translations") + " : "),
			0, 4);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Open Office, Gpsdrive, Babelfish, Leo, Launchpad"),
			1, 4);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.devtools") + " : "),
			0, 5);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Mandriva Linux, Sun Java, Eclipse, Svn, Gimp"),
			1, 5);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.othertools") + " : "),
			0, 6);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Garble, Kate, Povray, Exiftool, Inkscape, Google Earth"),
			1, 6);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel(I18nManager.getText("dialog.about.credits.thanks") + " : "),
			0, 7);
		addToGridBagPanel(creditsPanel, gridBag, constraints,
			new JLabel("Friends and loved ones, for encouragement and support"),
			1, 7);
		tabPane.add(I18nManager.getText("dialog.about.credits"), creditsPanel);

		// OK button at the bottom
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		_okButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {dispose();}
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
	 * Show window
	 */
	public void show()
	{
		pack();
		// setSize(300,200);
		super.show();
		_okButton.requestFocus();
	}
}
