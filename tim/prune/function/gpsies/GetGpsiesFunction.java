package tim.prune.function.gpsies;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.function.browser.BrowserLauncher;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.ZipFileLoader;

/**
 * Function to load track information from Gpsies.com
 * according to the currently viewed area
 */
public class GetGpsiesFunction extends GenericFunction implements Runnable
{
	/** Dialog object */
	private JDialog _dialog = null;
	/** list model */
	private TrackListModel _trackListModel = null;
	/** track table */
	private JTable _trackTable = null;
	/** Cancelled flag */
	private boolean _cancelled = false;
	/** Status label */
	private JLabel _statusLabel = null;
	/** Description box */
	private JTextArea _descriptionBox = null;
	/** Load button */
	private JButton _loadButton = null;
	/** Show button */
	private JButton _showButton = null;
	/** Number of results per page */
	private static final int RESULTS_PER_PAGE = 20;
	/** Maximum number of results to get */
	private static final int MAX_RESULTS = 60;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public GetGpsiesFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.getgpsies";
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
		_trackListModel = new TrackListModel();
		_trackTable = new JTable(_trackListModel);
		_trackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
				{
					if (_trackTable.getSelectedRow() >= 0
					 && _trackTable.getSelectedRow() < _trackListModel.getRowCount())
					{
						_loadButton.setEnabled(true);
						_showButton.setEnabled(true);
						setDescription(_trackListModel.getTrack(_trackTable.getSelectedRow()).getDescription());
						_descriptionBox.setCaretPosition(0);
					}
					else {
						_descriptionBox.setText("");
					}
				}
			}
		});
		_trackTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		_trackTable.getColumnModel().getColumn(1).setPreferredWidth(70);
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
				loadSelectedTrack();
			}
		});
		buttonPanel.add(_loadButton);
		_showButton = new JButton(I18nManager.getText("button.showwebpage"));
		_showButton.setEnabled(false);
		_showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				showSelectedTrack();
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
	 * Run method to call gpsies.com in separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Act on callback to update list and send another request if necessary
		double[] coords = _app.getViewport().getBounds();
		// Example http://www.gpsies.com/api.do?BBOX=10,51,12,53&limit=20&trackTypes=jogging&filetype=kml&device=Run.GPS
		int currPage = 1;

		ArrayList<GpsiesTrack> trackList = null;
		URL url = null;
		String descMessage = "";
		// Loop for each page of the results
		do
		{
			String urlString = "http://www.gpsies.com/api.do?BBOX=" +
				coords[1] + "," + coords[0] + "," + coords[3] + "," + coords[2] +
				"&limit=" + RESULTS_PER_PAGE + "&resultPage=" + currPage;
			// System.out.println(urlString);
			// Parse the returned XML with a special handler
			GpsiesXmlHandler xmlHandler = new GpsiesXmlHandler();
			try
			{
				url = new URL(urlString);
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				saxParser.parse(url.openStream(), xmlHandler);
			}
			catch (Exception e) {
				descMessage = e.getClass().getName() + " - " + e.getMessage();
			}
			// TODO: Close streams somehow?  Haven't got a reference to the input stream to close it!
			// Add track list to model
			trackList = xmlHandler.getTrackList();
			_trackListModel.addTracks(trackList);

			// Compare number of results with results per page and call again if necessary
			currPage++;
		}
		while (trackList != null && trackList.size() == RESULTS_PER_PAGE
		 && _trackListModel.getRowCount() < MAX_RESULTS && !_cancelled);
		// Set status label according to error or "none found", leave blank if ok
		if (descMessage.equals("") && (trackList == null || trackList.size() == 0)) {
			descMessage = I18nManager.getText("dialog.gpsies.nonefound");
		}
		_statusLabel.setText(descMessage);
	}


	/**
	 * Load the selected track
	 */
	private void loadSelectedTrack()
	{
		// Find the row selected in the table and get the corresponding track
		int rowNum = _trackTable.getSelectedRow();
		if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
		{
			String url = _trackListModel.getTrack(rowNum).getDownloadLink();
			XmlFileLoader xmlLoader = new XmlFileLoader(_app);
			ZipFileLoader loader = new ZipFileLoader(_app, xmlLoader);
			try
			{
				loader.openStream(new URL(url).openStream());
			}
			catch (IOException ioe) {
				System.err.println("IO Exception : " + ioe.getMessage());
			}
		}
		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}


	/**
	 * Show the webpage for the selected track
	 */
	private void showSelectedTrack()
	{
		// Find the row selected in the table and show the corresponding url
		int rowNum = _trackTable.getSelectedRow();
		if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
		{
			String id = _trackListModel.getTrack(rowNum).getFileId();
			BrowserLauncher.launchBrowser("http://gpsies.com/map.do?fileId=" + id);
		}
		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}
}
