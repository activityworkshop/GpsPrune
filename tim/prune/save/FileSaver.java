package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.load.OneCharDocument;

/**
 * Class to manage the saving of track data
 * into a user-specified file
 */
public class FileSaver
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private Track _track = null;
	private JDialog _dialog = null;
	private JFileChooser _fileChooser = null;
	private JPanel _cards = null;
	private JButton _nextButton = null, _backButton = null;
	private JTable _table = null;
	private FieldSelectionTableModel _model = null;
	private JButton _moveUpButton = null, _moveDownButton = null;
	private UpDownToggler _toggler = null;
	private JRadioButton[] _delimiterRadios = null;
	private JTextField _otherDelimiterText = null;
	private JCheckBox _headerRowCheckbox = null;
	private JRadioButton[] _coordUnitsRadios = null;
	private JRadioButton[] _altitudeUnitsRadios = null;
	private JRadioButton[] _timestampUnitsRadios = null;
	private static final int[] FORMAT_COORDS = {Coordinate.FORMAT_NONE, Coordinate.FORMAT_DEG_MIN_SEC,
		Coordinate.FORMAT_DEG_MIN, Coordinate.FORMAT_DEG};
	private static final int[] FORMAT_ALTS = {Altitude.FORMAT_NONE, Altitude.FORMAT_METRES, Altitude.FORMAT_FEET};
	private static final int[] FORMAT_TIMES = {Timestamp.FORMAT_ORIGINAL, Timestamp.FORMAT_LOCALE, Timestamp.FORMAT_ISO_8601};


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 * @param inParentFrame parent frame
	 * @param inTrack track object to save
	 */
	public FileSaver(App inApp, JFrame inParentFrame, Track inTrack)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
		_track = inTrack;
	}


	/**
	 * Show the save file dialog
	 * @param inDefaultDelimiter default delimiter to use
	 */
	public void showDialog(char inDefaultDelimiter)
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.saveoptions.title"), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Check field list
		FieldList fieldList = _track.getFieldList();
		int numFields = fieldList.getNumFields();
		_model = new FieldSelectionTableModel(numFields);
		for (int i=0; i<numFields; i++)
		{
			Field field = fieldList.getField(i);
			FieldInfo info = new FieldInfo(field, _track.hasData(field));
			_model.addFieldInfo(info, i);
		}
		// Initialise dialog and show it
		initDialog(_model, inDefaultDelimiter);
		_dialog.show();
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the save dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		_cards = new JPanel();
		_cards.setLayout(new CardLayout());
		panel.add(_cards, BorderLayout.CENTER);

		// Make first card for field selection and delimiter
		JPanel firstCard = new JPanel();
		firstCard.setLayout(new BoxLayout(firstCard, BoxLayout.Y_AXIS));
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		_table = new JTable();
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Enclose table in a scrollpane to prevent other components getting lost
		JScrollPane scrollPane = new JScrollPane(_table);
		_table.setPreferredScrollableViewportSize(new Dimension(300, 150));
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		// Make a panel to hold the table and up/down buttons
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new BorderLayout());
		fieldsPanel.add(tablePanel, BorderLayout.CENTER);
		JPanel updownPanel = new JPanel();
		updownPanel.setLayout(new BoxLayout(updownPanel, BoxLayout.Y_AXIS));
		_moveUpButton = new JButton(I18nManager.getText("button.moveup"));
		_moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int row = _table.getSelectedRow();
				if (row > 0)
				{
					_model.swapItems(row, row - 1);
					_table.setRowSelectionInterval(row - 1, row - 1);
				}
			}
		});
		_moveUpButton.setEnabled(false);
		updownPanel.add(_moveUpButton);
		_moveDownButton = new JButton(I18nManager.getText("button.movedown"));
		_moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int row = _table.getSelectedRow();
				if (row > -1 && row < (_model.getRowCount() - 1))
				{
					_model.swapItems(row, row + 1);
					_table.setRowSelectionInterval(row + 1, row + 1);
				}
			}
		});
		_moveDownButton.setEnabled(false);
		updownPanel.add(_moveDownButton);
		fieldsPanel.add(updownPanel, BorderLayout.EAST);
		// enable/disable buttons based on table row selection
		_toggler = new UpDownToggler(_moveUpButton, _moveDownButton);
		_table.getSelectionModel().addListSelectionListener(_toggler);

		// Add fields panel and the delimiter panel to first card in pack
		JLabel saveOptionsLabel = new JLabel(I18nManager.getText("dialog.save.fieldstosave"));
		saveOptionsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		firstCard.add(saveOptionsLabel);
		fieldsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		firstCard.add(fieldsPanel);
		firstCard.add(Box.createRigidArea(new Dimension(0,10)));

		// delimiter panel
		JLabel delimLabel = new JLabel(I18nManager.getText("dialog.delimiter.label"));
		delimLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		firstCard.add(delimLabel);
		JPanel delimsPanel = new JPanel();
		delimsPanel.setLayout(new GridLayout(0, 2));
		delimsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
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
		for (int i=0; i<_delimiterRadios.length; i++)
		{
			delimGroup.add(_delimiterRadios[i]);
		}
		delimsPanel.add(otherPanel);
		firstCard.add(delimsPanel);

		// header checkbox
		firstCard.add(Box.createRigidArea(new Dimension(0,10)));
		_headerRowCheckbox = new JCheckBox(I18nManager.getText("dialog.save.headerrow"));
		firstCard.add(_headerRowCheckbox);

		_cards.add(firstCard, "card1");

		JPanel secondCard = new JPanel();
		secondCard.setLayout(new BorderLayout());
		JPanel secondCardHolder = new JPanel();
		secondCardHolder.setLayout(new BoxLayout(secondCardHolder, BoxLayout.Y_AXIS));
		JLabel coordLabel = new JLabel(I18nManager.getText("dialog.save.coordinateunits"));
		coordLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		secondCardHolder.add(coordLabel);
		JPanel coordsUnitsPanel = new JPanel();
		coordsUnitsPanel.setBorder(BorderFactory.createEtchedBorder());
		coordsUnitsPanel.setLayout(new GridLayout(0, 2));
		_coordUnitsRadios = new JRadioButton[4];
		_coordUnitsRadios[0] = new JRadioButton(I18nManager.getText("units.original"));
		_coordUnitsRadios[1] = new JRadioButton(I18nManager.getText("units.degminsec"));
		_coordUnitsRadios[2] = new JRadioButton(I18nManager.getText("units.degmin"));
		_coordUnitsRadios[3] = new JRadioButton(I18nManager.getText("units.deg"));
		ButtonGroup coordGroup = new ButtonGroup();
		for (int i=0; i<4; i++)
		{
			coordGroup.add(_coordUnitsRadios[i]);
			coordsUnitsPanel.add(_coordUnitsRadios[i]);
			_coordUnitsRadios[i].setSelected(i==0);
		}
		coordsUnitsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		secondCardHolder.add(coordsUnitsPanel);
		secondCardHolder.add(Box.createRigidArea(new Dimension(0,10)));
		// altitude units
		JLabel altUnitsLabel = new JLabel(I18nManager.getText("dialog.save.altitudeunits"));
		altUnitsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		secondCardHolder.add(altUnitsLabel);
		JPanel altUnitsPanel = new JPanel();
		altUnitsPanel.setBorder(BorderFactory.createEtchedBorder());
		altUnitsPanel.setLayout(new GridLayout(0, 2));
		_altitudeUnitsRadios = new JRadioButton[3];
		_altitudeUnitsRadios[0] = new JRadioButton(I18nManager.getText("units.original"));
		_altitudeUnitsRadios[1] = new JRadioButton(I18nManager.getText("units.metres"));
		_altitudeUnitsRadios[2] = new JRadioButton(I18nManager.getText("units.feet"));
		ButtonGroup altGroup = new ButtonGroup();
		for (int i=0; i<3; i++)
		{
			altGroup.add(_altitudeUnitsRadios[i]);
			altUnitsPanel.add(_altitudeUnitsRadios[i]);
			_altitudeUnitsRadios[i].setSelected(i==0);
		}
		altUnitsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		secondCardHolder.add(altUnitsPanel);
		secondCardHolder.add(Box.createRigidArea(new Dimension(0,10)));
		// Selection of format of timestamps
		JLabel timestampLabel = new JLabel(I18nManager.getText("dialog.save.timestampformat"));
		timestampLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		secondCardHolder.add(timestampLabel);
		JPanel timestampPanel = new JPanel();
		timestampPanel.setBorder(BorderFactory.createEtchedBorder());
		timestampPanel.setLayout(new GridLayout(0, 2));
		_timestampUnitsRadios = new JRadioButton[3];
		_timestampUnitsRadios[0] = new JRadioButton(I18nManager.getText("units.original"));
		_timestampUnitsRadios[1] = new JRadioButton(I18nManager.getText("units.default"));
		_timestampUnitsRadios[2] = new JRadioButton(I18nManager.getText("units.iso8601"));
		ButtonGroup timeGroup = new ButtonGroup();
		for (int i=0; i<3; i++)
		{
			timeGroup.add(_timestampUnitsRadios[i]);
			timestampPanel.add(_timestampUnitsRadios[i]);
			_timestampUnitsRadios[i].setSelected(i==0);
		}
		timestampPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		secondCardHolder.add(timestampPanel);
		secondCard.add(secondCardHolder, BorderLayout.NORTH);
		_cards.add(secondCard, "card2");

		// Put together with ok/cancel buttons on the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				CardLayout cl = (CardLayout) _cards.getLayout();
				cl.previous(_cards);
				_backButton.setEnabled(false);
				_nextButton.setEnabled(true);
			}
		});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_nextButton = new JButton(I18nManager.getText("button.next"));
		_nextButton.setEnabled(true);
		_nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				CardLayout cl = (CardLayout) _cards.getLayout();
				cl.next(_cards);
				_backButton.setEnabled(true);
				_nextButton.setEnabled(false);
			}
		});
		buttonPanel.add(_nextButton);
		JButton okButton = new JButton(I18nManager.getText("button.finish"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (saveToFile())
				{
					_dialog.dispose();
				}
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Initialize the dialog with the given details
	 * @param inModel table model
	 * @param inDefaultDelimiter default delimiter character
	 */
	private void initDialog(TableModel inModel, char inDefaultDelimiter)
	{
		// set table model
		_table.setModel(inModel);
		// reset toggler
		_toggler.setListSize(inModel.getRowCount());
		// choose last-used delimiter as default
		switch (inDefaultDelimiter)
		{
			case ','  : _delimiterRadios[0].setSelected(true); break;
			case '\t' : _delimiterRadios[1].setSelected(true); break;
			case ';'  : _delimiterRadios[2].setSelected(true); break;
			case ' '  : _delimiterRadios[3].setSelected(true); break;
			default   : _delimiterRadios[4].setSelected(true);
						_otherDelimiterText.setText("" + inDefaultDelimiter);
		}
		// set card and enable buttons
		CardLayout cl = (CardLayout) _cards.getLayout();
		cl.first(_cards);
		_nextButton.setEnabled(true);
		_backButton.setEnabled(false);
	}


	/**
	 * Save the track to file with the chosen options
	 * @return true if successful or cancelled, false if failed
	 */
	private boolean saveToFile()
	{
		boolean saveOK = true;
		FileWriter writer = null;
		if (_fileChooser == null)
			_fileChooser = new JFileChooser();
		_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			File saveFile = _fileChooser.getSelectedFile();
			String lineSeparator = System.getProperty("line.separator");
			// Get coordinate format and altitude format
			int coordFormat = Coordinate.FORMAT_NONE;
			for (int i=0; i<_coordUnitsRadios.length; i++)
				if (_coordUnitsRadios[i].isSelected())
					coordFormat = FORMAT_COORDS[i];
			int altitudeFormat = Altitude.FORMAT_NONE;
			for (int i=0; i<_altitudeUnitsRadios.length; i++)
			{
				if (_altitudeUnitsRadios[i].isSelected())
				{
					altitudeFormat = FORMAT_ALTS[i];
				}
			}
			// Get timestamp formats
			int timestampFormat = Timestamp.FORMAT_ORIGINAL;
			for (int i=0; i<_timestampUnitsRadios.length; i++)
			{
				if (_timestampUnitsRadios[i].isSelected())
				{
					timestampFormat = FORMAT_TIMES[i];
				}
			}

			// Check if file exists, and confirm overwrite if necessary
			Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
			if (!saveFile.exists() || JOptionPane.showOptionDialog(_parentFrame,
					I18nManager.getText("dialog.save.overwrite.text"),
					I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
				== JOptionPane.YES_OPTION)
			{
				try
				{
					// Create output file
					writer = new FileWriter(saveFile);
					// Determine delimiter character to use
					char delimiter = getDelimiter();
					FieldInfo info = null;
					Field field = null;

					StringBuffer buffer = null;
					int numFields = _model.getRowCount();
					boolean firstField = true;
					// Write header row if required
					if (_headerRowCheckbox.isSelected())
					{
						buffer = new StringBuffer();
						for (int f=0; f<numFields; f++)
						{
							info = _model.getFieldInfo(f);
							if (info.isSelected())
							{
								if (!firstField)
								{
									// output field separator
									buffer.append(delimiter);
								}
								field = info.getField();
								buffer.append(field.getName());
								firstField = false;
							}
						}
						writer.write(buffer.toString());
						writer.write(lineSeparator);
					}

					// Loop over points outputting each in turn to buffer
					int numPoints = _track.getNumPoints();
					for (int p=0; p<numPoints; p++)
					{
						DataPoint point = _track.getPoint(p);
						firstField = true;
						buffer = new StringBuffer();
						for (int f=0; f<numFields; f++)
						{
							info = _model.getFieldInfo(f);
							if (info.isSelected())
							{
								if (!firstField)
								{
									// output field separator
									buffer.append(delimiter);
								}
								field = info.getField();
								// Output field according to type
								if (field == Field.LATITUDE)
								{
									buffer.append(point.getLatitude().output(coordFormat));
								}
								else if (field == Field.LONGITUDE)
								{
									buffer.append(point.getLongitude().output(coordFormat));
								}
								else if (field == Field.ALTITUDE)
								{
									try
									{
										buffer.append(point.getAltitude().getValue(altitudeFormat));
									}
									catch (NullPointerException npe) {}
								}
								else if (field == Field.TIMESTAMP)
								{
									if (point.hasTimestamp())
									{
										if (timestampFormat == Timestamp.FORMAT_ORIGINAL) {
											// output original string
											buffer.append(point.getFieldValue(Field.TIMESTAMP));
										}
										else {
											// format value accordingly
											buffer.append(point.getTimestamp().getText(timestampFormat));
										}
									}
								}
								else
								{
									String value = point.getFieldValue(field);
									if (value != null)
									{
										buffer.append(value);
									}
								}
								firstField = false;
							}
						}
						// Output to file
						writer.write(buffer.toString());
						writer.write(lineSeparator);
					}
					// Save successful
					UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
						 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
						 + " " + saveFile.getAbsolutePath());
					_app.informDataSaved();
				}
				catch (IOException ioe)
				{
					saveOK = false;
					JOptionPane.showMessageDialog(_parentFrame,
						I18nManager.getText("error.save.failed") + ioe.getMessage(),
						I18nManager.getText("error.save.dialogtitle"),
						JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					// try to close file if it's open
					try
					{
						if (writer != null)
						{
							writer.close();
						}
					}
					catch (Exception e) {}
				}
			}
			else
			{
				// Overwrite file confirm cancelled
				saveOK = false;
			}
		}
		return saveOK;
	}


	/**
	 * @return the selected delimiter character
	 */
	private char getDelimiter()
	{
		// Check the preset 4 delimiters
		final char[] delimiters = {',', '\t', ';', ' '};
		for (int i=0; i<4; i++)
		{
			if (_delimiterRadios[i].isSelected())
			{
				return delimiters[i];
			}
		}
		// Wasn't any of those so must be 'other'
		return _otherDelimiterText.getText().charAt(0);
	}
}
