package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.gui.WaypointNameMatcher;

/**
 * Class to provide the function to find a waypoint by name
 */
public class FindWaypoint extends GenericFunction
{
	private WaypointNameMatcher _nameMatcher = null;
	private JDialog _dialog = null;
	private JTextField _searchField = null;
	private JList<String> _pointList = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public FindWaypoint(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.findwaypoint";
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
		// Initialise waypoint list from track
		_nameMatcher.init(_app.getTrackInfo().getTrack());
		// Show dialog
		_searchField.setText("");
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(new JLabel(I18nManager.getText("dialog.findwaypoint.intro")));
		JPanel subPanel = new JPanel();
		subPanel.add(new JLabel(I18nManager.getText("dialog.findwaypoint.search")));
		_searchField = new JTextField(12);
		_searchField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				_nameMatcher.findMatches(_searchField.getText());
				if (_nameMatcher.getSize() == 0 || _nameMatcher.getSize() < _pointList.getSelectedIndex())
					_okButton.setEnabled(false);
				else if (_pointList.getSelectedIndex() >= 0)
					_okButton.setEnabled(true);
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		});
		subPanel.add(_searchField);
		topPanel.add(subPanel);
		dialogPanel.add(topPanel, BorderLayout.NORTH);

		// middle panel with list
		_nameMatcher = new WaypointNameMatcher();
		_pointList = new JList<String>(_nameMatcher);
		_pointList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) _okButton.setEnabled(true);
			}});
		JScrollPane listPane = new JScrollPane(_pointList);
		dialogPanel.add(listPane, BorderLayout.CENTER);

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
	 * Finish the dialog when OK pressed
	 * Select the point and close the dialog
	 */
	private void finish()
	{
		DataPoint selectedPoint = _nameMatcher.getWaypoint(_pointList.getSelectedIndex());
		_app.getTrackInfo().selectPoint(selectedPoint);
		_dialog.dispose();
	}
}
