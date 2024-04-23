package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.function.olc.OlcArea;
import tim.prune.function.olc.OlcDecoder;
import tim.prune.function.olc.OlcField;
import tim.prune.gui.GuiGridLayout;

/**
 * Class to provide the function to parse
 * OpenLocationCodes, or PlusCodes
 */
public class PlusCodeFunction extends GenericFunction
{
	private JDialog _dialog = null;
	private OlcField _codeField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public PlusCodeFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.enterpluscode";
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
		// MAYBE: Paste clipboard into the edit field
		_codeField.setText("");
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
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.pluscode.desc")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);
		_codeField = new OlcField();
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
		_codeField.addKeyListener(keyListener);
		_codeField.addMouseListener(mouseListener);
		JLabel codeLabel = new JLabel(I18nManager.getText("dialog.pluscode.code"));
		codeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(codeLabel);
		grid.add(_codeField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> {
			if (_okButton.isEnabled()) {finish();}
		};
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		_codeField.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Enable or disable the OK button based on the contents of the text field
	 */
	private void enableOK()
	{
		String text = _codeField.getText().trim();
		_okButton.setEnabled(OlcDecoder.isValidLongForm(text) || OlcDecoder.isValidShortForm(text));
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		OlcArea area = OlcDecoder.decode(_codeField.getText(), _app.getViewport().getCentreLatitude(),
			_app.getViewport().getCentreLongitude());

		if (area == null)
		{
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("dialog.pluscode.nothingfound"),
				getName(), JOptionPane.ERROR_MESSAGE);
		}
		else if (loadTrack(area))
		{
			_dialog.dispose();
		}
	}

	/**
	 * Load the generated points from the given area
	 * @param inArea rectangular area
	 * @return true on success
	 */
	private boolean loadTrack(OlcArea inArea)
	{
		if (inArea == null) {
			return false;
		}

		ArrayList<DataPoint> points = new ArrayList<>();
		if (inArea.minLat == inArea.maxLat && inArea.minLon == inArea.maxLon)
		{
			// Not actually an area, just a single point
			DataPoint point = new DataPoint(Latitude.make(inArea.minLat), Longitude.make(inArea.minLon));
			point.setWaypointName(inArea.code);
			points.add(point);
		}
		else
		{
			for (int i=0; i<5; i++)
			{
				Coordinate lat = Latitude.make((i%4==0 || i==3) ? inArea.minLat : inArea.maxLat);
				Coordinate lon = Longitude.make((i%4==0 || i==1) ? inArea.minLon : inArea.maxLon);
				DataPoint point = new DataPoint(lat, lon);
				point.setSegmentStart(i==0);
				points.add(point);
			}
			// Middle point with name
			Coordinate lat = Latitude.make((inArea.minLat + inArea.maxLat) / 2.0);
			Coordinate lon = Longitude.make((inArea.minLon + inArea.maxLon) / 2.0);
			DataPoint point = new DataPoint(lat, lon);
			point.setWaypointName(inArea.code);
			points.add(point);
		}
		// Make and execute the command
		AppendRangeCmd command = new AppendRangeCmd(points);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsadded", points.size()));
		return _app.execute(command);
	}
}
