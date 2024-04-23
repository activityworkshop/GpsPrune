package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.GuiGridLayout;


/**
 * Class to provide the function to project the current point
 * to a circle at a given distance
 */
public class ProjectCircle extends GenericFunction
{
	private JDialog _dialog = null;
	private JLabel _distanceDescLabel = null;
	private DecimalNumberField _distanceField = null;
	private boolean _distanceIsMetric = true;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public ProjectCircle(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.projectcircle";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
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
		_distanceField.setText("");
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
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.projectcircle.desc")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);
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
		_distanceField.addKeyListener(keyListener);
		_distanceField.addMouseListener(mouseListener);

		// Distance including units
		_distanceDescLabel = new JLabel(I18nManager.getText("fieldname.distance") + " (ft)");
		// Note, this label will be reset at each run
		_distanceDescLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(_distanceDescLabel);
		grid.add(_distanceField);

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
		Unit distUnit = getConfig().getUnitSet().getDistanceUnit();
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
		final boolean distanceOk = _distanceField.getValue() > 0.0;
		_okButton.setEnabled(distanceOk);
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

		final int NUM_POINTS_IN_CIRCLE = 24;
		ArrayList<DataPoint> points = new ArrayList<>();
		for (int pointNum=0; pointNum<=NUM_POINTS_IN_CIRCLE; pointNum++)
		{
			final double bearingRads = (pointNum % NUM_POINTS_IN_CIRCLE) * 2.0 * Math.PI / NUM_POINTS_IN_CIRCLE;

			double lat2 = Math.asin(Math.sin(origLatRads) * Math.cos(projectRads)
				+ Math.cos(origLatRads) * Math.sin(projectRads) * Math.cos(bearingRads));
			double lon2 = origLonRads + Math.atan2(Math.sin(bearingRads) * Math.sin(projectRads) * Math.cos(origLatRads),
				Math.cos(projectRads) - Math.sin(origLatRads) * Math.sin(lat2));

			// Create point and append to track
			DataPoint point = new DataPoint(Latitude.make(Math.toDegrees(lat2)), Longitude.make(Math.toDegrees(lon2)));
			point.setSegmentStart(pointNum == 0);
			points.add(point);
		}

		// give data to App
		AppendRangeCmd command = new AppendRangeCmd(points);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsadded", points.size()));
		_app.execute(command);
		_dialog.dispose();
	}
}
