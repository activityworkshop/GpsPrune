package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;

/**
 * Class to show a guide to the shortcut keys
 */
public class ShowKeysScreen extends GenericFunction
{
	/** dialog window */
	private JDialog _dialog = null;
	/** Ok button */
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public ShowKeysScreen(App inApp)
	{
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.showkeys";
	}

	/**
	 * Show the screen
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()));
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		_dialog.setLocationRelativeTo(_parentFrame);
		_dialog.setVisible(true);
		_okButton.requestFocus();
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.keys.intro") + " :");
		introLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		mainPanel.add(introLabel, BorderLayout.NORTH);

		JEditorPane kp = new JEditorPane("text/html", I18nManager.getText("dialog.keys.keylist"));
		kp.setEditable(false);
		kp.setOpaque(false);
		kp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(new JScrollPane(kp), BorderLayout.CENTER);

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
}
