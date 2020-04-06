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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.load.TextFileLoader;

/**
 * Class to provide the function to paste a list of coordinates
 * and create points for them as if they were loaded from a text file
 */
public class PasteCoordinateList extends GenericFunction
{
	private JDialog _dialog = null;
	private JTextArea _coordArea = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public PasteCoordinateList(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.pastecoordinatelist";
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
		// MAYBE: Paste clipboard into the edit area
		_coordArea.setText("");
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
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.pastecoordinatelist.desc")), BorderLayout.NORTH);
		_coordArea = new JTextArea(8, 35);
		_coordArea.setLineWrap(true);
		_coordArea.setWrapStyleWord(true);
		JScrollPane coordsPane = new JScrollPane(_coordArea);
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
		_coordArea.addKeyListener(keyListener);
		_coordArea.addMouseListener(mouseListener);
		dialogPanel.add(coordsPane, BorderLayout.CENTER);
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
		String text = _coordArea.getText();
		_okButton.setEnabled(text != null && text.length() > 6
			&& (text.indexOf(' ') >= 0 || text.indexOf(',') >= 0));
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		new TextFileLoader(_app, _parentFrame).loadText(_coordArea.getText());
		_dialog.dispose();
	}
}
