package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.gui.CombinedListAndModel;
import tim.prune.gui.GuiGridLayout;

/**
 * Class to provide the gui for selecting an alternative timezone
 */
public class SelectTimezoneFunction extends GenericFunction
{
	/** Arraylist of timezone infos */
	private ArrayList<TimezoneDetails> _zoneInfo;
	/** Dialog */
	private JDialog _dialog = null;
	/** Radio button to select system timezone instead of using listboxes */
	private JRadioButton _systemRadio = null;
	/** Radio button to select timezone using listboxes */
	private JRadioButton _customRadio = null;
	/** Array of list boxes */
	private CombinedListAndModel[] _listBoxes = null;
	/** Label for selected zone */
	private JLabel _selectedZoneLabel = null;
	/** Label for offset of selected zone */
	private JLabel _selectedOffsetLabel = null;
	/** OK button for finishing */
	private JButton _okButton = null;

	private static final int LIST_REGIONS = 0;
	private static final int LIST_OFFSETS = 1;
	private static final int LIST_GROUPS  = 2;
	private static final int LIST_NAMES   = 3;

	/**
	 * Inner class for listening to list clicks
	 */
	class ListListener implements ListSelectionListener
	{
		private int _key = 0;
		/** Constructor */
		ListListener(int inKey) {_key = inKey;}
		/** Listen for selection changes */
		public void valueChanged(ListSelectionEvent inEvent) {
			if (!inEvent.getValueIsAdjusting()) {
				processListClick(_key);
			}
		}
	}

	/** Inner class to hold categorisation info for a timezone */
	class TimezoneDetails
	{
		public String _id;
		public String _region;
		public int    _offset;
		public String _group;
		public String _name;
	}

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SelectTimezoneFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.selecttimezone";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		collectTimezoneInfo();
		_systemRadio.setText(I18nManager.getText("dialog.settimezone.system") + " ("
			+ TimeZone.getDefault().getID() + ")");
		// Set up dialog according to current config
		String selectedTimezone = Config.getConfigString(Config.KEY_TIMEZONE_ID);
		if (selectedTimezone == null || selectedTimezone.equals(""))
		{
			_systemRadio.setSelected(true);
		}
		else
		{
			_customRadio.setSelected(true);
		}
		_dialog.setVisible(true);
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(5, 5));
		// Listener for radio buttons
		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent inEvent) {
				radioSelected(_systemRadio.isSelected());
			}
		};
		FocusListener radioFocusListener = new FocusAdapter() {
			public void focusGained(FocusEvent inEvent) {
				radioSelected(_systemRadio.isSelected());
			}
		};

		// Panel at top
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.settimezone.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPanel.add(topLabel);
		_systemRadio = new JRadioButton(I18nManager.getText("dialog.settimezone.system"));
		_systemRadio.addActionListener(radioListener);
		_systemRadio.addFocusListener(radioFocusListener);
		topPanel.add(_systemRadio);
		_customRadio = new JRadioButton(I18nManager.getText("dialog.settimezone.custom"));
		_customRadio.addActionListener(radioListener);
		_customRadio.addFocusListener(radioFocusListener);
		topPanel.add(_customRadio);
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(_systemRadio); radioGroup.add(_customRadio);
		dialogPanel.add(topPanel, BorderLayout.NORTH);

		// Main panel with box layout, list Panel with four lists in a grid
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel listsPanel = new JPanel();
		listsPanel.setLayout(new GridLayout(1, 4));
		_listBoxes = new CombinedListAndModel[4];
		// First list for regions
		_listBoxes[LIST_REGIONS] = new CombinedListAndModel(0);
		// Add listener for list selection changes
		_listBoxes[LIST_REGIONS].addListSelectionListener(new ListListener(LIST_REGIONS));
		JScrollPane scrollPane = new JScrollPane(_listBoxes[LIST_REGIONS]);
		scrollPane.setPreferredSize(new Dimension(100, 200));
		scrollPane.setMinimumSize(new Dimension(100, 200));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		listsPanel.add(scrollPane);

		// second list for offsets
		_listBoxes[LIST_OFFSETS] = new CombinedListAndModel(1);
		_listBoxes[LIST_OFFSETS].setMaxNumEntries(24);
		_listBoxes[LIST_OFFSETS].addListSelectionListener(new ListListener(LIST_OFFSETS));
		scrollPane = new JScrollPane(_listBoxes[LIST_OFFSETS]);
		scrollPane.setPreferredSize(new Dimension(100, 200));
		scrollPane.setMinimumSize(new Dimension(100, 200));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		listsPanel.add(scrollPane);

		// third list for groups
		_listBoxes[LIST_GROUPS] = new CombinedListAndModel(2);
		_listBoxes[LIST_GROUPS].setMaxNumEntries(20);
		_listBoxes[LIST_GROUPS].addListSelectionListener(new ListListener(LIST_GROUPS));
		scrollPane = new JScrollPane(_listBoxes[LIST_GROUPS]);
		scrollPane.setPreferredSize(new Dimension(100, 200));
		scrollPane.setMinimumSize(new Dimension(100, 200));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		listsPanel.add(scrollPane);

		// fourth list for names
		_listBoxes[LIST_NAMES] = new CombinedListAndModel(3);
		_listBoxes[LIST_NAMES].setMaxNumEntries(20);
		_listBoxes[LIST_NAMES].addListSelectionListener(new ListListener(LIST_NAMES));
		scrollPane = new JScrollPane(_listBoxes[LIST_NAMES]);
		scrollPane.setPreferredSize(new Dimension(100, 200));
		scrollPane.setMinimumSize(new Dimension(100, 200));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		listsPanel.add(scrollPane);
		mainPanel.add(listsPanel);

		// Details labels underneath lists - description and offset
		JPanel detailsPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(detailsPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.settimezone.selectedzone") + " :"));
		_selectedZoneLabel = new JLabel("");
		grid.add(_selectedZoneLabel);
		grid.add(new JLabel(I18nManager.getText("dialog.settimezone.offsetfromutc") + " :"));
		_selectedOffsetLabel = new JLabel("");
		grid.add(_selectedOffsetLabel);
		mainPanel.add(detailsPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// close window if escape pressed
		KeyAdapter escListener = new KeyAdapter() {
			public void keyReleased(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_listBoxes[LIST_REGIONS].addKeyListener(escListener);
		_listBoxes[LIST_OFFSETS].addKeyListener(escListener);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// OK button
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishSelectTimezone();
			}
		});
		buttonPanel.add(_okButton);
		_okButton.addKeyListener(escListener);
		// Cancel button
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		cancelButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * React to changes in the radio buttons
	 * @param inUseSystem true for system, false for custom
	 */
	private void radioSelected(boolean inUseSystem)
	{
		for (int i=0; i<_listBoxes.length; i++)
		{
			if (inUseSystem)
			{
				_listBoxes[i].clear();
			}
			_listBoxes[i].setEnabled(!inUseSystem);
		}
		if (!inUseSystem)
		{
			populateTimezoneRegions();
			populateTimezoneOffsets(null);
			preselectTimezone(Config.getConfigString(Config.KEY_TIMEZONE_ID));
		}
		showTimezoneDetails();
	}

	/**
	 * React to a selection change on one of our lists
	 * @param inKey key of list which was clicked
	 */
	private void processListClick(int inKey)
	{
		final boolean offsetSelected = _listBoxes[LIST_OFFSETS].getSelectedItem() != null;
		final boolean groupSelected = _listBoxes[LIST_GROUPS].getSelectedItem() != null;
		// Update offsets?
		if (inKey == LIST_REGIONS)
		{
			populateTimezoneOffsets(_listBoxes[LIST_REGIONS].getSelectedItem());
		}
		// Update groups?
		if (inKey == LIST_OFFSETS
			|| (inKey == LIST_REGIONS && !offsetSelected))
		{
			populateTimezoneGroups(_listBoxes[LIST_REGIONS].getSelectedItem(), _listBoxes[LIST_OFFSETS].getSelectedItem());
		}
		// Update names?
		if (inKey == LIST_GROUPS
			|| (inKey <= LIST_OFFSETS && !groupSelected))
		{
			populateTimezoneNames(_listBoxes[LIST_REGIONS].getSelectedItem(), _listBoxes[LIST_OFFSETS].getSelectedItem(),
				_listBoxes[LIST_GROUPS].getSelectedItem());
		}
		// Show the details of the selected timezone
		showTimezoneDetails();
	}

	/**
	 * Use the system information to populate the list of available timezones
	 */
	private void collectTimezoneInfo()
	{
		_zoneInfo = new ArrayList<TimezoneDetails>();
		for (String id : TimeZone.getAvailableIDs())
		{
			String region = getRegion(id);
			if (region != null)
			{
				TimeZone tz = TimeZone.getTimeZone(id);
				TimezoneDetails details = new TimezoneDetails();
				details._id = id;
				details._region = region;
				details._offset = tz.getOffset(System.currentTimeMillis()) / 1000 / 60;
				details._group = tz.getDisplayName();
				details._name = getNameWithoutRegion(id);
				_zoneInfo.add(details);
			}
		}
	}

	/**
	 * Populate the timezone regions into the region list
	 */
	private void populateTimezoneRegions()
	{
		_listBoxes[LIST_REGIONS].clear();
		TreeSet<String> regions = new TreeSet<String>();
		for (TimezoneDetails currZone : _zoneInfo)
		{
			regions.add(currZone._region);
		}
		for (String region : regions)
		{
			_listBoxes[LIST_REGIONS].addItem(region);
		}
	}

	/**
	 * Extract the timezone region from the id
	 */
	private static String getRegion(String inId)
	{
		final int slashPos = (inId == null ? -1 : inId.indexOf('/'));
		if (slashPos > 0)
		{
			return inId.substring(0, slashPos);
		}
		return null;
	}

	/**
	 * Populate the second listbox with the offsets for the given region
	 * @param inRegion selected region, or null if none selected
	 */
	private void populateTimezoneOffsets(String inRegion)
	{
		_listBoxes[LIST_OFFSETS].clear();
		TreeSet<Integer> offsetsinMinutes = new TreeSet<Integer>();
		for (TimezoneDetails currZone : _zoneInfo)
		{
			String region = currZone._region;
			if (inRegion == null || region.equals(inRegion))
			{
				offsetsinMinutes.add(currZone._offset);
			}
		}
		for (Integer offset : offsetsinMinutes)
		{
			_listBoxes[LIST_OFFSETS].addItem(makeOffsetString(offset));
		}
	}

	/**
	 * @return String containing offset for display
	 */
	private static String makeOffsetString(int inOffsetInMinutes)
	{
		if (inOffsetInMinutes == 0) return "0";
		final boolean isWholeHours = (inOffsetInMinutes % 60) == 0;
		if (isWholeHours)
		{
			return (inOffsetInMinutes > 0 ? "+" : "") + (inOffsetInMinutes / 60);
		}
		final double numHours = inOffsetInMinutes / 60.0;
		return (inOffsetInMinutes > 0 ? "+" : "") + numHours;
	}

	/**
	 * Populate the group list using the specified region and offset
	 * @param inRegion selected region (if any) from the first list
	 * @param inOffset selected offset (if any) from the second list
	 */
	private void populateTimezoneGroups(String inRegion, String inOffset)
	{
		_listBoxes[LIST_GROUPS].clear();
		// Convert given offset string (in hours) into numeric offset (in minutes)
		final int offsetMins = convertToMinutes(inOffset);

		TreeSet<String> zoneGroups = new TreeSet<String>();
		for (TimezoneDetails currZone : _zoneInfo)
		{
			if (inRegion == null || currZone._region.equals(inRegion))
			{
				if (offsetMins == -1 || offsetMins == currZone._offset)
				{
					zoneGroups.add(currZone._group);
				}
			}
		}
		// If the region and offset were given, then list is unlimited
		_listBoxes[LIST_GROUPS].setUnlimited(inRegion != null && inOffset != null);
		// Add all the found names to the listbox
		for (String group : zoneGroups)
		{
			_listBoxes[LIST_GROUPS].addItem(group);
		}
	}

	/**
	 * Populate the group list using the specified region, offset and group
	 * @param inRegion selected region (if any) from the first list
	 * @param inOffset selected offset (if any) from the second list
	 * @param inGroup selected group (if any) from the third list
	 */
	private void populateTimezoneNames(String inRegion, String inOffset, String inGroup)
	{
		CombinedListAndModel nameList = _listBoxes[LIST_NAMES];
		nameList.clear();
		// Convert given offset string (in hours) into numeric offset (in minutes)
		final int offsetMins = convertToMinutes(inOffset);

		TreeSet<String> zoneNames = new TreeSet<String>();
		for (TimezoneDetails currZone : _zoneInfo)
		{
			if ((inRegion == null || currZone._region.equals(inRegion))
				&& (offsetMins == -1 || currZone._offset == offsetMins)
				&& (inGroup == null || currZone._group.equals(inGroup)))
			{
				zoneNames.add(currZone._name);
			}
		}
		// If the region and offset were given, then list is unlimited
		nameList.setUnlimited(inRegion != null && inOffset != null);
		// Add all the found names to the listbox
		for (String name : zoneNames)
		{
			nameList.addItem(name);
		}
	}

	/**
	 * Convert the given String from hours to minutes
	 * @param inOffsetInHours String from listbox in +/- hours
	 * @return offset in minutes, or -1
	 */
	private static int convertToMinutes(String inOffsetInHours)
	{
		int offsetMins = -1;
		try {
			offsetMins = (int) (60 * Double.parseDouble(inOffsetInHours));
		}
		catch (NumberFormatException nfe) {} // offset stays -1
		catch (NullPointerException npe) {} // offset stays -1
		return offsetMins;
	}

	/**
	 * Remove the timezone region from the id to just leave the name after the slash
	 */
	private static String getNameWithoutRegion(String inId)
	{
		final int slashPos = (inId == null ? -1 : inId.indexOf('/'));
		if (slashPos > 0)
		{
			return inId.substring(slashPos + 1);
		}
		return null;
	}

	/**
	 * Get the selected timezone, or null if none selected
	 */
	private TimeZone getSelectedTimezone()
	{
		if (_systemRadio.isSelected())
		{
			return TimeZone.getDefault();
		}

		String chosenRegion = _listBoxes[LIST_REGIONS].getSelectedItem();
		// Convert given offset string (in hours) into numeric offset (in minutes)
		final int offsetMins = convertToMinutes(_listBoxes[LIST_OFFSETS].getSelectedItem());
		String chosenGroup = _listBoxes[LIST_GROUPS].getSelectedItem();
		String chosenName = _listBoxes[LIST_NAMES].getSelectedItem();

		TreeSet<String> zoneIds = new TreeSet<String>();
		for (TimezoneDetails currZone : _zoneInfo)
		{
			if ((chosenRegion == null || currZone._region.equals(chosenRegion))
				&& (offsetMins == -1 || currZone._offset == offsetMins)
				&& (chosenGroup == null || currZone._group.equals(chosenGroup))
				&& (chosenName == null || currZone._name.equals(chosenName)))
			{
				zoneIds.add(currZone._id);
				if (zoneIds.size() > 1) {
					break;	// exit loop now, we've got too many
				}
			}
		}
		// Should have exactly one result now
		if (zoneIds.size() == 1)
		{
			return TimeZone.getTimeZone(zoneIds.first());
		}

		// none selected (yet)
		return null;
	}

	/**
	 * Show the details of the selected timezone
	 */
	private void showTimezoneDetails()
	{
		TimeZone selectedTimezone = getSelectedTimezone();
		if (selectedTimezone == null)
		{
			// Clear details labels
			_selectedZoneLabel.setText("");
			_selectedOffsetLabel.setText("");
		}
		else
		{
			// Fill results in labels
			String desc = selectedTimezone.getID() + " - " + selectedTimezone.getDisplayName();
			_selectedZoneLabel.setText(desc);
			String offsets = getOffsetDescription(selectedTimezone);
			_selectedOffsetLabel.setText(offsets);
		}
		_okButton.setEnabled(selectedTimezone != null);
	}

	/**
	 * @param inTimezone selected timezone
	 * @return String describing the time offset(s) of this zone including winter/summer time
	 */
	private static String getOffsetDescription(TimeZone inTimezone)
	{
		if (inTimezone == null)
		{
			return "";
		}
		TreeSet<Integer> offsetsinMinutes = new TreeSet<Integer>();
		long testTimeMillis = System.currentTimeMillis();
		final long testPeriodInMillis = 1000L * 60 * 60 * 24 * 30 * 2;
		for (int i=0; i<5; i++)
		{
			offsetsinMinutes.add(inTimezone.getOffset(testTimeMillis) / 1000 / 60);
			testTimeMillis += testPeriodInMillis;
		}
		// Make String describing the sorted set
		StringBuffer buff = new StringBuffer();
		for (Integer offset : offsetsinMinutes)
		{
			if (buff.length() > 0)
			{
				buff.append(" / ");
			}
			buff.append(makeOffsetString(offset));
		}
		return buff.toString();
	}

	/**
	 * On entry to the dialog, select the items in each listbox
	 * according to the given preselected timezone id
	 * @param zoneId id of zone to select
	 */
	private void preselectTimezone(String zoneId)
	{
		TimeZone tz = (zoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(zoneId));
		if (tz != null)
		{
			_listBoxes[LIST_REGIONS].selectItem(getRegion(zoneId));
			_listBoxes[LIST_OFFSETS].selectItem(makeOffsetString(tz.getOffset(System.currentTimeMillis()) / 1000 / 60));
			_listBoxes[LIST_GROUPS].selectItem(tz.getDisplayName());
			_listBoxes[LIST_NAMES].selectItem(getNameWithoutRegion(zoneId));
		}
	}

	/**
	 * Finish the dialog by setting the config according to the selected zone
	 */
	private void finishSelectTimezone()
	{
		TimeZone selectedTimezone = getSelectedTimezone();
		if (_systemRadio.isSelected() || selectedTimezone == null)
		{
			// Clear config, use default system timezone instead
			Config.setConfigString(Config.KEY_TIMEZONE_ID, null);
		}
		else
		{
			// Get selected timezone, set in config
			Config.setConfigString(Config.KEY_TIMEZONE_ID, selectedTimezone.getID());
		}
		_dialog.dispose();
		// Make sure listeners know to update themselves
		UpdateMessageBroker.informSubscribers(DataSubscriber.UNITS_CHANGED);
	}
}
