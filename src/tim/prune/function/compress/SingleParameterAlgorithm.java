package tim.prune.function.compress;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Superclass for compression algorithms with a single text field parameter
 */
public abstract class SingleParameterAlgorithm extends CompressionAlgorithm
{
	/** Text field for entering parameter */
	private JTextField _parameterField = null;
	/** Listener from parent dialog */
	private ActionListener _listener = null;


	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inDetails track details object
	 * @param inListener listener to attach to activation control
	 */
	public SingleParameterAlgorithm(Track inTrack, TrackDetails inDetails, ActionListener inListener)
	{
		super(inTrack, inDetails, inListener);
		_listener = inListener;
		_parameterField = new JTextField();
		// Add listener to parameter field to re-run preview (and en/disable ok) when param changed
		_parameterField.addKeyListener(new KeyListener() {
			public void keyTyped(java.awt.event.KeyEvent arg0) {};
			public void keyPressed(java.awt.event.KeyEvent arg0) {};
			public void keyReleased(java.awt.event.KeyEvent arg0) {if (isActivated()) _listener.actionPerformed(null);};
		});
	}

	/**
	 * @return specific gui components for dialog
	 * @param inLabelKey key for label
	 * @param inParamValue initial value of parameter
	 */
	protected Component getSpecificGuiComponents(String inLabelKey, String inParamValue)
	{
		// Create panel with label and text field
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		JLabel label = new JLabel(I18nManager.getText(inLabelKey) + " : ");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(label);
		panel.add(_parameterField);
		_parameterField.setText(inParamValue);
		return panel;
	}

	/**
	 * Parse the text field to get parameter
	 * @return parameter given as double
	 */
	protected double getParameter()
	{
		double param = 0.0;
		try {
			// Parse from string
			param = Double.parseDouble(_parameterField.getText());
		}
		catch (NumberFormatException nfe) {} // ignore, param stays zero
		return param;
	}

	/**
	 * @return the text field component
	 */
	protected JTextField getTextField()
	{
		return _parameterField;
	}
}
