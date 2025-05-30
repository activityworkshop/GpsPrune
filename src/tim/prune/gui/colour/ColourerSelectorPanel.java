package tim.prune.gui.colour;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import tim.prune.I18nManager;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.RadioButtonGroup;
import tim.prune.gui.colour.ColourerFactory.ColourerId;

/**
 * Class to provide a gui panel for selecting a colourer
 * including which colourer, the start/end colours and
 * optionally the max number of colours
 */
public class ColourerSelectorPanel extends JPanel
{
	/** Combo box for selecting the type of colourer */
	private JComboBox<String> _typeCombo = null;
	/** Array of type ids as stored in combo box */
	private final ColourerId[] _typeIds;
	/** Panel object holding the colour patches */
	private JPanel _patchPanel = null;
	/** Array of colour patches for start and end */
	private ColourPatch[] _startEndPatches = null;
	/** Panel holding the max colours selection */
	private JPanel _maxColoursPanel = null;
	private JComboBox<String> _maxColoursCombo = null;
	/** Panel holding the hue interpolation mode */
	private JPanel _wideHuesPanel = null;
	/** Radio buttons to select wide hues */
	private JRadioButton _narrowHueScalingRadio = null;
	private JRadioButton _wideHueScalingRadio = null;
	/** Panel holding the preview */
	private JPanel _previewPanel = null;
	private final ColourerPreview _colourerPreview = new ColourerPreview();

	/** Array of label keys for the 2 patches */
	private static final String[] LABEL_KEYS = {"start", "end"};


	/**
	 * Constructor
	 * @param inColourChooser colour chooser to use (needs reference to parent dialog)
	 */
	public ColourerSelectorPanel(ColourChooser inColourChooser)
	{
		_typeIds = new ColourerId[] {ColourerId.NONE, ColourerId.BY_FILE,
			ColourerId.BY_SEGMENT, ColourerId.BY_DATE, ColourerId.BY_ALTITUDE,
			ColourerId.BY_SPEED, ColourerId.BY_VSPEED, ColourerId.BY_GRADIENT};
		makeGuiComponents(inColourChooser);
	}


	/**
	 * Create all the gui components and lay them out in the panel
	 * @param inColourChooser colour chooser to use
	 */
	private void makeGuiComponents(ColourChooser inColourChooser)
	{
		// Etched border and vertical layout
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Label at the top
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.colourer.intro"));
		introLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(introLabel);

		// Combo box for selecting which colourer to use
		JPanel typePanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(typePanel);
		final String keyPrefix = "dialog.colourer.type.";
		String[] colourerTypes = new String[_typeIds.length];
		for (int i=0; i<colourerTypes.length; i++)
		{
			colourerTypes[i] = I18nManager.getText(keyPrefix +
				ColourerFactory.getDescriptionKey(_typeIds[i]));
		}
		_typeCombo = new JComboBox<>(colourerTypes);
		_typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
		_typeCombo.addActionListener(e -> onColourerTypeChanged());
		// Add to the panel
		grid.add(new JLabel(I18nManager.getText("dialog.colourer.type")));
		grid.add(_typeCombo);
		typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typePanel);

		// Make panel for colour patches
		_patchPanel = new JPanel();
		_patchPanel.setLayout(new GridLayout());
		_startEndPatches = new ColourPatch[2];

		// Blank column
		JPanel blankColumn = new JPanel();
		ColourPatch blankPatch = new ColourPatch(Color.BLACK);
		blankPatch.setVisible(false);
		blankColumn.add(blankPatch);
		_patchPanel.add(blankColumn);

		// Loop over two columns of patches
		for (int i=0; i<2; i++)
		{
			JPanel colPanel = new JPanel();
			colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));
			// Top label and patch
			colPanel.add(new JLabel(I18nManager.getText("dialog.colourer." + LABEL_KEYS[i])));
			ColourPatch patch = new ColourPatch(Color.BLUE, this::updatePreview);
			patch.addMouseListener(new PatchListener(patch, inColourChooser));
			colPanel.add(patch);
			_startEndPatches[i] = patch;

			// Add column to panel
			colPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			_patchPanel.add(colPanel);
		}

		// Blank column
		blankColumn = new JPanel();
		blankPatch = new ColourPatch(Color.BLACK);
		blankPatch.setVisible(false);
		blankColumn.add(blankPatch);
		_patchPanel.add(blankColumn);

		_patchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(_patchPanel);

		// Combo box for selecting max colours
		_maxColoursPanel = new JPanel();
		grid = new GuiGridLayout(_maxColoursPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.colourer.maxcolours")));
		String[] colourOptions = new String[] {"2", "3", "5", "10", "15"};
		_maxColoursCombo = new JComboBox<>(colourOptions);
		_maxColoursCombo.addActionListener(e -> updatePreview());
		grid.add(_maxColoursCombo);
		_maxColoursPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(_maxColoursPanel);

		// Radios for narrow/wide hues
		_wideHuesPanel = new JPanel();
		grid = new GuiGridLayout(_wideHuesPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.colourer.huesmode")));
		JPanel radiosPanel = new JPanel();
		radiosPanel.setLayout(new FlowLayout());
		_narrowHueScalingRadio = new JRadioButton(I18nManager.getText("dialog.colourer.huesmode.narrow"));
		_wideHueScalingRadio = new JRadioButton(I18nManager.getText("dialog.colourer.huesmode.wide"));
		radiosPanel.add(_narrowHueScalingRadio);
		radiosPanel.add(_wideHueScalingRadio);
		_narrowHueScalingRadio.addActionListener(e -> updatePreview());
		_wideHueScalingRadio.addActionListener(e -> updatePreview());
		new RadioButtonGroup(_narrowHueScalingRadio, _wideHueScalingRadio);
		grid.add(radiosPanel);
		_wideHuesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(_wideHuesPanel);

		// Preview
		_previewPanel = new JPanel();
		_previewPanel.setLayout(new BoxLayout(_previewPanel, BoxLayout.Y_AXIS));
		_previewPanel.add(new JLabel("Preview:"));
		_previewPanel.add(_colourerPreview);
		add(_previewPanel);
	}

	private void updatePreview() {
		_colourerPreview.setColourer(getSelectedColourer());
	}

	/**
	 * Init the colours from the colourer (if possible) or the default colour
	 * @param inColourer current colourer object, or null
	 * @param inDefaultColour current colour for points
	 */
	public void init(PointColourer inColourer, Color inDefaultColour)
	{
		Color startColour = null, endColour = null;
		if (inColourer != null)
		{
			selectColourerType(ColourerFactory.getId(inColourer));
			startColour = inColourer.getStartColour();
			endColour   = inColourer.getEndColour();
			_maxColoursCombo.setSelectedItem("" + inColourer.getMaxColours());
			_narrowHueScalingRadio.setSelected(!inColourer.isWideHueScaling());
			_wideHueScalingRadio.setSelected(inColourer.isWideHueScaling());
		}
		else
		{
			// no colourer, so default to 5 colours maximum
			_maxColoursCombo.setSelectedIndex(2);
			_narrowHueScalingRadio.setSelected(true);
		}
		if (startColour == null) {startColour = inDefaultColour;}
		if (endColour   == null) {endColour = makeDefaultEndColour(inDefaultColour);}
		_startEndPatches[0].setColour(startColour);
		_startEndPatches[1].setColour(endColour);
		onColourerTypeChanged(); // make sure gui is updated
	}

	/**
	 * Make a default end colour if there isn't one already defined
	 * @param inStartColour start colour
	 * @return end colour, with the hue shifted by a third from the start
	 */
	private static Color makeDefaultEndColour(Color inStartColour)
	{
		float[] defaultHSB = Color.RGBtoHSB(inStartColour.getRed(), inStartColour.getGreen(), inStartColour.getBlue(), null);
		// add 180 degrees to the hue
		return Color.getHSBColor(defaultHSB[0] + 0.5f, defaultHSB[1], defaultHSB[2]);
	}

	/**
	 * React to the colourer type being changed
	 * by showing / hiding gui elements
	 */
	private void onColourerTypeChanged()
	{
		PointColourer colourer = getSelectedColourer();
		_patchPanel.setVisible(colourer != null);
		_maxColoursPanel.setVisible(colourer != null && colourer.isDiscrete());
		_wideHuesPanel.setVisible(colourer != null);
		_previewPanel.setVisible(colourer != null);
		updatePreview();
	}

	/**
	 * @return the selected colourer object, or null
	 */
	public PointColourer getSelectedColourer()
	{
		final ColourerId id = _typeIds[_typeCombo.getSelectedIndex()];
		final Object selectedItem = _maxColoursCombo.getSelectedItem();
		final String selectedString = (selectedItem == null ? "2" : selectedItem.toString());
		PointColourer result = ColourerFactory.createColourer(id, _startEndPatches[0].getBackground(),
			_startEndPatches[1].getBackground(), selectedString, _wideHueScalingRadio.isSelected());
		return result;
	}

	/**
	 * Select the appropriate item in the dropdown
	 * @param inId id of colourer to choose
	 */
	private void selectColourerType(ColourerId inId)
	{
		int selIndex = -1;
		for (int i=0; i<_typeIds.length; i++)
		{
			if (_typeIds[i] == inId) {
				selIndex = i;
				break;
			}
		}
		if (selIndex < 0) {
			System.err.println("Id " + inId + " not found in _typeIds!");
		}
		else {
			_typeCombo.setSelectedIndex(selIndex);
		}
	}
}
