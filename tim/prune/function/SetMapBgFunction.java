package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.gui.map.MapSourceLibrary;

/**
 * Function to set the tile server for the map backgrounds
 */
public class SetMapBgFunction extends GenericFunction
{
	private JDialog _dialog = null;
	private JList _list = null;
	private MapSourceListModel _listModel = null;
	private String _initialSource = null;
	private JButton _okButton = null;
	private JButton _deleteButton = null;
	// Add dialog
	private AddMapSourceDialog _addDialog = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetMapBgFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.setmapbg";
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
		initValues();
		enableButtons();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(8, 8));
		// intro label
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.setmapbg.intro"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(5, 4, 1, 4));
		dialogPanel.add(introLabel, BorderLayout.NORTH);
		// list box
		_listModel = new MapSourceListModel();
		_list = new JList(_listModel);
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dialogPanel.add(new JScrollPane(_list), BorderLayout.CENTER);
		_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				enableButtons();
			}
		});
		_list.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		});
		_list.setPreferredSize(new Dimension(200, 200));
		// button panel on right
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		JButton addButton = new JButton(I18nManager.getText("button.addnew"));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewSource();
			}
		});
		rightPanel.add(addButton);
		_deleteButton = new JButton(I18nManager.getText("button.delete"));
		_deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteMapSource(_list.getSelectedIndex());
			}
		});
		rightPanel.add(_deleteButton);
		dialogPanel.add(rightPanel, BorderLayout.EAST);

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
		return dialogPanel;
	}


	/**
	 * Get the initial values from the Config and set gui values accordingly
	 */
	private void initValues()
	{
		updateList();
		// Get selected value from config
		int currSource = Config.getConfigInt(Config.KEY_MAPSOURCE_INDEX);
		if (currSource < 0 || currSource >= _listModel.getSize()) {
			currSource = 0;
		}
		_initialSource = _listModel.getSource(currSource).getSiteStrings();
		_list.setSelectedIndex(currSource);
	}

	/**
	 * @return index of selected server, or -1 if none
	 */
	private int getSelectedServer()
	{
		return _list.getSelectedIndex();
	}

	/**
	 * Enable or disable the buttons according to the selection
	 */
	private void enableButtons()
	{
		int serverNum = getSelectedServer();
		_okButton.setEnabled(serverNum >= 0 && serverNum < _listModel.getSize()
			&& !_listModel.getSource(serverNum).getSiteStrings().equals(_initialSource));
		_deleteButton.setEnabled(serverNum >= MapSourceLibrary.getNumFixedSources()
			&& serverNum < _listModel.getSize());
	}

	/**
	 * Start the dialog to add a new map source to the list
	 */
	private void addNewSource()
	{
		if (_addDialog == null) {
			_addDialog = new AddMapSourceDialog(_dialog, this);
		}
		_addDialog.showDialog();
	}

	/**
	 * Delete the selected map source so it is no longer available
	 * @param inIndex index within list
	 */
	private void deleteMapSource(int inIndex)
	{
		MapSourceLibrary.deleteSource(inIndex);
		updateList();
		enableButtons();
	}

	/**
	 * use the library to update the current list, after add or delete
	 */
	public void updateList()
	{
		_listModel.fireChanged();
		Config.setConfigString(Config.KEY_MAPSOURCE_LIST, MapSourceLibrary.getConfigString());
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		int serverNum = getSelectedServer();
		if (serverNum < 0) {serverNum = 0;}
		Config.setConfigInt(Config.KEY_MAPSOURCE_INDEX, serverNum);
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}
}
