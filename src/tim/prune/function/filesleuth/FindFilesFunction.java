package tim.prune.function.filesleuth;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.TimezoneHelper;
import tim.prune.function.filesleuth.data.DateRange;
import tim.prune.function.filesleuth.data.Filter;
import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileList;
import tim.prune.function.filesleuth.data.TrackFileStatus;
import tim.prune.function.filesleuth.gui.DateRangeEditor;
import tim.prune.function.filesleuth.gui.DateRangeUser;
import tim.prune.function.filesleuth.gui.LocationFilterEditor;
import tim.prune.function.filesleuth.gui.LocationFilterUser;
import tim.prune.function.filesleuth.gui.ResultsTableModel;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.StatusIcon;

/**
 * Function to do the actual looking for track files meeting certain criteria.
 */
public class FindFilesFunction extends GenericFunction implements DateRangeUser, LocationFilterUser
{
	private JDialog _dialog = null;
	private final File _startDir;
	private final boolean _subdirectories;
	private final TrackFileList _trackList = new TrackFileList();
	private ScanController _scanController = null;
	private ResultsTableModel _resultsModel = null;
	private TrackMatcher _matcher = null;
	private JProgressBar _progressBar;
	private JTextField _searchText = null;
	private JTextField _dateRangeField = null;
	private StatusIcon _dateValidStatus = null;
	private JTextField _locationFilterField = null;
	private JButton _editLocationButton = null;
	private JTable _resultsTable = null;
	private JButton _loadButton = null;
	private DateRangeEditor _dateRangeEditor = null;
	private LocationFilter _locationFilter = null;
	private LocationFilterEditor _locationFilterEditor = null;
	private JLabel _filePathLabel = null;


	public FindFilesFunction(App inApp, File inStartDir, boolean inSubdirectories)
	{
		super(inApp);
		_startDir = inStartDir;
		_subdirectories = inSubdirectories;
	}

	@Override
	public String getNameKey() {
		return "menu.file.findfile";
	}

	@Override
	public void begin()
	{
		_dialog = new JDialog(_parentFrame, getName());
		_dialog.setLocationRelativeTo(_parentFrame);
		_dialog.getContentPane().add(makeContents());
		_dialog.pack();

		// Add listener(s) to the track list
		_trackList.addListener(idx -> updateProgressBar());
		TimeZone timezone = TimezoneHelper.getSelectedTimezone(getConfig());
		// Make a scan controller, which will attach itself to the list
		_scanController = new ScanController(_trackList, timezone);
		_progressBar.setIndeterminate(true);
		new TrackFileFinder(_trackList, _startDir, _subdirectories).begin();
		_dialog.setVisible(true);
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		dialogPanel.setLayout(new BorderLayout(0, 5));

		// Filter panel
		JPanel filterPanel = new JPanel();
		double[] weights = new double[] {0.2, 3.0, 0.2, 0.2};
		boolean[] aligns = new boolean[] {true, false, false, false};
		GuiGridLayout grid = new GuiGridLayout(filterPanel, weights, aligns);
		// Search by text
		_searchText = new JTextField(20);
		_searchText.addActionListener(e -> filterChanged());
		JButton searchButton = new JButton(I18nManager.getText("button.search"));
		searchButton.addActionListener(e -> filterChanged());
		grid.add(new JLabel(I18nManager.getText("dialog.findfile.searchtext")));
		grid.add(_searchText, true);
		grid.add(new StatusIcon(getIconManager()));
		grid.add(searchButton);
		// Search by date
		grid.add(new JLabel(I18nManager.getText("dialog.findfile.daterange")));
		_dateRangeField = new JTextField(20);
		_dateRangeField.addActionListener(e -> filterChanged());
		_dateValidStatus = new StatusIcon(getIconManager());
		JButton editDateRangeButton = new JButton(I18nManager.getText("button.edit"));
		editDateRangeButton.addActionListener(e -> editDateRange());
		grid.add(_dateRangeField, true);
		grid.add(_dateValidStatus);
		grid.add(editDateRangeButton);
		// Search by location
		grid.add(new JLabel(I18nManager.getText("dialog.findfile.locationfilter")));
		_locationFilterField = new JTextField(20);
		_locationFilterField.setEditable(false);
		_locationFilterField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				checkLocation();
			}
		});
		_editLocationButton = new JButton(I18nManager.getText("button.edit"));
		_editLocationButton.setEnabled(_app.getTrackInfo().getTrack().getNumPoints() > 0);
		_editLocationButton.addActionListener(e -> editLocationFilter());
		grid.add(_locationFilterField, true);
		grid.add(new StatusIcon(getIconManager()));
		grid.add(_editLocationButton);
		// the filter panel goes at the top of the outer border panel
		dialogPanel.add(filterPanel, BorderLayout.NORTH);

		// Center panel
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0, 5));
		_progressBar = new JProgressBar(0, 10);
		_progressBar.setPreferredSize(new Dimension(300, 30));
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		// Progress panel below the filters at the top of the inner border panel
		centerPanel.add(_progressBar, BorderLayout.NORTH);

		_resultsModel = new ResultsTableModel();
		_matcher = new TrackMatcher(_trackList, _resultsModel);
		_resultsTable = new JTable(_resultsModel);
		_resultsTable.getSelectionModel().addListSelectionListener(this::fileSelected);
		centerPanel.add(new JScrollPane(_resultsTable), BorderLayout.CENTER);
		_filePathLabel = new JLabel(I18nManager.getText("dialog.findfile.filepath.none"));
		centerPanel.add(_filePathLabel, BorderLayout.SOUTH);
		dialogPanel.add(centerPanel, BorderLayout.CENTER);

		// Cancel button at the bottom right
		JPanel buttonPanelr = new JPanel();
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> cancel());
		buttonPanelr.add(cancelButton);

		// Load button at bottom left
		JPanel buttonPanell = new JPanel();
		_loadButton = new JButton(I18nManager.getText("button.load"));
		_loadButton.setEnabled(false);
		_loadButton.addActionListener(e -> loadSelectedTracks());
		buttonPanell.add(_loadButton);

		// Put them together
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(buttonPanell, BorderLayout.WEST);
		buttonPanel.add(buttonPanelr, BorderLayout.EAST);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	private void checkLocation()
	{
		_locationFilterField.setSelectionStart(0);
		_locationFilterField.setSelectionEnd(0);
		final boolean hasPoints = _app.getTrackInfo().getTrack().getNumPoints() > 0;
		_editLocationButton.setEnabled(hasPoints);
		if (!hasPoints) {
			updateLocationFilter(null);
		}
	}

	private void editDateRange()
	{
		String currentRange = _dateRangeField.getText().trim();
		if (_dateRangeEditor == null) {
			_dateRangeEditor = new DateRangeEditor(_dialog, this, getIconManager());
		}
		_dateRangeEditor.show(currentRange);
	}

	private void editLocationFilter()
	{
		if (_app.getTrackInfo().getCurrentPoint() == null)
		{
			if (_locationFilter == null) {
				_app.showErrorMessage(getNameKey(), "dialog.locationfilter.nopointselected");
			}
			updateLocationFilter(null);
			return;
		}
		if (_locationFilterEditor == null) {
			_locationFilterEditor = new LocationFilterEditor(_dialog, this);
		}
		_locationFilterEditor.show(_locationFilter, _app.getTrackInfo().getCurrentPoint());
	}

	private void cancel()
	{
		if (_scanController != null) {
			_scanController.cancel();
		}
		_dialog.dispose();
	}

	/** React to one of the filter settings being changed, pass to Matcher */
	private void filterChanged()
	{
		_matcher.setFilter(new Filter(_searchText.getText(), _dateRangeField.getText(), _locationFilter));
		setDateValidStatus();
		updateProgressBar();
	}

	private void setDateValidStatus()
	{
		if (_dateRangeField.getText().isEmpty()) {
			_dateValidStatus.setStatusBlank();
		}
		else
		{
			final DateRange dateRange = DateRange.parseString(_dateRangeField.getText());
			if (dateRange.isValid()) {
				_dateValidStatus.setStatusValid();
			}
			else {
				_dateValidStatus.setStatusInvalid();
			}
		}
	}

	private void updateProgressBar()
	{
		if (_trackList.isListComplete() && _progressBar.isVisible())
		{
			_progressBar.setIndeterminate(false);
			List<TrackFile> tracks = _trackList.getCurrentContents();
			_progressBar.setMinimum(0);
			_progressBar.setMaximum(tracks.size());
			final int numScanned = countScanned(tracks);
			_progressBar.setValue(numScanned);
			_progressBar.setVisible(numScanned != tracks.size());
		}
	}

	/** Given the list of track files, count the ones which have already been scanned */
	private int countScanned(List<TrackFile> inTracks)
	{
		if (inTracks == null) {
			return 0;
		}
		int numScanned = 0;
		for (TrackFile track : inTracks)
		{
			if (track.getStatus() == TrackFileStatus.COMPLETE) {
				numScanned++;
			}
		}
		return numScanned;
	}

	private void loadSelectedTracks()
	{
		ArrayList<File> files = new ArrayList<>();
		int[] selectedRows = _resultsTable.getSelectedRows();
		for (int row : selectedRows) {
			files.add(_resultsModel.getFile(row));
		}
		if (!files.isEmpty()) {
			_app.loadDataFiles(files);
		}
	}

	public void updateDateRange(DateRange inRange)
	{
		if (inRange == null || inRange.isEmpty())
		{
			_dateRangeField.setText("");
			_dateValidStatus.setStatusBlank();
		}
		else
		{
			_dateRangeField.setText(inRange.toShortString());
			_dateValidStatus.setStatusValid();
		}
		filterChanged();
	}

	public void updateLocationFilter(LocationFilter inFilter)
	{
		_locationFilter = inFilter;
		_locationFilterField.setText(makeLocationFilterText(inFilter));
		filterChanged();
	}

	private static String makeLocationFilterText(LocationFilter inFilter)
	{
		if (inFilter == null || inFilter.getPoint() == null || inFilter.getDistanceValue() <= 0) {
			return "";
		}
		final String pointId = inFilter.getPointDescription();
		final String distanceString = inFilter.getDistanceValue() + " "
			+ I18nManager.getText(inFilter.getDistanceUnit().getShortnameKey());
		return I18nManager.getText("dialog.locationfilter.describe", distanceString, pointId);
	}

	/**
	 * Update the status of the load button and the file path label when a file
	 * or multiple files are selected
	 */
	private void fileSelected(ListSelectionEvent e)
	{
		final int rowsSelected = _resultsTable.getSelectedRowCount();
		_loadButton.setEnabled(rowsSelected > 0);
		if (rowsSelected == 0) {
			_filePathLabel.setText(I18nManager.getText("dialog.findfile.filepath.none"));
		}
		else if (rowsSelected == 1)
		{
			File file = _resultsModel.getFile(_resultsTable.getSelectedRow());
			_filePathLabel.setText(I18nManager.getText("dialog.findfile.filepath.single",
				file.getAbsolutePath()));
		}
		else
		{
			// Get the file path common to all selected files
			String commonPath = _resultsModel.getFile(_resultsTable.getSelectedRow()).getAbsolutePath();
			int[] selectedRows = _resultsTable.getSelectedRows();
			for (int row : selectedRows)
			{
				String currPath = _resultsModel.getFile(row).getAbsolutePath();
				while (!currPath.startsWith(commonPath)) {
					commonPath = new File(commonPath).getParentFile().getAbsolutePath();
				}
			}
			_filePathLabel.setText(I18nManager.getText("dialog.findfile.filepath.multi",
				"" + rowsSelected, commonPath));
		}
	}
}
