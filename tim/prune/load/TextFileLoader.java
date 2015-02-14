package tim.prune.load;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import tim.prune.data.Altitude;
import tim.prune.data.Field;
import tim.prune.data.SourceInfo;


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
	private JPanel _cardPanel = null;
	private CardLayout _layout = null;
	private JButton _backButton = null, _nextButton = null;
	private JButton _finishButton = null;
	private JButton _moveUpButton = null, _moveDownButton = null;
	private JRadioButton[] _delimiterRadios = null;
	private JTextField _otherDelimiterText = null;
	private JLabel _statusLabel = null;
	private DelimiterInfo[] _delimiterInfos = null;
	private FileCacher _fileCacher = null;
	private JList _snippetBox = null;
	private FileExtractTableModel _fileExtractTableModel = null;
	private JTable _fieldTable;
	private FieldSelectionTableModel _fieldTableModel = null;
	private JComboBox _unitsDropDown = null;
	private int _selectedField = -1;
	private char _currentDelimiter = ',';

	// previously selected values
	private char _lastUsedDelimiter = ',';
	private Field[] _lastSelectedFields = null;
	private Altitude.Format _lastAltitudeFormat = Altitude.Format.NO_FORMAT;

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
		else {
			// Didn't pass pre-check
			_app.showErrorMessage("error.load.dialogtitle", "error.load.noread");
			_app.informNoDataLoaded();
		}
	}


	/**
	 * Check the given file for readability and funny characters,
	 * and count the fields for the various separators
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
		_fileCacher = new FileCacher(inFile);

		// Check each line of the file
		String[] fileContents = _fileCacher.getContents();
		boolean fileOK = true;
		_delimiterInfos = new DelimiterInfo[5];
		for (int i=0; i<4; i++) _delimiterInfos[i] = new DelimiterInfo(DELIMITERS[i]);

		String currLine = null;
		String[] splitFields = null;
		int commaFields = 0, semicolonFields = 0, tabFields = 0, spaceFields = 0;
		for (int lineNum=0; lineNum<fileContents.length && fileOK; lineNum++)
		{
			currLine = fileContents[lineNum];
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
				_layout.previous(_cardPanel);
				_backButton.setEnabled(false);
				_nextButton.setEnabled(true);
				_finishButton.setEnabled(false);
			}
		});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_nextButton = new JButton(I18nManager.getText("button.next"));
		_nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				prepareSecondPanel();
				_layout.next(_cardPanel);
				_nextButton.setEnabled(false);
				_backButton.setEnabled(true);
				_finishButton.setEnabled(_fieldTableModel.getRowCount() > 1);
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

		// Make the two cards, for delimiter and fields
		_cardPanel = new JPanel();
		_layout = new CardLayout();
		_cardPanel.setLayout(_layout);
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
		_snippetBox = new JList(_fileCacher.getSnippet(SNIPPET_SIZE, MAX_SNIPPET_WIDTH));
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
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(_fieldTable.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(_fieldTable, BorderLayout.CENTER);
		innerPanel2.add(tablePanel, BorderLayout.CENTER);

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
		JPanel altUnitsPanel = new JPanel();
		altUnitsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		altUnitsPanel.add(new JLabel(I18nManager.getText("dialog.openoptions.altitudeunits")));
		String[] units = {I18nManager.getText("units.metres"), I18nManager.getText("units.feet")};
		_unitsDropDown = new JComboBox(units);
		altUnitsPanel.add(_unitsDropDown);
		secondCard.add(altUnitsPanel, BorderLayout.SOUTH);
		_cardPanel.add(firstCard, "card1");
		_cardPanel.add(secondCard, "card2");

		wholePanel.add(_cardPanel, BorderLayout.CENTER);
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
		for (int i=0; i<4; i++)
			if (_delimiterRadios[i].isSelected()) return _delimiterInfos[i];
		// must be "other" - build info if necessary
		if (_delimiterInfos[4] == null)
			_delimiterInfos[4] = new DelimiterInfo(_otherDelimiterText.getText().charAt(0));
		return _delimiterInfos[4];
	}


	/**
	 * Use the delimiter selected to determine the fields in the file
	 * and prepare the second panel accordingly
	 */
	private void prepareSecondPanel()
	{
		DelimiterInfo info = getSelectedDelimiterInfo();
		FileSplitter splitter = new FileSplitter(_fileCacher);
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
		JComboBox fieldTypesBox = new JComboBox();
		String[] fieldNames = Field.getFieldNames();
		for (int i=0; i<fieldNames.length; i++)
		{
			fieldTypesBox.addItem(fieldNames[i]);
		}
		_fieldTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(fieldTypesBox));

		// Set altitude format to same as last time if available
		if (_lastAltitudeFormat == Altitude.Format.METRES)
			_unitsDropDown.setSelectedIndex(0);
		else if (_lastAltitudeFormat == Altitude.Format.FEET)
			_unitsDropDown.setSelectedIndex(1);
		// no selection on field list
		selectField(-1);
	}


	/**
	 * All options have been selected, so load file
	 */
	private void finished()
	{
		// Save delimiter, field array and altitude format for later use
		_lastUsedDelimiter = _currentDelimiter;
		_lastSelectedFields = _fieldTableModel.getFieldArray();
		Altitude.Format altitudeFormat = Altitude.Format.METRES;
		if (_unitsDropDown.getSelectedIndex() == 1)
		{
			altitudeFormat = Altitude.Format.FEET;
		}
		_lastAltitudeFormat = altitudeFormat;
		// give data to App
		SourceInfo sourceInfo = new SourceInfo(_file, SourceInfo.FILE_TYPE.TEXT);
		_app.informDataLoaded(_fieldTableModel.getFieldArray(),
			_fileExtractTableModel.getData(), altitudeFormat, sourceInfo);
		// clear up file cacher
		_fileCacher.clear();
		// dispose of dialog
		_dialog.dispose();
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
