package tim.prune.gui;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.undo.UndoStack;

/**
 * Class to manage the selection of actions to undo
 */
public class UndoManager
{
	private final App _app;
	private final JFrame _parentFrame;
	private JDialog _dialog = null;
	private JList<String> _actionList = null;


	/**
	 * Constructor
	 * @param inApp App object
	 * @param inFrame parent frame
	 */
	public UndoManager(App inApp, JFrame inFrame)
	{
		_app = inApp;
		_parentFrame = inFrame;
	}

	/**
	 * Show the dialog to select which actions to undo
	 */
	public void show()
	{
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.undo.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(3, 3));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		UndoStack undoStack = _app.getUndoStack();
		mainPanel.add(new JLabel(I18nManager.getText("dialog.undo.pretext")), BorderLayout.NORTH);

		String[] undoActions = new String[undoStack.size()];
		for (int i=0; i<undoStack.size(); i++) {
			undoActions[i] = undoStack.getOperationAt(undoStack.size()-1-i).getDescription();
		}
		_actionList = new JList<String>(undoActions);
		_actionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		_actionList.setSelectedIndex(0);
		_actionList.addListSelectionListener((e) -> {
			if (_actionList.getMinSelectionIndex() > 0) {
				_actionList.setSelectionInterval(0, _actionList.getMaxSelectionIndex());
			}
		});
		_actionList.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		});

		mainPanel.add(new JScrollPane(_actionList), BorderLayout.CENTER);
		// Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener((e) -> {
			_app.undoActions(_actionList.getMaxSelectionIndex() + 1);
			_dialog.dispose();
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener((e) -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		_dialog.getContentPane().add(mainPanel);
		_dialog.pack();
		_dialog.setVisible(true);
	}
}
