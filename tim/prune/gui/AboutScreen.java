package tim.prune.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.GpsPruner;
import tim.prune.I18nManager;

/**
 * Class to represent the "About" popup window
 */
public class AboutScreen extends JDialog
{

	/**
	 * Constructor
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
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel titleLabel = new JLabel("Prune");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		mainPanel.add(titleLabel);
		JLabel versionLabel = new JLabel(I18nManager.getText("dialog.about.version") + ": " + GpsPruner.VERSION_NUMBER);
		versionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		mainPanel.add(versionLabel);
		JLabel buildLabel = new JLabel(I18nManager.getText("dialog.about.build") + ": " + GpsPruner.BUILD_NUMBER);
		buildLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		mainPanel.add(buildLabel);
		mainPanel.add(new JLabel(" "));
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

		mainPanel.add(descPane);
		mainPanel.add(new JLabel(" "));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		okButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		mainPanel.add(okButton);
		return mainPanel;
	}


	/**
	 * Show window
	 */
	public void show()
	{
		pack();
		// setSize(300,200);
		super.show();
	}
}
