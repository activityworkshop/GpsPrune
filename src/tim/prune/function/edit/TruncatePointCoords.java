package tim.prune.function.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.gui.GuiGridLayout;

/**
 * Function to manage the rounding of point coordinates
 */
public class TruncatePointCoords extends GenericFunction
{
	private JDialog _dialog = null;
	private DataPoint _point = null;
	private JComboBox<String> _coordFormatDropdown = null;
	private JSpinner _numDigitsField = null;
	private JTextField _previewField = null;


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 */
	public TruncatePointCoords(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.truncatecoords";
	}

	/**
	 * Begin the function by showing the dialog
	 */
	public void begin()
	{
		_point = _app.getTrackInfo().getCurrentPoint();
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			// Create Gui and show it
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		showPreview();
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.truncatecoords.intro")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);

		String[] coordFormats = {I18nManager.getText("units.degminsec"),
			I18nManager.getText("units.degmin"), I18nManager.getText("units.deg")};
		_coordFormatDropdown = new JComboBox<String>(coordFormats);
		_coordFormatDropdown.setSelectedIndex(1); // Go for DD MM.MMM by default
		_numDigitsField = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));
		_numDigitsField.setValue(3);

		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				// enableOK();
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		ActionListener formatListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showPreview();
			}
		};
		_coordFormatDropdown.addKeyListener(keyListener);
		_coordFormatDropdown.addActionListener(formatListener);
		_numDigitsField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				showPreview();
			}
		});

		// Coordinate format
		JLabel formatLabel = new JLabel(I18nManager.getText("details.coordformat"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(formatLabel);
		grid.add(_coordFormatDropdown);

		// Num digits
		JLabel digitsLabel = new JLabel(I18nManager.getText("dialog.truncatecoords.numdigits"));
		digitsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(digitsLabel);
		grid.add(_numDigitsField);

		// Preview
		JLabel previewLabel = new JLabel(I18nManager.getText("dialog.truncatecoords.preview"));
		previewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(previewLabel);
		_previewField = new JTextField("", 26);
		_previewField.setEditable(false);
		grid.add(_previewField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		okButton.addActionListener(okListener);
		buttonPanel.add(okButton);
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
	 * Update the preview with the current values
	 */
	private void showPreview()
	{
		String[] coordStrings = getTruncatedCoords();
		_previewField.setText(coordStrings[0] + ", " + coordStrings[1]);
	}

	/**
	 * @return the truncated latitude and longitude strings
	 */
	private String[] getTruncatedCoords()
	{
		int[] formatIds = new int[] {Coordinate.FORMAT_DEG_MIN_SEC, Coordinate.FORMAT_DEG_MIN, Coordinate.FORMAT_DECIMAL_FORCE_POINT};
		final int selectedFormat = formatIds[_coordFormatDropdown.getSelectedIndex()];
		final int numDigits = ((SpinnerNumberModel) _numDigitsField.getModel()).getNumber().intValue();
		return new String[] {_point.getLatitude().output(selectedFormat, numDigits),
			_point.getLongitude().output(selectedFormat, numDigits)};
	}

	/**
	 * Confirm the edit and inform the app
	 */
	private void finish()
	{
		// Make lists for edit and undo, and add the changed fields
		FieldEditList editList = new FieldEditList();
		FieldEditList undoList = new FieldEditList();

		// Set latitude, longitude
		String[] coordStrings = getTruncatedCoords();
		editList.addEdit(new FieldEdit(Field.LATITUDE, coordStrings[0]));
		undoList.addEdit(new FieldEdit(Field.LATITUDE, _point.getFieldValue(Field.LATITUDE)));
		editList.addEdit(new FieldEdit(Field.LONGITUDE, coordStrings[1]));
		undoList.addEdit(new FieldEdit(Field.LONGITUDE, _point.getFieldValue(Field.LONGITUDE)));

		// Pass back to App to perform edit
		_app.completePointEdit(editList, undoList);
		_dialog.dispose();
	}
}
