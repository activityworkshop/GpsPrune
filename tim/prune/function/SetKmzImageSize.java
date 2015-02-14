package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.gui.WholeNumberField;

/**
 * Class to provide the function to set the image size for kmz output
 */
public class SetKmzImageSize extends GenericFunction
{
	private JDialog _dialog = null;
	private JButton _okButton = null;
	private WholeNumberField _widthField = null, _heightField = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public SetKmzImageSize(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.setkmzimagesize";
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
		// Initialise values from config
		_widthField.setValue(Config.getConfigInt(Config.KEY_KMZ_IMAGE_WIDTH));
		_heightField.setValue(Config.getConfigInt(Config.KEY_KMZ_IMAGE_HEIGHT));
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		// Make a central panel with the text boxes
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2));
		mainPanel.add(makeRightLabel("dialog.saveconfig.prune.kmzimagewidth"));
		_widthField = new WholeNumberField(4);
		mainPanel.add(_widthField);
		mainPanel.add(makeRightLabel("dialog.saveconfig.prune.kmzimageheight"));
		_heightField = new WholeNumberField(4);
		mainPanel.add(_heightField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent arg0) {
				_okButton.setEnabled(_widthField.getValue()>0 && _heightField.getValue()>0);
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent arg0) {
				_okButton.setEnabled(_widthField.getValue()>0 && _heightField.getValue()>0);
			};
		};
		_widthField.addKeyListener(keyListener);
		_heightField.addKeyListener(keyListener);
		_widthField.addMouseListener(mouseListener);
		_heightField.addMouseListener(mouseListener);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
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
	 * @param inKey text key
	 * @return right-aligned label
	 */
	private static final JLabel makeRightLabel(String inKey)
	{
		JLabel label = new JLabel(I18nManager.getText(inKey) + " : ");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		return label;
	}


	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		if (_widthField.getValue() > 0 && _heightField.getValue() > 0)
		{
			// Set entered values in Config
			Config.setConfigInt(Config.KEY_KMZ_IMAGE_WIDTH, _widthField.getValue());
			Config.setConfigInt(Config.KEY_KMZ_IMAGE_HEIGHT, _heightField.getValue());
			_dialog.dispose();
		}
	}
}
