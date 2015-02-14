package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.Field;

/**
 * Class to provide the function to add an altitude offset to a track range
 */
public class AddAltitudeOffset extends GenericFunction
{
	private JDialog _dialog = null;
	private JLabel _descLabel = null;
	private JTextField _editField = null;
	private JButton _okButton = null;
	private Altitude.Format _altFormat = Altitude.Format.NO_FORMAT;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public AddAltitudeOffset(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.addaltitudeoffset";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		int selStart = _app.getTrackInfo().getSelection().getStart();
		int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (!_app.getTrackInfo().getTrack().hasData(Field.ALTITUDE, selStart, selEnd))
		{
			_app.showErrorMessage(getNameKey(), "dialog.addaltitude.noaltitudes");
			return;
		}
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Set label according to altitude units
		setLabelText(selStart, selEnd);
		// Select the contents of the edit field
		_editField.selectAll();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		_descLabel = new JLabel(I18nManager.getText("dialog.addaltitude.desc") + " (ft)");
		// Note, this label will be reset at each run
		mainPanel.add(_descLabel);
		_editField = new JTextField("0", 6);
		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent arg0) {
				_okButton.setEnabled(Math.abs(getOffset()) > 0.001);
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent arg0) {
				_okButton.setEnabled(Math.abs(getOffset()) > 0.001);
			};
		};
		_editField.addKeyListener(keyListener);
		_editField.addMouseListener(mouseListener);
		mainPanel.add(_editField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		_editField.addActionListener(okListener);
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
	 * Get the value of the altitude to add
	 * @return entered altitude offset as numeric value
	 */
	private double getOffset()
	{
		double ans = 0.0;
		try {
			ans = Double.parseDouble(_editField.getText());
		}
		catch (Exception e) {}
		return ans;
	}

	/**
	 * Set the label text according to the current units
	 * @param inStart start index of selection
	 * @param inEnd end index of selection
	 */
	private void setLabelText(int inStart, int inEnd)
	{
		_altFormat = Altitude.Format.NO_FORMAT;
		for (int i=inStart; i<=inEnd && _altFormat==Altitude.Format.NO_FORMAT; i++)
		{
			Altitude alt = _app.getTrackInfo().getTrack().getPoint(i).getAltitude();
			if (alt != null) {
				_altFormat = alt.getFormat();
			}
		}
		if (_altFormat==Altitude.Format.NO_FORMAT) {
			_altFormat = Altitude.Format.METRES;
		}
		final String unitKey = (_altFormat==Altitude.Format.METRES?"units.metres.short":"units.feet.short");
		_descLabel.setText(I18nManager.getText("dialog.addaltitude.desc") + " (" + I18nManager.getText(unitKey) + ")");
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		// Pass information back to App to complete function
		_app.finishAddAltitudeOffset(_editField.getText(), _altFormat);
		_dialog.dispose();
	}
}
