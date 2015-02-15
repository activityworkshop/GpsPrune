package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
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
import tim.prune.gui.map.CloudmadeMapSource;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.gui.map.OsmMapSource;

/**
 * Class to handle the adding of a new map source
 */
public class AddMapSourceDialog
{
	private SetMapBgFunction _parent = null;
	private JDialog _addDialog = null;
	private JRadioButton[] _typeRadios = null;
	private JPanel _cards = null;
	// controls for osm panel
	private JTextField _oNameField = null;
	private JTextField _baseUrlField = null, _topUrlField = null;
	private JComboBox _oZoomCombo = null;
	// controls for cloudmade panel
	private JTextField _cNameField = null;
	private JTextField _cStyleField = null;
	private JComboBox _cZoomCombo = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inParent parent dialog
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
		// Top panel with two radio buttons to select source type
		JPanel radioPanel = new JPanel();
		ButtonGroup radioGroup = new ButtonGroup();
		radioPanel.setLayout(new GridLayout(1, 0));
		_typeRadios = new JRadioButton[2];
		_typeRadios[0] = new JRadioButton("Openstreetmap");
		radioGroup.add(_typeRadios[0]);
		radioPanel.add(_typeRadios[0]);
		_typeRadios[1] = new JRadioButton("Cloudmade");
		radioGroup.add(_typeRadios[1]);
		radioPanel.add(_typeRadios[1]);
		_typeRadios[0].setSelected(true);
		// listener for clicks on type radios
		ActionListener typeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CardLayout cl = (CardLayout) _cards.getLayout();
				if (_typeRadios[0].isSelected()) {cl.first(_cards);}
				else {cl.last(_cards);}
				enableOK();
			}
		};
		_typeRadios[0].addActionListener(typeListener);
		_typeRadios[1].addActionListener(typeListener);
		radioPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialogPanel.add(radioPanel, BorderLayout.NORTH);

		_cards = new JPanel();
		_cards.setLayout(new CardLayout());
		// listener
		KeyAdapter keyListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				enableOK();
			}
		};
		// openstreetmap panel
		JPanel osmPanel = new JPanel();
		osmPanel.setLayout(new GridLayout(0, 2, 5, 5));
		osmPanel.setBorder(BorderFactory.createEmptyBorder(6, 2, 4, 2));
		osmPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.sourcename")));
		_oNameField = new JTextField(18);
		_oNameField.addKeyListener(keyListener);
		osmPanel.add(_oNameField);
		// Base layer
		osmPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.layer1url")));
		_baseUrlField = new JTextField(18);
		_baseUrlField.addKeyListener(keyListener);
		osmPanel.add(_baseUrlField);
		// Top layer
		osmPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.layer2url")));
		_topUrlField = new JTextField(18);
		_topUrlField.addKeyListener(keyListener);
		osmPanel.add(_topUrlField);
		// Max zoom
		osmPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.maxzoom")));
		_oZoomCombo = new JComboBox();
		for (int i=10; i<=20; i++) {
			_oZoomCombo.addItem("" + i);
		}
		osmPanel.add(_oZoomCombo);
		_cards.add(osmPanel, "card1");
		// Panel for cloudmade source
		JPanel cloudPanel = new JPanel();
		cloudPanel.setLayout(new GridLayout(0, 2, 5, 5));
		cloudPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.sourcename")));
		_cNameField = new JTextField(18);
		_cNameField.addKeyListener(keyListener);
		cloudPanel.add(_cNameField);
		cloudPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.cloudstyle")));
		_cStyleField = new JTextField(18);
		_cStyleField.addKeyListener(keyListener);
		cloudPanel.add(_cStyleField);
		cloudPanel.add(new JLabel(I18nManager.getText("dialog.addmapsource.maxzoom")));
		_cZoomCombo = new JComboBox();
		for (int i=10; i<=20; i++) {
			_cZoomCombo.addItem("" + i);
		}
		cloudPanel.add(_cZoomCombo);
		cloudPanel.add(new JLabel(" ")); // force four rows to space text boxes properly
		_cards.add(cloudPanel, "card2");
		// cards
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout());
		holderPanel.add(_cards, BorderLayout.NORTH);
		dialogPanel.add(holderPanel, BorderLayout.CENTER);

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
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_addDialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Init and show the dialog
	 */
	public void showDialog()
	{
		_oNameField.setText("");
		_baseUrlField.setText("");
		_topUrlField.setText("");
		_oZoomCombo.setSelectedIndex(8);
		_cNameField.setText("");
		_cStyleField.setText("");
		_cZoomCombo.setSelectedIndex(8);
		_okButton.setEnabled(false);
		_addDialog.setVisible(true);
	}


	/**
	 * Check the currently entered details and enable the OK button if it looks OK
	 */
	private void enableOK()
	{
		boolean ok = false;
		if (_typeRadios[0].isSelected()) {ok = isOsmPanelOk();}
		if (_typeRadios[1].isSelected()) {ok = isCloudPanelOk();}
		_okButton.setEnabled(ok);
	}

	/**
	 * Check the openstreetmap panel if all details are complete
	 * @return true if details look ok
	 */
	private boolean isOsmPanelOk()
	{
		boolean ok = _oNameField.getText().trim().length() > 1;
		String baseUrl = null, topUrl = null;
		// Try to parse base url if given
		String baseText = _baseUrlField.getText().trim();
		baseUrl = MapSource.fixBaseUrl(baseText);
		if (baseText.length() > 0 && baseUrl == null) {ok = false;}
		// Same again for top url if given
		String topText = _topUrlField.getText().trim();
		topUrl = MapSource.fixBaseUrl(topText);
		if (topText.length() > 0 && topUrl == null) {ok = false;}
		// looks ok if at least one url given
		return (ok && (baseUrl != null || topUrl != null));
	}

	/**
	 * Check the cloudmade panel if all details are complete
	 * @return true if details look ok
	 */
	private boolean isCloudPanelOk()
	{
		boolean ok = _cNameField.getText().trim().length() > 1;
		int styleNum = 0;
		try {
			styleNum = Integer.parseInt(_cStyleField.getText());
		}
		catch (NumberFormatException nfe) {}
		return (ok && styleNum > 0);
	}

	/**
	 * Finish by adding the requested source and refreshing the parent
	 */
	private void finish()
	{
		MapSource newSource = null;
		if (_typeRadios[0].isSelected())
		{
			// Openstreetmap source
			String sourceName = getUniqueSourcename(_oNameField.getText());
			String url1 = _baseUrlField.getText().trim();
			String url2 = _topUrlField.getText().trim();
			newSource = new OsmMapSource(sourceName, url1, url2, _oZoomCombo.getSelectedIndex()+10);
		}
		else if (_typeRadios[1].isSelected())
		{
			String sourceName = getUniqueSourcename(_cNameField.getText());
			newSource = new CloudmadeMapSource(sourceName, _cStyleField.getText(),
				_cZoomCombo.getSelectedIndex()+10);
		}
		// Add new source if ok
		if (newSource != null)
		{
			MapSourceLibrary.addSource(newSource);
			// inform setmapbg dialog
			_parent.updateList();
			_addDialog.setVisible(false);
		}
	}

	/**
	 * Check the given source name if it exists in library already
	 * @param inName name to check
	 * @return unique name not yet in library
	 */
	private static String getUniqueSourcename(String inName)
	{
		String name = inName;
		if (name == null) {name = "";}
		else {name = name.trim();}
		if (name.equals("")) {
			name = I18nManager.getText("dialog.addmapsource.noname");
		}
		// Check there isn't already a map source with this name
		if (MapSourceLibrary.hasSourceName(name))
		{
			int suffix = 1;
			while (MapSourceLibrary.hasSourceName(name + suffix)) {
				suffix++;
			}
			name += suffix;
		}
		return name;
	}
}
