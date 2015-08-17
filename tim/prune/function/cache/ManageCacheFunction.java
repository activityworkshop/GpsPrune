package tim.prune.function.cache;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.gui.WholeNumberField;

/**
 * Function class to manage the tile cache on local disk
 */
public class ManageCacheFunction extends GenericFunction implements Runnable
{
	private JDialog _dialog = null;
	private CardLayout _cards = null;
	private JPanel _cardPanel = null;
	private JProgressBar _progressBar = null;
	private File _cacheDir = null;
	private TileCacheModel _model = null;
	private JTable _setsTable = null;
	private JButton _deleteSetButton = null;
	private JLabel _tileSetLabel = null, _zoomLabel = null;
	private JLabel _ageLabel = null;
	private JRadioButton _deleteAllRadio = null;
	private WholeNumberField _daysField = null;

	private static TileFilter _TILEFILTER = new TileFilter();


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public ManageCacheFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.managetilecache";
	}

	/**
	 * Show the dialog to start
	 */
	public void begin()
	{
		// First check if directory even exists
		_cacheDir = null;
		String path = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (path != null && !path.equals("")) {
			_cacheDir = new File(path);
		}
		if (_cacheDir == null || !_cacheDir.exists() || !_cacheDir.isDirectory())
		{
			_app.showErrorMessage(getNameKey(), "error.cache.notthere");
			return;
		}

		// Build the dialog if it hasn't already been built
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true); // modal
			_dialog.setLocationRelativeTo(_parentFrame);
			_cardPanel = makeContents();
			_dialog.getContentPane().add(_cardPanel);
			_dialog.pack();
		}
		// Start a new thread to build the model
		new Thread(this).start();
		// Show the first panel of the dialog including progress bar
		_cards.first(_cardPanel);
		_dialog.setVisible(true);
	}


	/**
	 * Make the components for the dialog
	 * @return contents inside a panel
	 */
	private JPanel makeContents()
	{
		JPanel dialogPanel = new JPanel();
		_cards = new CardLayout();
		dialogPanel.setLayout(_cards);

		// Make first card including progress bar
		JPanel firstCard = new JPanel();
		firstCard.setLayout(new BorderLayout());
		JPanel progPanel = new JPanel();
		progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.Y_AXIS));
		progPanel.add(Box.createVerticalGlue());
		progPanel.add(new JLabel(I18nManager.getText("confirm.running")));
		_progressBar = new JProgressBar(0, 10);
		_progressBar.setIndeterminate(true);
		progPanel.add(_progressBar);
		progPanel.add(Box.createVerticalGlue());
		firstCard.add(progPanel, BorderLayout.CENTER);
		// Cancel button at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Cancel model building and close dialog
				if (_model != null) _model.cancel();
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		firstCard.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.add(firstCard, "card1");

		// Make second card including tileset table
		JPanel secondCard = new JPanel();
		secondCard.setLayout(new BorderLayout());
		// Table in the middle
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BorderLayout());
		_setsTable = new JTable();
		_setsTable.setPreferredScrollableViewportSize(new Dimension(500, 130));
		_setsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		midPanel.add(new JScrollPane(_setsTable), BorderLayout.CENTER);
		midPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
		secondCard.add(midPanel, BorderLayout.CENTER);
		// Activate buttons if a tileset is selected
		_setsTable.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					_deleteSetButton.setEnabled(!lsm.isSelectionEmpty());
				}
			});

		// button panel at bottom
		buttonPanel = new JPanel();
		// left group
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		_deleteSetButton = new JButton(I18nManager.getText("button.delete"));
		_deleteSetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showDeleteCard();
			}
		});
		leftPanel.add(_deleteSetButton);
		// right group
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_dialog != null) _dialog.dispose();
			}
		});
		rightPanel.add(closeButton);
		buttonPanel.add(leftPanel, BorderLayout.WEST);
		buttonPanel.add(rightPanel, BorderLayout.EAST);
		secondCard.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.add(secondCard, "card2");

		// Make third card including delete options
		JPanel thirdCard = new JPanel();
		thirdCard.setLayout(new BorderLayout());
		// main panel
		JPanel mainPanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mainPanel.setLayout(gridbag);
		c.gridx = 0; c.gridy = 0;
		c.gridheight = 1; c.gridwidth = 2;
		c.weightx = 0.0; c.weighty = 0.0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		_tileSetLabel = new JLabel("dummy text to be replaced");
		mainPanel.add(_tileSetLabel, c);
		c.gridx = 0; c.gridy = 1;
		c.ipady = 20;
		_zoomLabel = new JLabel("dummy text to be replaced");
		mainPanel.add(_zoomLabel, c);

		JRadioButton deleteOldRadio = new JRadioButton(I18nManager.getText("dialog.diskcache.deleteold"));
		_deleteAllRadio = new JRadioButton(I18nManager.getText("dialog.diskcache.deleteall"));
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(_deleteAllRadio);
		bGroup.add(deleteOldRadio);
		_deleteAllRadio.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				enableAgeFields();
			}
		});
		c.gridx = 0; c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		c.ipady = 0;
		mainPanel.add(deleteOldRadio, c);
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 3;
		c.insets = new Insets(0, 40, 0, 0);
		_ageLabel = new JLabel(I18nManager.getText("dialog.diskcache.maximumage"));
		mainPanel.add(_ageLabel, c);
		_daysField = new WholeNumberField(2);
		_daysField.setMinimumSize(new Dimension(20, 1));
		_daysField.setText("30"); // default is 30 days
		c.gridx = 1; c.gridy = 3;
		c.ipadx = 20;
		c.insets = new Insets(0, 15, 0, 0);
		mainPanel.add(_daysField, c);
		c.gridx = 0; c.gridy = 4;
		c.gridwidth = 2;
		c.ipadx = 0;
		c.insets = new Insets(0, 0, 0, 0);
		mainPanel.add(_deleteAllRadio, c);
		_deleteAllRadio.setSelected(true);
		thirdCard.add(mainPanel, BorderLayout.CENTER);
		// button panel
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton deleteButton = new JButton(I18nManager.getText("button.delete"));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteCurrentSets();
			}
		});
		buttonPanel.add(deleteButton);
		cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Go back to second card
				_cards.show(_cardPanel, "card2");
			}
		});
		buttonPanel.add(cancelButton);
		thirdCard.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.add(thirdCard, "card3");
		return dialogPanel;
	}


	/**
	 * Construct the model in a separate thread
	 * and go on to the second panel of the dialog
	 */
	public void run()
	{
		// TODO: Maybe this can be speeded up so that it just finds the tilesets first and then gets the details later
		// Check if directory has anything in it
		_model = new TileCacheModel(_cacheDir);
		_model.buildTileSets();
		if (_model.isAborted()) return;

		if (_model.getNumTileSets() <= 0)
		{
			_app.showErrorMessage(getNameKey(), "error.cache.empty");
			_dialog.dispose();
			return;
		}

		// Set controls according to current config
		_setsTable.setModel(new TileSetTableModel(_model));
		_deleteSetButton.setEnabled(false);
		// Set column widths after model has been set
		_setsTable.getColumnModel().getColumn(0).setPreferredWidth(220);
		_setsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		// Show second panel
		_cards.next(_cardPanel);
	}

	/**
	 * Prepare and show the delete panel
	 */
	private void showDeleteCard()
	{
		// set tileset label
		int numSelected = 0;
		String desc = null;
		RowInfo totals = new RowInfo();
		for (int i=0; i<_setsTable.getRowCount(); i++) {
			if (_setsTable.isRowSelected(i))
			{
				if (desc == null) desc = _model.getTileSet(i).getPath();
				totals.addRow(_model.getTileSet(i).getRowInfo());
				numSelected++;
			}
		}
		if (numSelected == 0) return;
		String tileSetDesc = (numSelected == 1?desc:I18nManager.getText("dialog.diskcache.tileset.multiple"));
		_tileSetLabel.setText(I18nManager.getText("dialog.diskcache.tileset") + " : "
			+ tileSetDesc);
		_zoomLabel.setText(I18nManager.getText("dialog.diskcache.table.zoom") + " : "
			+ totals.getZoomRange());

		// enable/disable edit fields
		enableAgeFields();
		// show the next card
		_cards.next(_cardPanel);
	}

	/**
	 * Enable or disable the fields for entering tile age
	 */
	private void enableAgeFields()
	{
		boolean showAgeBoxes = !_deleteAllRadio.isSelected();
		_ageLabel.setEnabled(showAgeBoxes);
		_daysField.setEnabled(showAgeBoxes);
	}

	/**
	 * Try to delete all the files in the currently selected tilesets
	 * (Maybe more than one tileset is selected in the table)
	 */
	private void deleteCurrentSets()
	{
		// Determine age limit if given
		int ageLimit = -1;
		if (!_deleteAllRadio.isSelected()) {
			ageLimit = _daysField.getValue();
		}
		// Loop over selected tilesets and delete them
		int totalDeleted = 0;
		for (int i=0; i<_setsTable.getRowCount(); i++)
		{
			if (_setsTable.isRowSelected(i))
			{
				File dir = new File(_model.getCacheDir(), _model.getTileSet(i).getPath());
				if (dir.exists())
				{
					int numFilesDeleted = deleteFilesFrom(dir, ageLimit);
					if (numFilesDeleted > 0) {
						totalDeleted += numFilesDeleted;
					}
				}
			}
		}
		if (totalDeleted > 0)
		{
			// Show confirmation message
			JOptionPane.showMessageDialog(_dialog, I18nManager.getTextWithNumber("dialog.diskcache.deleted", totalDeleted),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
			// reload model
			_cards.first(_cardPanel);
			new Thread(this).start();
		}
		else {
			_app.showErrorMessage(getNameKey(), "error.cache.cannotdelete");
		}
	}


	/**
	 * Delete recursively all files which are older than the age limit
	 * @param inDir directory to delete from
	 * @param inMaxDays age limit in days
	 * @return number of files deleted
	 */
	private static int deleteFilesFrom(File inDir, int inMaxDays)
	{
		int numDeleted = 0;
		long now = System.currentTimeMillis();
		if (inDir.exists() && inDir.isDirectory())
		{
			for (File subdir : inDir.listFiles())
			{
				if (subdir.isDirectory()) {
					numDeleted += deleteFilesFrom(subdir, inMaxDays);
				}
				else if (subdir.isFile() && subdir.exists() && _TILEFILTER.accept(subdir))
				{
					long fileAge = (now - subdir.lastModified()) / 1000 / 60 / 60 / 24;
					if (inMaxDays < 0 || fileAge > inMaxDays)
					{
						if (subdir.delete()) {
							numDeleted++;
						}
					}
				}
			}
			// Try to delete the directory (doesn't work if not empty)
			inDir.delete();
		}
		return numDeleted;
	}
}
