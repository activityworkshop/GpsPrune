package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.cmd.Command;
import tim.prune.cmd.EditPositionsCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.edit.PointEdit;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.RadioButtonGroup;
import tim.prune.gui.WholeNumberField;


/**
 * Class to provide the function to project each of the points
 * in the current range with a direction and distance
 */
public class ProjectRange extends GenericFunction
{
	private JDialog _dialog = null;
	private WholeNumberField _bearingField = null;
	private JLabel _distanceDescLabel = null;
	private DecimalNumberField _distanceField = null;
	private boolean _distanceIsMetric = true;
	private JRadioButton _createNewRadio = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public ProjectRange(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.projectrange";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (!checkSelection()) {
			return;
		}
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}

		// Clear fields
		_bearingField.setText("");
		_distanceField.setText("");
		// Set the units of the distance label
		setLabelText();
		enableOK();
		_dialog.setVisible(true);
	}

	/** @return true if the selection is ok */
	private boolean checkSelection()
	{
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex = _app.getTrackInfo().getSelection().getEnd();
		return startIndex >= 0 && endIndex > startIndex;
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.projectcircle.desc")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);
		_bearingField = new WholeNumberField(3);
		_distanceField = new DecimalNumberField(false);
		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				enableOK();
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
				else if (inE.getKeyCode() == KeyEvent.VK_ENTER && _okButton.isEnabled()) {
					finish();
				}
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(MouseEvent inE) {
				enableOK();
			}
		};
		_bearingField.addKeyListener(keyListener);
		_bearingField.addMouseListener(mouseListener);
		_distanceField.addKeyListener(keyListener);
		_distanceField.addMouseListener(mouseListener);

		JLabel bearingLabel = new JLabel(I18nManager.getText("dialog.projectpoint.bearing"));
		bearingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(bearingLabel);
		grid.add(_bearingField);

		// Distance including units
		_distanceDescLabel = new JLabel(I18nManager.getText("fieldname.distance") + " (ft)");
		// Note, this label will be reset at each run
		_distanceDescLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(_distanceDescLabel);
		grid.add(_distanceField);

		// Pair of radio buttons for create / edit
		_createNewRadio = new JRadioButton(I18nManager.getText("dialog.projectrange.createcopies"));
		JRadioButton editRadio = new JRadioButton(I18nManager.getText("dialog.projectrange.editexisting"));
		new RadioButtonGroup(_createNewRadio, editRadio);
		_createNewRadio.setSelected(true);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(1, 2)); // one row, two columns
		radioPanel.add(_createNewRadio);
		radioPanel.add(editRadio);
		grid.add(radioPanel, 2, false);

		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> {
			if (_okButton.isEnabled()) {finish();}
		});
		_okButton.setEnabled(false);

		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Set the label text according to the current units
	 */
	private void setLabelText()
	{
		_distanceIsMetric = getConfig().getUnitSet().isMetric();
		final Unit distUnit = _distanceIsMetric ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET;
		final String unitKey = distUnit.getShortnameKey();
		_distanceDescLabel.setText(I18nManager.getText("fieldname.distance") + " (" + I18nManager.getText(unitKey) + ")");
	}

	/**
	 * Enable or disable the OK button based on the contents of the input fields
	 */
	private void enableOK()
	{
		final boolean distanceOk = _distanceField.getValue() > 0.0;
		_okButton.setEnabled(distanceOk);
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		Unit distUnit = _distanceIsMetric ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET;
		final double projectRads = Distance.convertDistanceToRadians(_distanceField.getValue(), distUnit);
		final double bearingRads = Math.toRadians(_bearingField.getValue());
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex = _app.getTrackInfo().getSelection().getEnd();
		if (startIndex < 0 || endIndex <= startIndex) {
			return;
		}

		final Command command;
		if (_createNewRadio != null && _createNewRadio.isSelected()) {
			command = makeAppendCommand(startIndex, endIndex, projectRads, bearingRads);
		}
		else {
			command = makeEditCommand(startIndex, endIndex, projectRads, bearingRads);
		}
		_app.execute(command);
		_dialog.dispose();
	}

	/** The 'create' option was chosen, so we need to create new points */
	private Command makeAppendCommand(int inStartIndex, int inEndIndex,
		double inDistanceRads, double inBearingRads)
	{
		Track track = _app.getTrackInfo().getTrack();
		ArrayList<DataPoint> points = new ArrayList<>();
		for (int pointNum=inStartIndex; pointNum<=inEndIndex; pointNum++)
		{
			// Create point and append to track
			DataPoint currPoint = track.getPoint(pointNum);
			DataPoint point = PointUtils.projectPoint(currPoint, inBearingRads, inDistanceRads);
			point.setSegmentStart(pointNum == inStartIndex || currPoint.getSegmentStart());
			if (currPoint.isWaypoint()) {
				point.setWaypointName(currPoint.getWaypointName() + "'");
			}
			if (currPoint.hasAltitude()) {
				point.setAltitude(currPoint.getAltitude());
			}
			points.add(point);
		}

		// put the points into a new command
		AppendRangeCmd command = new AppendRangeCmd(points);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsadded", points.size()));
		return command;
	}

	/** The 'edit' option was chosen, so we need to edit the existing points */
	private Command makeEditCommand(int inStartIndex, int inEndIndex,
			double inDistanceRads, double inBearingRads)
	{
		ArrayList<PointEdit> latEditList = new ArrayList<>();
		ArrayList<PointEdit> lonEditList = new ArrayList<>();
		Track track = _app.getTrackInfo().getTrack();
		for (int pointNum=inStartIndex; pointNum<=inEndIndex; pointNum++)
		{
			// Project each point and find the calculated latitude and longitude
			DataPoint currPoint = track.getPoint(pointNum);
			DataPoint projectedPoint = PointUtils.projectPoint(currPoint, inBearingRads, inDistanceRads);
			latEditList.add(new PointEdit(pointNum, projectedPoint.getFieldValue(Field.LATITUDE)));
			lonEditList.add(new PointEdit(pointNum, projectedPoint.getFieldValue(Field.LONGITUDE)));
		}

		// put the edits into a new command
		Command command = new EditPositionsCmd(latEditList, lonEditList);
		command.setDescription(getName());
		final int numEdited = inEndIndex - inStartIndex + 1;
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsedited", numEdited));
		return command;
	}
}
