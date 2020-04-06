package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Field;
import tim.prune.function.olc.OlcArea;
import tim.prune.function.olc.OlcDecoder;
import tim.prune.gui.GuiGridLayout;

/**
 * Class to provide the function to parse
 * OpenLocationCodes, or PlusCodes
 */
public class PlusCodeFunction extends GenericFunction
{
	private JDialog _dialog = null;
	private JTextField _codeField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public PlusCodeFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.enterpluscode";
	}

	/**
	 * Begin the function
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
		// MAYBE: Paste clipboard into the edit field
		_codeField.setText("");
		enableOK();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.pluscode.desc")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);
		_codeField = new JTextField("", 12);
		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				enableOK();
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(MouseEvent inE) {
				enableOK();
			}
		};
		_codeField.addKeyListener(keyListener);
		_codeField.addMouseListener(mouseListener);
		JLabel codeLabel = new JLabel(I18nManager.getText("dialog.pluscode.code"));
		codeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(codeLabel);
		grid.add(_codeField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (_okButton.isEnabled()) {finish();}
			}
		};
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		_codeField.addActionListener(okListener);
		buttonPanel.add(_okButton);
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
	 * Enable or disable the OK button based on the contents of the text field
	 */
	private void enableOK()
	{
		String text = _codeField.getText();
		_okButton.setEnabled(text != null && text.length() > 7
			&& text.indexOf(' ') < 0 && text.indexOf(',') < 0);
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		OlcArea area = OlcDecoder.decode(_codeField.getText());

		if (area == null)
		{
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("dialog.pluscode.nothingfound"),
				I18nManager.getText(getNameKey()), JOptionPane.ERROR_MESSAGE);
		}
		else if (loadTrack(area))
		{
			_dialog.dispose();
		}
	}

	/**
	 * Load the generated points from the given area
	 * @param inArea rectangular area
	 * @return true on success
	 */
	private boolean loadTrack(OlcArea inArea)
	{
		if (inArea == null)
		{
			return false;
		}

		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.WAYPT_NAME};
		_app.autoAppendNextFile();

		if (inArea.minLat == inArea.maxLat && inArea.minLon == inArea.maxLon)
		{
			String[][] pointData = new String[1][];
			pointData[0] = new String[3]; // lat, long, name
			pointData[0][0] = "" + inArea.minLat;
			pointData[0][1] = "" + inArea.minLon;
			pointData[0][2] = _codeField.getText();
			_app.informDataLoaded(fields, pointData, null, null);
		}
		else
		{
			String[][] pointData = new String[6][];
			for (int i=0; i<5; i++)
			{
				pointData[i] = new String[3]; // lat, long, name
				pointData[i][0] = "" + ((i%4==0 || i==3) ? inArea.minLat : inArea.maxLat);
				pointData[i][1] = "" + ((i%4==0 || i==1) ? inArea.minLon : inArea.maxLon);
				pointData[i][2] = null;
			}
			// Middle point with name
			pointData[5] = new String[3]; // lat, long, name
			pointData[5][0] = "" + ((inArea.minLat + inArea.maxLat) / 2.0);
			pointData[5][1] = "" + ((inArea.minLon + inArea.maxLon) / 2.0);
			pointData[5][2] = _codeField.getText();
			_app.informDataLoaded(fields, pointData, null, null);
		}
		return true;
	}
}
