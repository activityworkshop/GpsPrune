package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tim.prune.I18nManager;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.gui.map.OsmMapSource;

/**
 * Class to handle the adding of a new map source
 */
public class AddMapSourceDialog
{
	private final SetMapBgFunction _parent;
	private final JDialog _addDialog;
	private MapSource _originalSource = null;
	// controls for osm panel
	private JTextField _oNameField = null;
	private JTextField _baseUrlField = null, _topUrlField = null;
	private JRadioButton[] _baseTypeRadios = null, _topTypeRadios = null;
	private JComboBox<Integer> _oZoomCombo = null;
	private JButton _okButton = null;

	/** array of file types */
	private static final String[] FILE_TYPES = {"png", "jpg", "gif"};


	/**
	 * Constructor
	 * @param inParentDialog parent dialog
	 * @param inParentFunction parent function used for callbacks
	 */
	public AddMapSourceDialog(JDialog inParentDialog, SetMapBgFunction inParentFunction)
	{
		_parent = inParentFunction;
		_addDialog = new JDialog(inParentDialog, I18nManager.getText("dialog.addmapsource.title"), true);
		_addDialog.add(makeDialogComponents());
		_addDialog.setLocationRelativeTo(inParentDialog);
		_addDialog.pack();
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		// Top panel with spacer
		dialogPanel.add(new JLabel(" "), BorderLayout.NORTH);

		// listener
		KeyAdapter keyListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_addDialog.dispose();
				}
				else {
					enableOK();
				}
			}
		};
		// Listener for any gui changes (to enable ok when anything changes on an edit)
		ActionListener okEnabler = arg0 -> enableOK();

		// openstreetmap panel
		JPanel osmPanel = new JPanel();
		osmPanel.setLayout(new BorderLayout());
		osmPanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 4, 3));
		JPanel gbPanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		gbPanel.setLayout(gridbag);
		c.gridx = 0; c.gridy = 0;
		c.gridheight = 1; c.gridwidth = 1;
		c.weightx = 0.0; c.weighty = 0.0;
		c.ipadx = 3; c.ipady = 5;
		c.insets = new Insets(0, 0, 5, 0);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		gbPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.sourcename")), c);
		_oNameField = new JTextField();
		_oNameField.addKeyListener(keyListener);
		c.gridx = 1; c.weightx = 1.0;
		gbPanel.add(_oNameField, c);
		// Base layer
		c.gridx = 0; c.gridy = 1;
		c.weightx = 0.0;
		c.insets = new Insets(0, 0, 0, 0);
		gbPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.layer1url")), c);
		_baseUrlField = new JTextField(25);
		_baseUrlField.addKeyListener(keyListener);
		c.gridx = 1; c.weightx = 1.0;
		gbPanel.add(_baseUrlField, c);
		_baseTypeRadios = new JRadioButton[3];
		ButtonGroup radioGroup = new ButtonGroup();
		for (int i=0; i<3; i++)
		{
			_baseTypeRadios[i] = new JRadioButton(FILE_TYPES[i]);
			radioGroup.add(_baseTypeRadios[i]);
			c.gridx = 2+i; c.weightx = 0.0;
			gbPanel.add(_baseTypeRadios[i], c);
			// Each type radio needs listener to call enableOk()
			_baseTypeRadios[i].addActionListener(okEnabler);
		}

		// Top layer
		c.gridx = 0; c.gridy = 2;
		gbPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.layer2url")), c);
		_topUrlField = new JTextField();
		_topUrlField.addKeyListener(keyListener);
		c.gridx = 1; c.weightx = 1.0;
		gbPanel.add(_topUrlField, c);
		_topTypeRadios = new JRadioButton[3];
		radioGroup = new ButtonGroup();
		for (int i=0; i<3; i++)
		{
			_topTypeRadios[i] = new JRadioButton(FILE_TYPES[i]);
			radioGroup.add(_topTypeRadios[i]);
			c.gridx = 2+i; c.weightx = 0.0;
			gbPanel.add(_topTypeRadios[i], c);
			// Each type radio needs listener to call enableOk()
			_topTypeRadios[i].addActionListener(okEnabler);
		}
		// Max zoom
		c.gridx = 0; c.gridy = 3;
		c.fill = GridBagConstraints.NONE;
		gbPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.maxzoom")), c);
		_oZoomCombo = new JComboBox<>();
		for (int i=10; i<=20; i++) {
			_oZoomCombo.addItem(i);
		}
		// zoom dropdown needs listener to call enableOk()
		_oZoomCombo.addActionListener(okEnabler);
		c.gridx = 1;
		gbPanel.add(_oZoomCombo, c);
		osmPanel.add(gbPanel, BorderLayout.NORTH);

		// cards
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout());
		holderPanel.add(osmPanel, BorderLayout.NORTH);
		dialogPanel.add(holderPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> finish());
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _addDialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Init and show the dialog
	 * @param inSource source object before edit, or null to add
	 */
	public void showDialog(MapSource inSource)
	{
		_originalSource = inSource;
		populateFields();
	}

	/**
	 * Clear all the dialog fields to prepare for an add
	 */
	private void clearAllFields()
	{
		_oNameField.setText("");
		_baseUrlField.setText("");
		_baseTypeRadios[0].setSelected(true);
		_topUrlField.setText("");
		_topTypeRadios[0].setSelected(true);
		_oZoomCombo.setSelectedIndex(8);
	}

	/**
	 * Init the dialog fields from the given source object
	 */
	private void populateFields()
	{
		if (_originalSource == null)
		{
			clearAllFields();
		}
		else
		{
			_oNameField.setText(_originalSource.getName());
			_baseUrlField.setText(_originalSource.getBaseUrl(0));
			int baseType = getBaseType(_originalSource.getFileExtension(0));
			_baseTypeRadios[baseType].setSelected(true);
			_topUrlField.setText(_originalSource.getNumLayers()==1 ? "" : _originalSource.getBaseUrl(1));
			int topType = getBaseType(_originalSource.getFileExtension(1));
			_topTypeRadios[topType].setSelected(true);
			_oZoomCombo.setSelectedIndex(getZoomIndex(_originalSource.getMaxZoomLevel()));
		}

		_okButton.setEnabled(false);
		_addDialog.setVisible(true);
	}

	/**
	 * Check the currently entered details and enable the OK button if it looks OK
	 */
	private void enableOK()	{
		_okButton.setEnabled(isOsmPanelOk());
	}

	/**
	 * Check the openstreetmap panel if all details are complete
	 * @return true if details look ok
	 */
	private boolean isOsmPanelOk()
	{
		boolean ok = _oNameField.getText().trim().length() > 1;
		// Try to parse base url if given
		String baseText = _baseUrlField.getText().trim();
		final String baseUrl = MapSource.fixBaseUrl(baseText);
		if (baseText.length() > 0 && baseUrl == null) {ok = false;}
		// Same again for top url if given
		String topText = _topUrlField.getText().trim();
		final String topUrl = MapSource.fixBaseUrl(topText);
		if (topText.length() > 0 && topUrl == null) {ok = false;}
		// looks ok if at least one url given
		return (ok && (baseUrl != null || topUrl != null));
	}

	/**
	 * Finish by adding the requested source and refreshing the parent
	 */
	private void finish()
	{
		MapSource newSource = null;
		String origName = (_originalSource == null ? null : _originalSource.getName());

		if (isOsmPanelOk())
		{
			// Openstreetmap source
			String sourceName = getValidSourcename(_oNameField.getText(), origName);
			String url1 = _baseUrlField.getText().trim();
			String ext1 = getFileExtension(_baseTypeRadios);
			String url2 = _topUrlField.getText().trim();
			String ext2 = getFileExtension(_topTypeRadios);
			newSource = new OsmMapSource(sourceName, url1, ext1, url2, ext2, _oZoomCombo.getSelectedIndex()+10);
		}

		// Add new source if ok
		if (newSource != null)
		{
			if (_originalSource == null) {
				MapSourceLibrary.addSource(newSource);
			}
			else {
				MapSourceLibrary.editSource(_originalSource, newSource);
			}
			// inform setmapbg dialog
			_parent.updateList();
			_addDialog.setVisible(false);
		}
	}

	/**
	 * Check the given source name is valid and whether it exists in library already
	 * @param inName name to check
	 * @param inOriginalName name of source before edit (or null for new source)
	 * @return valid name for the new source
	 */
	private static String getValidSourcename(String inName, String inOriginalName)
	{
		String name = inName;
		if (name == null) {name = "";}
		else {name = name.trim();}
		if (name.equals("")) {
			name = I18nManager.getText("dialog.addmapsource.noname");
		}
		// Check there isn't already a map source with this name
		if (inOriginalName == null || !inOriginalName.equals(name))
		{
			if (MapSourceLibrary.hasSourceName(name))
			{
				int suffix = 1;
				while (MapSourceLibrary.hasSourceName(name + suffix)) {
					suffix++;
				}
				name += suffix;
			}
		}
		return name;
	}

	/**
	 * Get the selected file extension
	 * @param inRadios array of radio buttons for selection
	 * @return selected file extension
	 */
	private String getFileExtension(JRadioButton[] inRadios)
	{
		if (inRadios != null)
		{
			for (int i=0; i<inRadios.length; i++) {
				if (inRadios[i] != null && inRadios[i].isSelected()) {
					return FILE_TYPES[i];
				}
			}
		}
		return FILE_TYPES[0];
	}

	/**
	 * Get the index of the given image extension
	 * @param inExt file extension, such as "png"
	 * @return index from 0 to 2
	 */
	private static int getBaseType(String inExt)
	{
		for (int i=0; i<FILE_TYPES.length; i++) {
			if (FILE_TYPES[i].equals(inExt)) {
				return i;
			}
		}
		// Not found so default to png
		return 0;
	}

	/**
	 * Get the dropdown index of the given zoom level
	 * @param inZoomLevel zoom level, eg 18
	 * @return index of dropdown to select
	 */
	private static int getZoomIndex(int inZoomLevel)
	{
		return Math.max(0, inZoomLevel - 10);
	}
}
