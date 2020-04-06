package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import java.io.File;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Field;
import tim.prune.data.PointCreateOptions;
import tim.prune.data.SourceInfo;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WizardLayout;


/**
 * Class to handle loading of text files including GUI options,
 * and passing loaded data back to App object
 */
public class TextFileLoader
{
	private File _file = null;
	private App _app = null;
	private JFrame _parentFrame = null;
	private JDialog _dialog = null;
	private WizardLayout _wizard = null;
	private JButton _backButton = null, _nextButton = null;
	private JButton _finishButton = null;
	private JButton _moveUpButton = null, _moveDownButton = null;
	private JRadioButton[] _delimiterRadios = null;
	private JTextField _otherDelimiterText = null;
	private JLabel _statusLabel = null;
	private DelimiterInfo[] _delimiterInfos = null;
	private ContentCacher _contentCacher = null;
	private JList<String> _snippetBox = null;
	private FileExtractTableModel _fileExtractTableModel = null;
	private JTable _fieldTable;
	private FieldSelectionTableModel _fieldTableModel = null;
	private JComboBox<String> _altitudeUnitsDropdown = null;
	private JComboBox<String> _hSpeedUnitsDropdown = null;
	private JComboBox<String> _vSpeedUnitsDropdown = null;
	private JRadioButton _vSpeedUpwardsRadio = null;
	private ComponentHider _componentHider = null;
	private int _selectedField = -1;
	private char _currentDelimiter = ',';

	// previously selected values
	private char _lastUsedDelimiter = ',';
	private Field[] _lastSelectedFields = null;
	private Unit _lastAltitudeUnit = null;

	// constants
	private static final int SNIPPET_SIZE = 6;
	private static final int MAX_SNIPPET_WIDTH = 80;
	private static final char[] DELIMITERS = {',', '\t', ';', ' '};


	/**
	 * Inner class to listen for delimiter change operations
	 */
	private class DelimListener implements ActionListener, DocumentListener
	{
		public void actionPerformed(ActionEvent e)
		{
			informDelimiterSelected();
		}
		public void changedUpdate(DocumentEvent e)
		{
			informDelimiterSelected();
		}
		public void insertUpdate(DocumentEvent e)
		{
			informDelimiterSelected();
		}
		public void removeUpdate(DocumentEvent e)
		{
			informDelimiterSelected();
		}
	}


	/**
	 * Constructor
	 * @param inApp Application object to inform of track load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public TextFileLoader(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Open the selected file and show the GUI dialog to select load options
	 * @param inFile file to open
	 */
	public void openFile(File inFile)
	{
		_file = inFile;
		if (preCheckFile(_file))
		{
			showDialog();
		}
		else
		{
			// Didn't pass pre-check
			_app.showErrorMessageNoLookup("error.load.dialogtitle",
				I18nManager.getText("error.load.noread") + ": " + inFile.getName());
			_app.informNoDataLoaded();
		}
	}


	/**
	 * Checks passed, so now build and show the dialog
	 */
	private void showDialog()
	{
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.openoptions.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		_dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// add closing listener
		_dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_dialog.dispose();
				_app.informNoDataLoaded();
			}
		});
		_dialog.getContentPane().add(makeDialogComponents());

		// select best separator according to row counts (more is better)
		int bestDelim = getBestOption(_delimiterInfos[0].getNumWinningRecords(),
			_delimiterInfos[1].getNumWinningRecords(), _delimiterInfos[2].getNumWinningRecords(),
			_delimiterInfos[3].getNumWinningRecords());
		if (bestDelim >= 0)
			_delimiterRadios[bestDelim].setSelected(true);
		else
			_delimiterRadios[_delimiterRadios.length-1].setSelected(true);
		informDelimiterSelected();
		_dialog.pack();
		_dialog.setVisible(true);
	}


	/**
	 * Check the given file for validity
	 * @param inFile file to check
	 */
	private boolean preCheckFile(File inFile)
	{
		// Check file exists and is readable
		if (inFile == null || !inFile.exists() || !inFile.canRead())
		{
			return false;
		}
		// Use a FileCacher to read the file into an array
		_contentCacher = new FileCacher(inFile);

		return preCheckContents();
	}

	/**
	 * Check the contents for readability and funny characters,
	 * and count the fields for the various separators
	 */
	private boolean preCheckContents()
	{
		// Check each line of the file
		String[] contents = _contentCacher.getContents();
		if (contents == null) {
			return false; // nothing cached, might be binary
		}
		boolean fileOK = true;
		_delimiterInfos = new DelimiterInfo[5];
		for (int i=0; i<4; i++) _delimiterInfos[i] = new DelimiterInfo(DELIMITERS[i]);

		String currLine = null;
		String[] splitFields = null;
		int commaFields = 0, semicolonFields = 0, tabFields = 0, spaceFields = 0;
		for (int lineNum=0; lineNum<contents.length && fileOK; lineNum++)
		{
			currLine = contents[lineNum];
			// check for invalid characters
			if (currLine.indexOf('\0') >= 0) {fileOK = false;}
			// check for commas
			splitFields = currLine.split(",");
			commaFields = splitFields.length;
			if (commaFields > 1) _delimiterInfos[0].incrementNumRecords();
			_delimiterInfos[0].updateMaxFields(commaFields);
			// check for tabs
			splitFields = currLine.split("\t");
			tabFields = splitFields.length;
			if (tabFields > 1) _delimiterInfos[1].incrementNumRecords();
			_delimiterInfos[1].updateMaxFields(tabFields);
			// check for semicolons
			splitFields = currLine.split(";");
			semicolonFields = splitFields.length;
			if (semicolonFields > 1) _delimiterInfos[2].incrementNumRecords();
			_delimiterInfos[2].updateMaxFields(semicolonFields);
			// check for spaces
			splitFields = currLine.split(" ");
			spaceFields = splitFields.length;
			if (spaceFields > 1) _delimiterInfos[3].incrementNumRecords();
			_delimiterInfos[3].updateMaxFields(spaceFields);
			// increment counters
			int bestScorer = getBestOption(commaFields, tabFields, semicolonFields, spaceFields);
			if (bestScorer >= 0)
				_delimiterInfos[bestScorer].incrementNumWinningRecords();
		}
		return fileOK;
	}

	/**
	 * @param inText text to load (as if it came from a file)
	 */
	public void loadText(String inText)
	{
		_file = null;
		if (preCheckText(inText))
		{
			showDialog();
		}
		else
		{
			// Didn't pass pre-check
			_app.showErrorMessage("error.load.dialogtitle", "error.load.nopointsintext");
		}
	}

	/**
	 * Check the given text for validity
	 * @param inText (pasted) text to check
	 */
	private boolean preCheckText(String inText)
	{
		if (inText == null || inText.length() < 6)
		{
			return false;
		}
		// Use a cacher to split the text into an array
		_contentCacher = new TextCacher(inText);

		return preCheckContents();
	}


	/**
	 * Get the index of the best one in the list
	 * @return the index of the maximum of the four given values
	 */
	private static int getBestOption(int inOpt0, int inOpt1, int inOpt2, int inOpt3)
	{
		int bestIndex = -1;
		int maxScore = 1;
		if (inOpt0 > maxScore) {bestIndex = 0; maxScore = inOpt0;}
		if (inOpt1 > maxScore) {bestIndex = 1; maxScore = inOpt1;}
		if (inOpt2 > maxScore) {bestIndex = 2; maxScore = inOpt2;}
		if (inOpt3 > maxScore) {bestIndex = 3; maxScore = inOpt3;}
		return bestIndex;
	}


	/**
	 * Make the components for the open options dialog
	 * @return Component for all options
	 */
	private Component makeDialogComponents()
	{
		JPanel wholePanel = new JPanel();
		wholePanel.setLayout(new BorderLayout());

		// add buttons to south
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_wizard.showPreviousCard();
				_nextButton.setEnabled(!_wizard.isLastCard());
				_backButton.setEnabled(!_wizard.isFirstCard());
				_finishButton.setEnabled(false);
			}
		});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_nextButton = new JButton(I18nManager.getText("button.next"));
		_nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				prepareNextPanel(); // Maybe it needs to be initialized based on previous panels
				_wizard.showNextCard();
				_nextButton.setEnabled(!_wizard.isLastCard() && isCurrentCardValid());
				_backButton.setEnabled(!_wizard.isFirstCard());
				_finishButton.setEnabled(_wizard.isLastCard() && isCurrentCardValid());
			}
		});
		buttonPanel.add(_nextButton);
		_finishButton = new JButton(I18nManager.getText("button.finish"));
		_finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finished();
			}
		});
		_finishButton.setEnabled(false);
		buttonPanel.add(_finishButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
				_app.informNoDataLoaded();
			}
		});
		buttonPanel.add(cancelButton);
		wholePanel.add(buttonPanel, BorderLayout.SOUTH);

		// Make the card panel in the centre
		JPanel cardPanel = new JPanel();
		_wizard = new WizardLayout(cardPanel);
		JPanel firstCard = new JPanel();
		firstCard.setLayout(new BorderLayout());
		firstCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));

		JPanel delimsPanel = new JPanel();
		delimsPanel.setLayout(new GridLayout(0, 2));
		delimsPanel.add(new JLabel(I18nManager.getText("dialog.delimiter.label")));
		delimsPanel.add(new JLabel("")); // blank label to go to next grid row
		// radio buttons
		_delimiterRadios = new JRadioButton[5];
		_delimiterRadios[0] = new JRadioButton(I18nManager.getText("dialog.delimiter.comma"));
		delimsPanel.add(_delimiterRadios[0]);
		_delimiterRadios[1] = new JRadioButton(I18nManager.getText("dialog.delimiter.tab"));
		delimsPanel.add(_delimiterRadios[1]);
		_delimiterRadios[2] = new JRadioButton(I18nManager.getText("dialog.delimiter.semicolon"));
		delimsPanel.add(_delimiterRadios[2]);
		_delimiterRadios[3] = new JRadioButton(I18nManager.getText("dialog.delimiter.space"));
		delimsPanel.add(_delimiterRadios[3]);
		JPanel otherPanel = new JPanel();
		otherPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		_delimiterRadios[4] = new JRadioButton(I18nManager.getText("dialog.delimiter.other"));
		otherPanel.add(_delimiterRadios[4]);
		_otherDelimiterText = new JTextField(new OneCharDocument(), null, 2);
		otherPanel.add(_otherDelimiterText);
		// Group radio buttons
		ButtonGroup delimGroup = new ButtonGroup();
		DelimListener delimListener = new DelimListener();
		for (int i=0; i<_delimiterRadios.length; i++)
		{
			delimGroup.add(_delimiterRadios[i]);
			_delimiterRadios[i].addActionListener(delimListener);
		}
		_otherDelimiterText.getDocument().addDocumentListener(delimListener);
		delimsPanel.add(new JLabel(""));
		delimsPanel.add(otherPanel);
		_statusLabel = new JLabel("");
		delimsPanel.add(_statusLabel);
		firstCard.add(delimsPanel, BorderLayout.SOUTH);
		// load snippet to show first few lines
		_snippetBox = new JList<String>(_contentCacher.getSnippet(SNIPPET_SIZE, MAX_SNIPPET_WIDTH));
		_snippetBox.setEnabled(false);
		firstCard.add(makeLabelledPanel("dialog.openoptions.filesnippet", _snippetBox), BorderLayout.CENTER);

		// Second screen, for field order selection
		JPanel secondCard = new JPanel();
		secondCard.setLayout(new BorderLayout());
		secondCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		// table for file contents
		_fileExtractTableModel = new FileExtractTableModel();
		JTable extractTable = new JTable(_fileExtractTableModel);
		JScrollPane tableScrollPane = new JScrollPane(extractTable);
		extractTable.setPreferredScrollableViewportSize(new Dimension(350, 80));
		extractTable.getTableHeader().setReorderingAllowed(false);
		secondCard.add(makeLabelledPanel("dialog.openoptions.filesnippet", tableScrollPane), BorderLayout.NORTH);
		JPanel innerPanel2 = new JPanel();
		innerPanel2.setLayout(new BorderLayout());
		innerPanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		_fieldTable = new JTable(new FieldSelectionTableModel());
		_fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// add listener for selected table row
		_fieldTable.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					if (lsm.isSelectionEmpty()) {
						//no rows are selected
						selectField(-1);
					} else {
						selectField(lsm.getMinSelectionIndex());
					}
				}
			});
		JScrollPane lowerTablePane = new JScrollPane(_fieldTable);
		lowerTablePane.setPreferredSize(new Dimension(300, 100));
		innerPanel2.add(lowerTablePane, BorderLayout.CENTER);

		JPanel innerPanel3 = new JPanel();
		innerPanel3.setLayout(new BoxLayout(innerPanel3, BoxLayout.Y_AXIS));
		_moveUpButton = new JButton(I18nManager.getText("button.moveup"));
		_moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int currRow = _fieldTable.getSelectedRow();
				closeTableComboBox(currRow);
				_fieldTableModel.moveUp(currRow);
				_fieldTable.setRowSelectionInterval(currRow-1, currRow-1);
			}
		});
		innerPanel3.add(_moveUpButton);
		_moveDownButton = new JButton(I18nManager.getText("button.movedown"));
		_moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int currRow = _fieldTable.getSelectedRow();
				closeTableComboBox(currRow);
				_fieldTableModel.moveDown(currRow);
				_fieldTable.setRowSelectionInterval(currRow+1, currRow+1);
			}
		});
		innerPanel3.add(_moveDownButton);
		innerPanel3.add(Box.createVerticalStrut(60));
		JButton guessButton = new JButton(I18nManager.getText("button.guessfields"));
		guessButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_lastSelectedFields = null;
				prepareSecondPanel();
			}
		});
		innerPanel3.add(guessButton);

		innerPanel2.add(innerPanel3, BorderLayout.EAST);
		secondCard.add(innerPanel2, BorderLayout.CENTER);

		// Third card, for units selection of altitude and speeds
		JPanel thirdCard = new JPanel();
		thirdCard.setLayout(new BorderLayout(10, 10));
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BoxLayout(holderPanel, BoxLayout.Y_AXIS));
		// Altitude
		JPanel altUnitsPanel = new JPanel();
		GuiGridLayout altGrid = new GuiGridLayout(altUnitsPanel);
		altUnitsPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("fieldname.altitude")));
		JLabel altLabel = new JLabel(I18nManager.getText("dialog.openoptions.altitudeunits") + ": ");
		altGrid.add(altLabel);
		String[] altUnits = {I18nManager.getText("units.metres"), I18nManager.getText("units.feet")};
		_altitudeUnitsDropdown = new JComboBox<String>(altUnits);
		altGrid.add(_altitudeUnitsDropdown);
		holderPanel.add(altUnitsPanel);
		// Horizontal speed
		JPanel speedPanel = new JPanel();
		GuiGridLayout speedGrid = new GuiGridLayout(speedPanel);
		speedPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("fieldname.speed")));
		JLabel speedLabel = new JLabel(I18nManager.getText("dialog.openoptions.speedunits") + ": ");
		speedGrid.add(speedLabel);
		_hSpeedUnitsDropdown = new JComboBox<String>();
		for (Unit spUnit : UnitSetLibrary.ALL_SPEED_UNITS) {
			_hSpeedUnitsDropdown.addItem(I18nManager.getText(spUnit.getNameKey()));
		}
		speedGrid.add(_hSpeedUnitsDropdown);
		holderPanel.add(speedPanel);
		// Vertical speed
		JPanel vSpeedPanel = new JPanel();
		GuiGridLayout vSpeedGrid = new GuiGridLayout(vSpeedPanel);
		vSpeedPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("fieldname.verticalspeed")));
		JLabel vSpeedLabel = new JLabel(I18nManager.getText("dialog.openoptions.vertspeedunits") + ": ");
		vSpeedGrid.add(vSpeedLabel);
		_vSpeedUnitsDropdown = new JComboBox<String>();
		for (Unit spUnit : UnitSetLibrary.ALL_SPEED_UNITS) {
			_vSpeedUnitsDropdown.addItem(I18nManager.getText(spUnit.getNameKey()));
		}
		vSpeedGrid.add(_vSpeedUnitsDropdown);
		final String vSpeedLabelText = I18nManager.getText("dialog.openoptions.vspeed.intro");
		if (!vSpeedLabelText.isEmpty()) {
			vSpeedGrid.add(new JLabel(vSpeedLabelText));
			vSpeedGrid.add(new JLabel(""));
		}
		_vSpeedUpwardsRadio = new JRadioButton(I18nManager.getText("dialog.openoptions.vspeed.positiveup"));
		JRadioButton vSpeedDownwardsRadio = new JRadioButton(I18nManager.getText("dialog.openoptions.vspeed.positivedown"));
		ButtonGroup vSpeedDirGroup = new ButtonGroup();
		vSpeedDirGroup.add(_vSpeedUpwardsRadio); vSpeedDirGroup.add(vSpeedDownwardsRadio);
		vSpeedGrid.add(_vSpeedUpwardsRadio);     vSpeedGrid.add(vSpeedDownwardsRadio);
		_vSpeedUpwardsRadio.setSelected(true);
		holderPanel.add(vSpeedPanel);
		thirdCard.add(holderPanel, BorderLayout.NORTH);

		// Make a hider to show and hide the components according to the selected fields
		_componentHider = new ComponentHider();
		_componentHider.addComponent(altLabel, Field.ALTITUDE);
		_componentHider.addComponent(_altitudeUnitsDropdown, Field.ALTITUDE);
		_componentHider.addComponent(speedLabel, Field.SPEED);
		_componentHider.addComponent(_hSpeedUnitsDropdown, Field.SPEED);
		_componentHider.addComponent(vSpeedLabel, Field.VERTICAL_SPEED);
		_componentHider.addComponent(_vSpeedUnitsDropdown, Field.VERTICAL_SPEED);
		_componentHider.addComponent(_vSpeedUpwardsRadio, Field.VERTICAL_SPEED);
		_componentHider.addComponent(vSpeedDownwardsRadio, Field.VERTICAL_SPEED);

		// Add cards to the wizard
		_wizard.addCard(firstCard);
		_wizard.addCard(secondCard);
		_wizard.addCard(thirdCard);

		wholePanel.add(cardPanel, BorderLayout.CENTER);
		return wholePanel;
	}


	/**
	 * Close the combo box on the selected row of the field table
	 * @param inRow currently selected row number
	 */
	private void closeTableComboBox(int inRow)
	{
		TableCellEditor editor = _fieldTable.getCellEditor(inRow, 1);
		if (editor != null)
		{
			editor.stopCellEditing();
		}
	}


	/**
	 * change the status based on selection of a delimiter
	 */
	protected void informDelimiterSelected()
	{
		int fields = 0;
		// Loop through radios to see which one is selected
		for (int i=0; i<(_delimiterRadios.length-1); i++)
		{
			if (_delimiterRadios[i].isSelected())
			{
				// Set label text to describe records and fields
				int numRecords = _delimiterInfos[i].getNumRecords();
				if (numRecords == 0)
				{
					_statusLabel.setText(I18nManager.getText("dialog.openoptions.deliminfo.norecords"));
				}
				else
				{
					fields = _delimiterInfos[i].getMaxFields();
					_statusLabel.setText("" + numRecords + " " + I18nManager.getText("dialog.openoptions.deliminfo.records")
						+ " " + fields + " " + I18nManager.getText("dialog.openoptions.deliminfo.fields"));
				}
			}
		}
		// Don't show label if "other" delimiter is chosen (as records, fields are unknown)
		if (_delimiterRadios[_delimiterRadios.length-1].isSelected())
		{
			_statusLabel.setText("");
		}
		// enable/disable next button
		_nextButton.setEnabled((_delimiterRadios[4].isSelected() == false && fields > 1)
			|| _otherDelimiterText.getText().length() == 1);
	}


	/**
	 * Get the delimiter info from the first step
	 * @return delimiter information object for the selected delimiter
	 */
	public DelimiterInfo getSelectedDelimiterInfo()
	{
		for (int i=0; i<4; i++) {
			if (_delimiterRadios[i].isSelected()) return _delimiterInfos[i];
		}
		// must be "other" - build info if necessary
		if (_delimiterInfos[4] == null) {
			_delimiterInfos[4] = new DelimiterInfo(_otherDelimiterText.getText().charAt(0));
		}
		return _delimiterInfos[4];
	}


	/**
	 * Prepare the next panel to be shown, if necessary
	 */
	private void prepareNextPanel()
	{
		int currPanel = _wizard.getCurrentCardIndex();
		if (currPanel == 0) {
			prepareSecondPanel();
		}
		else if (currPanel == 1)
		{
			Field[] selectedFields = _fieldTableModel.getFieldArray();
			// Enable / disable controls based on whether altitude / speed / vspeed fields were chosen on second panel
			_componentHider.enableComponents(Field.ALTITUDE, doesFieldArrayContain(selectedFields, Field.ALTITUDE));
			_componentHider.enableComponents(Field.SPEED, doesFieldArrayContain(selectedFields, Field.SPEED));
			_componentHider.enableComponents(Field.VERTICAL_SPEED, doesFieldArrayContain(selectedFields, Field.VERTICAL_SPEED));
			// TODO: Also check ranges of altitudes, speeds, vert speeds to show them in the third panel
		}
	}

	/**
	 * Use the delimiter selected to determine the fields in the file
	 * and prepare the second panel accordingly
	 */
	private void prepareSecondPanel()
	{
		DelimiterInfo info = getSelectedDelimiterInfo();
		FileSplitter splitter = new FileSplitter(_contentCacher);
		// Check info makes sense - num fields > 0, num records > 0
		// set "Finished" button to disabled if not ok
		// Add data to GUI elements
		String[][] tableData = splitter.splitFieldData(info.getDelimiter());
		// possible to ignore blank columns here
		_currentDelimiter = info.getDelimiter();
		_fileExtractTableModel.updateData(tableData);
		_fieldTableModel = new FieldSelectionTableModel();

		// Check number of fields and use last ones if count matches
		Field[] startFieldArray = null;
		if (_lastSelectedFields != null && splitter.getNumColumns() == _lastSelectedFields.length)
		{
			startFieldArray = _lastSelectedFields;
		}
		else
		{
			// Take first full row of file and use it to guess fields
			startFieldArray = FieldGuesser.guessFields(splitter.getFirstFullRow());
		}

		_fieldTableModel.updateData(startFieldArray);
		_fieldTable.setModel(_fieldTableModel);
		// add dropdowns to second column
		JComboBox<String> fieldTypesBox = new JComboBox<String>();
		String[] fieldNames = Field.getFieldNames();
		for (int i=0; i<fieldNames.length; i++)
		{
			fieldTypesBox.addItem(fieldNames[i]);
		}
		_fieldTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(fieldTypesBox));

		// Set altitude format to same as last time if available
		if (_lastAltitudeUnit == UnitSetLibrary.UNITS_METRES)
			_altitudeUnitsDropdown.setSelectedIndex(0);
		else if (_lastAltitudeUnit == UnitSetLibrary.UNITS_FEET)
			_altitudeUnitsDropdown.setSelectedIndex(1);
		// no selection on field list
		selectField(-1);
	}

	/**
	 * See if the given array of selected fields contains the specified one
	 * @param inFields array of fields selected by user in the second panel
	 * @param inCheck field to check
	 * @return true if the field is present in the array
	 */
	private boolean doesFieldArrayContain(Field[] inFields, Field inCheck)
	{
		if (inFields != null) {
			for (int i=0; i<inFields.length; i++) {
				if (inFields[i] == inCheck) { // == check ok here because it only checks for built-in fields
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * All options have been selected, so load file
	 */
	private void finished()
	{
		// Save delimiter, field array and altitude format for later use
		_lastUsedDelimiter = _currentDelimiter;
		_lastSelectedFields = _fieldTableModel.getFieldArray();
		// TODO: Remember all the units selections for next load?
		// Get the selected units for altitudes and speeds
		SourceInfo sourceInfo = (_file == null ? null : new SourceInfo(_file, SourceInfo.FILE_TYPE.TEXT));
		PointCreateOptions options = new PointCreateOptions();
		options.setAltitudeUnits(_altitudeUnitsDropdown.getSelectedIndex() == 0 ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET);
		Unit hSpeedUnit = UnitSetLibrary.ALL_SPEED_UNITS[_hSpeedUnitsDropdown.getSelectedIndex()];
		options.setSpeedUnits(hSpeedUnit);
		Unit vSpeedUnit = UnitSetLibrary.ALL_SPEED_UNITS[_vSpeedUnitsDropdown.getSelectedIndex()];
		options.setVerticalSpeedUnits(vSpeedUnit, _vSpeedUpwardsRadio.isSelected());

		// give data to App
		_app.informDataLoaded(_fieldTableModel.getFieldArray(),
			_fileExtractTableModel.getData(), options, sourceInfo, null);
		// clear up file cacher
		_contentCacher.clear();
		// dispose of dialog
		_dialog.dispose();
	}

	/**
	 * @return true if the inputs on the current tab are valid, user is allowed to proceed
	 */
	private boolean isCurrentCardValid()
	{
		int cardIndex = _wizard.getCurrentCardIndex();
		if (cardIndex == 1)
		{
			// validate second panel
			return _fieldTableModel.getRowCount() > 1;
		}
		// all other panels are always valid
		return true;
	}

	/**
	 * Make a panel with a label and a component
	 * @param inLabelKey label key to use
	 * @param inComponent component for main area of panel
	 * @return labelled Panel
	 */
	private static JPanel makeLabelledPanel(String inLabelKey, JComponent inComponent)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(I18nManager.getText(inLabelKey)), BorderLayout.NORTH);
		panel.add(inComponent, BorderLayout.CENTER);
		return panel;
	}


	/**
	 * An entry in the field list has been selected
	 * @param inFieldNum index of field, starting with 0
	 */
	private void selectField(int inFieldNum)
	{
		if (inFieldNum == -1 || inFieldNum != _selectedField)
		{
			_selectedField = inFieldNum;
			_moveUpButton.setEnabled(inFieldNum > 0);
			_moveDownButton.setEnabled(inFieldNum >= 0
				&& inFieldNum < (_fieldTableModel.getRowCount()-1));
		}
	}


	/**
	 * @return the last delimiter character used for a load
	 */
	public char getLastUsedDelimiter()
	{
		return _lastUsedDelimiter;
	}
}
