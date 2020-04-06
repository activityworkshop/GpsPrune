package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;


/**
 * Class to provide the function to project the current point
 * with a given bearing and distance
 */
public class ProjectPoint extends GenericFunction
{
	private JDialog _dialog = null;
	private WholeNumberField _bearingField = null;
	private JLabel _distanceDescLabel = null;
	private DecimalNumberField _distanceField = null;
	private boolean _distanceIsMetric = true;
	private JTextField _nameField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public ProjectPoint(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.projectpoint";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}

		// Clear fields
		_bearingField.setText("");
		_distanceField.setText("");
		_nameField.setText("");
		// Set the units of the distance label
		setLabelText();
		enableOK();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.projectpoint.desc")), BorderLayout.NORTH);
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

		// Waypoint name
		JLabel nameLabel = new JLabel(I18nManager.getText("dialog.pointnameedit.name"));
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(nameLabel);
		_nameField = new JTextField("", 12);
		grid.add(_nameField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (_okButton.isEnabled()) {finish();}
			}
		};
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);

		buttonPanel.add(_okButton);
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
	 * Set the label text according to the current units
	 */
	private void setLabelText()
	{
		Unit distUnit = Config.getUnitSet().getDistanceUnit();
		_distanceIsMetric = (distUnit == UnitSetLibrary.UNITS_METRES || distUnit == UnitSetLibrary.UNITS_KILOMETRES);
		distUnit = _distanceIsMetric ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET;
		final String unitKey = distUnit.getShortnameKey();
		_distanceDescLabel.setText(I18nManager.getText("fieldname.distance") + " (" + I18nManager.getText(unitKey) + ")");
	}

	/**
	 * Enable or disable the OK button based on the contents of the input fields
	 */
	private void enableOK()
	{
		final boolean bearingOk = !_bearingField.getText().isEmpty()
			&& _bearingField.getValue() < 360;
		final boolean distanceOk = _distanceField.getValue() > 0.0;
		_okButton.setEnabled(bearingOk && distanceOk);
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		DataPoint currPoint = _app.getTrackInfo().getCurrentPoint();
		Unit distUnit = _distanceIsMetric ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET;
		final double projectRads = Distance.convertDistanceToRadians(_distanceField.getValue(), distUnit);
		final double origLatRads = Math.toRadians(currPoint.getLatitude().getDouble());
		final double origLonRads = Math.toRadians(currPoint.getLongitude().getDouble());
		System.out.println("Project from: " + origLatRads + ", " + origLonRads);
		final double bearingRads = Math.toRadians(_bearingField.getValue());

		double lat2 = Math.asin(Math.sin(origLatRads) * Math.cos(projectRads)
			+ Math.cos(origLatRads) * Math.sin(projectRads) * Math.cos(bearingRads));
		double lon2 = origLonRads + Math.atan2(Math.sin(bearingRads) * Math.sin(projectRads) * Math.cos(origLatRads),
			Math.cos(projectRads) - Math.sin(origLatRads) * Math.sin(lat2));

		double finalLatDeg = Math.toDegrees(lat2);
		double finalLonDeg = Math.toDegrees(lon2);
		System.out.println("Result is: lat=" + finalLatDeg + ", lon=" + finalLonDeg);

		// Create point and append to track
		DataPoint point = new DataPoint(new Latitude(finalLatDeg, Coordinate.FORMAT_DEG),
			new Longitude(finalLonDeg, Coordinate.FORMAT_DEG), null);
		point.setFieldValue(Field.WAYPT_NAME, _nameField.getText(), false);
		_app.createPoint(point);

		_dialog.dispose();
	}
}
