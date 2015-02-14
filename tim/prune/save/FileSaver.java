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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
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
	private JRadioButton[] _delimiterRadios = null;
	private JTextField _otherDelimiterText = null;
	private JRadioButton[] _coordUnitsRadios = null;
	private JRadioButton[] _altitudeUnitsRadios = null;
	private static final int[] FORMAT_COORDS = {Coordinate.FORMAT_NONE, Coordinate.FORMAT_DEG_MIN_SEC,
		Coordinate.FORMAT_DEG_MIN, Coordinate.FORMAT_DEG};
	private static final int[] FORMAT_ALTS = {Altitude.FORMAT_NONE, Altitude.FORMAT_METRES, Altitude.FORMAT_FEET};


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
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.saveoptions.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
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
		_dialog.getContentPane().add(makeDialogComponents(_model, inDefaultDelimiter));
		_dialog.pack();
		_dialog.show();
	}


	/**
	 * Make the dialog components
	 * @param inTableModel table model for fields
	 * @param inDelimiter default delimiter character
	 * @return the GUI components for the save dialog
	 */
	private Component makeDialogComponents(FieldSelectionTableModel inTableModel, char inDelimiter)
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
		_table = new JTable(inTableModel);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePanel.add(_table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(_table, BorderLayout.CENTER);

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
		_table.getSelectionModel().addListSelectionListener(
			new UpDownToggler(_moveUpButton, _moveDownButton, inTableModel.getRowCount())
		);

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
		// choose last-used delimiter as default
		switch (inDelimiter)
		{
			case ','  : _delimiterRadios[0].setSelected(true); break;
			case '\t' : _delimiterRadios[1].setSelected(true); break;
			case ';'  : _delimiterRadios[2].setSelected(true); break;
			case ' '  : _delimiterRadios[3].setSelected(true); break;
			default   : _delimiterRadios[4].setSelected(true);
						_otherDelimiterText.setText("" + inDelimiter);
		}
		delimsPanel.add(otherPanel);
		firstCard.add(delimsPanel);
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
		_coordUnitsRadios[0] = new JRadioButton(I18nManager.getText("dialog.save.units.original"));
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
		JLabel altUnitsLabel = new JLabel(I18nManager.getText("dialog.save.altitudeunits"));
		altUnitsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		secondCardHolder.add(altUnitsLabel);
		JPanel altUnitsPanel = new JPanel();
		altUnitsPanel.setBorder(BorderFactory.createEtchedBorder());
		altUnitsPanel.setLayout(new GridLayout(0, 2));
		_altitudeUnitsRadios = new JRadioButton[3];
		_altitudeUnitsRadios[0] = new JRadioButton(I18nManager.getText("dialog.save.units.original"));
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
		secondCard.add(secondCardHolder, BorderLayout.NORTH);
		_cards.add(secondCard, "card2");

		// Put together with ok/cancel buttons on the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
			    CardLayout cl = (CardLayout)(_cards.getLayout());
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
			    CardLayout cl = (CardLayout)(_cards.getLayout());
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
				if (_altitudeUnitsRadios[i].isSelected())
					altitudeFormat = FORMAT_ALTS[i];
			
			// Check if file exists, don't overwrite any files for v1!
			if (!saveFile.exists())
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
					// For now, just spit out to console
					int numPoints = _track.getNumPoints();
					int numFields = _model.getRowCount();
					for (int p=0; p<numPoints; p++)
					{
						DataPoint point = _track.getPoint(p);
						boolean firstField = true;
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
									buffer.append(point.getAltitude().getValue(altitudeFormat));
								}
								else if (field == Field.TIMESTAMP)
								{
									buffer.append(point.getTimestamp().getText());
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
					JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.ok1")
						 + " " + numPoints + " " + I18nManager.getText("dialog.save.ok2")
						 + saveFile.getAbsolutePath(),
						I18nManager.getText("dialog.save.oktitle"), JOptionPane.INFORMATION_MESSAGE);
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
				saveOK = false;
				JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.save.fileexists"),
					I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
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
