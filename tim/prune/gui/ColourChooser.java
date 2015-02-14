package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tim.prune.I18nManager;

/**
 * Class to offer a dialog to choose a colour.
 * Normally a JColorChooser would be used, but this is too buggy
 * in Java 1.6 and extremely prone to thread-locking, meaning
 * that the application has to be killed (and all data lost).
 */
public class ColourChooser
{
	/** main dialog object */
	private JDialog _dialog = null;
	/** array of three slider objects for rgb */
	private JSlider[] _rgbSliders = null;
	/** array of labels for rgb values */
	private JLabel[] _rgbLabels = null;
	/** colour patch */
	private ColourPatch _patch = null;
	/** chosen colour */
	private Color _chosenColour = null;


	/**
	 * Constructor
	 * @param inParent parent dialog
	 */
	public ColourChooser(JDialog inParent)
	{
		_dialog = new JDialog(inParent, I18nManager.getText("dialog.colourchooser.title"), true);
		_dialog.setLocationRelativeTo(inParent);
		_dialog.getContentPane().add(makeContents());
		_dialog.pack();
	}

	/**
	 * Make the dialog contents
	 * @return JPanel containing dialog elements
	 */
	private JPanel makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		_rgbSliders = new JSlider[3];
		_rgbLabels = new JLabel[3];
		_patch = new ColourPatch(Color.WHITE);
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BorderLayout());
		centrePanel.add(_patch, BorderLayout.CENTER);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		final String[] labelKeys = {"red", "green", "blue"};
		for (int i=0; i<3; i++)
		{
			String key = I18nManager.getText("dialog.colourchooser." + labelKeys[i]);
			sliderPanel.add(new JLabel(key));
			_rgbSliders[i] = new JSlider(0, 255);
			_rgbSliders[i].addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					updatePatch();
				}
			});
			_rgbSliders[i].setToolTipText(key);
			JPanel sliderHolder = new JPanel();
			sliderHolder.setLayout(new BorderLayout(5, 0));
			sliderHolder.add(_rgbSliders[i], BorderLayout.CENTER);
			_rgbLabels[i] = new JLabel("255");
			_rgbLabels[i].setPreferredSize(new Dimension(40, 1));
			sliderHolder.add(_rgbLabels[i], BorderLayout.EAST);
			sliderPanel.add(sliderHolder);
		}
		centrePanel.add(sliderPanel, BorderLayout.SOUTH);
		mainPanel.add(centrePanel, BorderLayout.CENTER);

		// Button panel for ok, cancel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_chosenColour = _patch.getBackground();
				_dialog.setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_chosenColour = null;
				_dialog.setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * Show the dialog to choose a colour
	 * @param inStartColour current colour
	 */
	public void showDialog(Color inStartColour)
	{
		// Initialise sliders
		_rgbSliders[0].setValue(inStartColour.getRed());
		_rgbSliders[1].setValue(inStartColour.getGreen());
		_rgbSliders[2].setValue(inStartColour.getBlue());
		updatePatch();
		_dialog.setLocationRelativeTo(_dialog.getParent());
		_dialog.setVisible(true);
	}

	/**
	 * Update the patch colour from the slider values
	 */
	private void updatePatch()
	{
		for (int i=0; i<3; i++) {
			_rgbLabels[i].setText("" + _rgbSliders[i].getValue());
		}
		final Color colour = new Color(_rgbSliders[0].getValue(),
			_rgbSliders[1].getValue(),_rgbSliders[2].getValue());
		_patch.setColour(colour);
	}

	/**
	 * @return the selected colour, or null if cancel pressed
	 */
	public Color getChosenColour()
	{
		return _chosenColour;
	}
}
