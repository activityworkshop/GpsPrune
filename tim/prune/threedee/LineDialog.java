package tim.prune.threedee;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;

/**
 * Class to show a dialog displaying the line coordinates
 * for a 3d view (either java3d or povray)
 */
public class LineDialog
{
	private JDialog _dialog = null;
	private JFrame _parent = null;
	private double[] _latLines = null;
	private double[] _lonLines = null;


	/**
	 * Constructor giving parent frame, latitude and longitude lines
	 * @param inParent parent frame for dialog
	 * @param inLatLines latitude lines as doubles
	 * @param inLonLines longitude lines as doubles
	 */
	public LineDialog(JFrame inParent, double[] inLatLines, double[] inLonLines)
	{
		_parent = inParent;
		_latLines = inLatLines;
		_lonLines = inLonLines;
	}


	/**
	 * Show the dialog with the lines
	 */
	public void showDialog()
	{
		_dialog = new JDialog(_parent, I18nManager.getText("dialog.3dlines.title"), true);
		_dialog.setLocationRelativeTo(_parent);
		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		_dialog.setVisible(true);
	}


	/**
	 * @return dialog components
	 */
	private JPanel makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		StringBuffer descBuffer = new StringBuffer();
		final int numLatLines = (_latLines == null?0:_latLines.length);
		final int numLonLines = (_lonLines == null?0:_lonLines.length);
		if (numLatLines == 0 && numLonLines == 0)
		{
			descBuffer.append("<p>").append(I18nManager.getText("dialog.3dlines.empty")).append("</p>");
		}
		else
		{
			descBuffer.append("<p>").append(I18nManager.getText("dialog.3dlines.intro")).append(":</p>");
			descBuffer.append("<p>").append(I18nManager.getText("fieldname.latitude")).append("<ul>");
			Latitude lat = null;
			for (int i=0; i<numLatLines; i++)
			{
				lat = new Latitude(_latLines[i], Latitude.FORMAT_DEG);
				descBuffer.append("<li>").append(lat.output(Latitude.FORMAT_DEG_WHOLE_MIN)).append("</li>");
			}
			descBuffer.append("</ul></p>");
			descBuffer.append("<p>").append(I18nManager.getText("fieldname.longitude")).append("<ul>");
			Longitude lon = null;
			for (int i=0; i<numLonLines; i++)
			{
				lon = new Longitude(_lonLines[i], Longitude.FORMAT_DEG);
				descBuffer.append("<li>").append(lon.output(Longitude.FORMAT_DEG_WHOLE_MIN)).append("</li>");
			}
			descBuffer.append("</ul></p>");
		}
		JEditorPane descPane = new JEditorPane("text/html", descBuffer.toString());
		descPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		descPane.setEditable(false);
		descPane.setOpaque(false);
		panel.add(descPane, BorderLayout.CENTER);
		// ok button
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
				_dialog = null;
			}
		});
		buttonPanel.add(okButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}
}
