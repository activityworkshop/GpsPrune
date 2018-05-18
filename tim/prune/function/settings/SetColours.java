package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.gui.colour.ColourChooser;
import tim.prune.gui.colour.ColourPatch;
import tim.prune.gui.colour.ColourerSelectorPanel;
import tim.prune.gui.colour.PatchListener;
import tim.prune.gui.colour.PointColourer;

/**
 * Class to show the popup window for setting the colours
 */
public class SetColours extends GenericFunction
{
	private JDialog _dialog = null;
	private JButton _okButton = null;
	/** Array of 8 colour patches */
	private ColourPatch[] _patches = null;
	/** colourer selection panel */
	private ColourerSelectorPanel _colourerSelector = null;
	/** Single colour chooser */
	private ColourChooser _colourChooser = null;

	/** Array of label keys for the 8 patches */
	private static final String[] LABEL_KEYS = {"background", "primary", "point", "secondary",
		"selection", "borders", "text", "lines"};
	/** Array of indices for the 8 patches */
	private static final int[] INDICES = {ColourScheme.IDX_BACKGROUND, ColourScheme.IDX_PRIMARY,
		ColourScheme.IDX_POINT, ColourScheme.IDX_SECONDARY,
		ColourScheme.IDX_SELECTION, ColourScheme.IDX_BORDERS,
		ColourScheme.IDX_TEXT, ColourScheme.IDX_LINES
	};


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetColours(App inApp)
	{
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey()
	{
		return "function.setcolours";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 10));

		JLabel introLabel = new JLabel(I18nManager.getText("dialog.setcolours.intro"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		mainPanel.add(introLabel, BorderLayout.NORTH);

		// Panel in centre, to hold both the patch panel and the colourer panel (and maybe introLabel too?)
		JPanel centralPanel = new JPanel();
		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));

		// Make panel for 8 colour patches
		JPanel patchPanel = new JPanel();
		patchPanel.setLayout(new GridLayout());
		_patches = new ColourPatch[8];

		ColourScheme scheme = Config.getColourScheme();
		ColourPatch patch = null;
		// Loop over four columns of patches
		for (int i=0; i<4; i++)
		{
			JPanel colPanel = new JPanel();
			colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));
			// Top label and patch
			colPanel.add(new JLabel(I18nManager.getText("dialog.setcolours." + LABEL_KEYS[i*2])));
			patch = new ColourPatch(scheme.getColour(INDICES[i*2]));
			patch.addMouseListener(new PatchListener(patch, _colourChooser));
			colPanel.add(patch);
			_patches[i*2] = patch;
			// separator
			colPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			// Bottom label and patch
			colPanel.add(new JLabel(I18nManager.getText("dialog.setcolours." + LABEL_KEYS[i*2+1])));
			patch = new ColourPatch(scheme.getColour(INDICES[i*2+1]));
			patch.addMouseListener(new PatchListener(patch, _colourChooser));
			colPanel.add(patch);
			_patches[i*2+1] = patch;

			// Add column to panel
			colPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			patchPanel.add(colPanel);
		}
		patchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centralPanel.add(patchPanel);
		centralPanel.add(Box.createVerticalStrut(15));

		// now the colourer selector
		_colourerSelector = new ColourerSelectorPanel(_colourChooser);
		_colourerSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
		centralPanel.add(_colourerSelector);
		// add the central panel to the main one
		mainPanel.add(centralPanel, BorderLayout.CENTER);

		// Buttons at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateConfigColours();
				_dialog.dispose();
			}
		});
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		JButton resetButton = new JButton(I18nManager.getText("button.resettodefaults"));
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i=0; i<8; i++) {
					_patches[i].setColour(ColourScheme.getDefaultColour(INDICES[i]));
				}
			}
		});
		buttonPanel.add(resetButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(_okButton);
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(cancelButton);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}


	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()));
			_dialog.setLocationRelativeTo(_parentFrame);
			_colourChooser = new ColourChooser(_dialog);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Reset colours to current ones
		ColourScheme scheme = Config.getColourScheme();
		for (int i=0; i<8; i++) {
			_patches[i].setColour(scheme.getColour(INDICES[i]));
		}
		PointColourer colourer = Config.getPointColourer();
		_colourerSelector.init(colourer, scheme.getColour(ColourScheme.IDX_POINT));
		_dialog.setVisible(true);
		_okButton.requestFocus();
	}

	/**
	 * Update the current colour scheme with the selected colours
	 */
	private void updateConfigColours()
	{
		ColourScheme scheme = Config.getColourScheme();
		for (int i=0; i<_patches.length; i++)
		{
			scheme.setColour(INDICES[i], _patches[i].getBackground());
		}
		Config.updateColourScheme();
		PointColourer colourer = _colourerSelector.getSelectedColourer();
		Config.updatePointColourer(colourer);
		_app.updatePointColourer();
		UpdateMessageBroker.informSubscribers();
	}
}
