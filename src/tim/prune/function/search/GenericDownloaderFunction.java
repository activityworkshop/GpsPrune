package tim.prune.function.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Function to load track information from any source,
 * subclassed for special cases like wikipedia or OSM
 */
public abstract class GenericDownloaderFunction extends GenericFunction implements Runnable
{
	/** Dialog object */
	protected JDialog _dialog = null;
	/** list model */
	protected TrackListModel _trackListModel = null;
	/** track table */
	protected JTable _trackTable = null;
	/** Cancelled flag */
	protected boolean _cancelled = false;
	/** error message */
	protected String _errorMessage = null;
	/** Status label */
	protected JLabel _statusLabel = null;
	/** Description box */
	private JTextArea _descriptionBox = null;
	/** Load button */
	private JButton _loadButton = null;
	/** Show button */
	private JButton _showButton = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public GenericDownloaderFunction(App inApp) {
		super(inApp);
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Initialise dialog, show empty list
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			// add closing listener
			_dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					_cancelled = true;
				}
			});
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Clear list
		_trackListModel.clear();
		_loadButton.setEnabled(false);
		_showButton.setEnabled(false);
		_cancelled = false;
		_descriptionBox.setText("");
		_errorMessage = null;
		// Start new thread to load list asynchronously
		new Thread(this).start();

		// Show dialog
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

		// Status label
		_statusLabel = new JLabel(I18nManager.getText("confirm.running"));
		dialogPanel.add(_statusLabel, BorderLayout.NORTH);
		// Main panel with track list
		_trackListModel = new TrackListModel(getColumnKey(0), getColumnKey(1));
		_trackTable = new JTable(_trackListModel);
		_trackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
				{
					final int numSelected = _trackTable.getSelectedRowCount();
					boolean foundUrl = false;
					if (numSelected > 0)
					{
						setDescription(_trackListModel.getTrack(_trackTable.getSelectedRow()).getDescription());
						_descriptionBox.setCaretPosition(0);
						foundUrl = _trackListModel.getTrack(_trackTable.getSelectedRow()).getWebUrl() != null;
					}
					else {
						_descriptionBox.setText("");
					}
					_loadButton.setEnabled(numSelected > 0);
					_showButton.setEnabled(numSelected == 1 && foundUrl);
				}
			}
		});
		_trackTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		if (_trackListModel.getColumnCount() > 1) {
			_trackTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		}
		JScrollPane tablePane = new JScrollPane(_trackTable);
		tablePane.setPreferredSize(new Dimension(450, 200));
		// Panel to hold description label and box
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new BorderLayout());
		JLabel descLabel = new JLabel(I18nManager.getText("dialog.gpsies.description") + " :");
		descPanel.add(descLabel, BorderLayout.NORTH);
		_descriptionBox = new JTextArea(5, 20);
		_descriptionBox.setEditable(false);
		_descriptionBox.setLineWrap(true);
		_descriptionBox.setWrapStyleWord(true);
		JScrollPane descPane = new JScrollPane(_descriptionBox);
		descPane.setPreferredSize(new Dimension(400, 80));
		descPanel.add(descPane, BorderLayout.CENTER);
		// Use split pane to split table from description
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, descPanel);
		splitPane.setResizeWeight(1.0);
		dialogPanel.add(splitPane, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_loadButton = new JButton(I18nManager.getText("button.load"));
		_loadButton.setEnabled(false);
		_loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				loadSelected();
			}
		});
		buttonPanel.add(_loadButton);
		_showButton = new JButton(I18nManager.getText("button.showwebpage"));
		_showButton.setEnabled(false);
		_showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				showSelectedWebpage();
			}
		});
		buttonPanel.add(_showButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_cancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * @param inColNum index of column, 0 or 1
	 * @return key for this column
	 */
	protected abstract String getColumnKey(int inColNum);

	/**
	 * Set the description in the box
	 * @param inDesc description to set, or null for no description
	 */
	private void setDescription(String inDesc)
	{
		String text = inDesc;
		if (inDesc == null || inDesc.length() < 2) {
			text = I18nManager.getText("dialog.gpsies.nodescription");
		}
		_descriptionBox.setText(text);
	}


	/**
	 * Load the selected track or point
	 */
	protected abstract void loadSelected();


	/**
	 * Show the webpage for the selected item
	 */
	private void showSelectedWebpage()
	{
		// Find the row selected in the table and show the corresponding url
		int rowNum = _trackTable.getSelectedRow();
		if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
		{
			String url = _trackListModel.getTrack(rowNum).getWebUrl();
			BrowserLauncher.launchBrowser(url);
		}
		// Don't close the dialog
	}
}
