package tim.prune.undo;

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

/**
 * Class to manage the selection of actions to undo or redo
 */
public abstract class UndoRedoManager
{
	private final App _app;
	private final JFrame _parentFrame;
	private JDialog _dialog = null;
	private JList<String> _actionList = null;
	private final String _tokenPrefix;


	/**
	 * Constructor
	 * @param inApp App object
	 * @param inFrame parent frame
	 * @param inUndo true for undo, false for redo
	 */
	protected UndoRedoManager(App inApp, JFrame inFrame, boolean inUndo)
	{
		_app = inApp;
		_parentFrame = inFrame;
		_tokenPrefix = "dialog." + (inUndo ? "undo" : "redo") + ".";
	}

	/**
	 * Show the dialog to select which actions to undo or redo
	 */
	public void show(UndoStack inStack)
	{
		_dialog = new JDialog(_parentFrame, getText("title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(3, 3));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(new JLabel(getText("pretext") + ":"), BorderLayout.NORTH);

		String[] actions = inStack.getDescriptions().toArray(new String[0]);
		_actionList = new JList<String>(actions);
		_actionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		_actionList.setSelectedIndex(0);
		_actionList.addListSelectionListener(e -> {
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
		okButton.addActionListener(e -> {
			undoRedoActions(_app, _actionList.getMaxSelectionIndex() + 1);
			_dialog.dispose();
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		_dialog.getContentPane().add(mainPanel);
		_dialog.pack();
		_dialog.setVisible(true);
	}

	/** To be overridden by subclasses */
	protected abstract void undoRedoActions(App inApp, int inNumActions);

	/** @return text for the given key */
	private String getText(String inKey) {
		return I18nManager.getText(_tokenPrefix + inKey);
	}
}
