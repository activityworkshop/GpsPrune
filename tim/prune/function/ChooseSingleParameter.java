package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.gui.WholeNumberField;

/**
 * First step of functions which just require a single numeric
 * parameter in order to run
 */
public class ChooseSingleParameter extends GenericFunction
{
	/** Parent function which needs this parameter */
	private SingleNumericParameterFunction _parent = null;
	/** dialog */
	private JDialog _dialog = null;
	/** label which might need to be changed */
	private JLabel _descLabel = null;
	/** entry field */
	private WholeNumberField _numberField = null;
	/** ok button */
	private JButton _okButton = null;


	/** Constructor */
	public ChooseSingleParameter(App inApp, SingleNumericParameterFunction inFunction)
	{
		super(inApp);
		_parent = inFunction;
	}

	@Override
	public String getNameKey() {
		return _parent.getNameKey();
	}

	@Override
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(_parent.getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// refresh and show the dialog
		_descLabel.setText(I18nManager.getText(_parent.getDescriptionKey()));
		int param = _parent.getCurrentParamValue();
		if (param > 0) {
			_numberField.setValue(param);
		}
		else {
			_numberField.setText("");
		}
		_dialog.setVisible(true);
		enableOkButton();
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		// label
		_descLabel = new JLabel(I18nManager.getText(_parent.getDescriptionKey()));
		dialogPanel.add(_descLabel, BorderLayout.NORTH);
		// Centre panel with number entry field
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BorderLayout(8, 8));
		_numberField = new WholeNumberField(4);
		centrePanel.add(_numberField, BorderLayout.NORTH);
		dialogPanel.add(centrePanel, BorderLayout.CENTER);

		// Listener to enable/disable ok button
		_numberField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enableOkButton();
			}
		});
		_numberField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent inE)
			{
				int eCode = inE.getKeyCode();
				if (eCode == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
				else if (eCode == KeyEvent.VK_ENTER) {finish();}
				super.keyReleased(inE);
			}
		});

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
	 * Enable or disable the OK button as appropriate
	 */
	private void enableOkButton()
	{
		_okButton.setEnabled(_numberField.getValue() >= _parent.getMinAllowedValue()
			&& _numberField.getValue() <= _parent.getMaxAllowedValue());
	}

	/**
	 * The OK button (or Enter) has been pressed
	 */
	private void finish()
	{
		if (_numberField.getValue() >= _parent.getMinAllowedValue()
			&& _numberField.getValue() <= _parent.getMaxAllowedValue())
		{
			_parent.completeFunction(_numberField.getValue());
			_dialog.dispose();
		}
	}
}
