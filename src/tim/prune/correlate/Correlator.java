package tim.prune.correlate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.MediaObject;
import tim.prune.data.MediaList;
import tim.prune.data.TimeDifference;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.tips.TipManager;

/**
 * Abstract superclass of the two correlator functions
 */
public abstract class Correlator extends GenericFunction
{
	protected JDialog _dialog;
	private CardStack _cards = null;
	private JTable _selectionTable = null;
	protected JTable _previewTable = null;
	private boolean _previewEnabled = false; // flag required to enable preview function on final panel
	private boolean[] _cardEnabled = null; // flag for each card
	private TimeZone _timezone = null;
	private JTextField _offsetHourBox = null, _offsetMinBox = null, _offsetSecBox = null;
	private JRadioButton _mediaLaterOption = null, _pointLaterOption = null;
	private JRadioButton _timeLimitRadio = null, _distLimitRadio = null;
	private JTextField _limitMinBox = null, _limitSecBox = null;
	private JTextField _limitDistBox = null;
	private JComboBox<String> _distUnitsDropdown = null;
	private JButton _nextButton = null, _backButton = null;
	protected JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp App object to report actions to
	 */
	public Correlator(App inApp) {
		super(inApp);
	}

	/**
	 * @return type key eg photo, audio
	 */
	protected abstract String getMediaTypeKey();

	/**
	 * @return media list
	 */
	protected abstract MediaList getMediaList();

	/**
	 * Begin the function by initialising and showing the dialog
	 */
	public void begin()
	{
		// Check whether track has timestamps, exit if not
		if (!_app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP))
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.correlate.notimestamps"),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Show warning if no uncorrelated audios
		if (!getMediaList().hasUncorrelatedMedia())
		{
			Object[] buttonTexts = {I18nManager.getText("button.continue"), I18nManager.getText("button.cancel")};
			if (JOptionPane.showOptionDialog(_parentFrame,
					I18nManager.getText("dialog.correlate.nouncorrelated" + getMediaTypeKey() + "s"),
					I18nManager.getText(getNameKey()), JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
				== JOptionPane.NO_OPTION)
			{
				return;
			}
		}
		// Create dialog if necessary
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogContents());
			_dialog.pack();
		}
		_okButton.setEnabled(false);
		// Init timezone to the currently selected one
		_timezone = TimezoneHelper.getSelectedTimezone();
		// Go to first available card
		int card = 0;
		_cardEnabled = null;
		while (!isCardEnabled(card)) {
			card++;
		}
		_cards.showCard(card);
		showCard(0); // does set up and next/prev enabling
		if (!isCardEnabled(1)) {
			_app.showTip(TipManager.Tip_ManuallyCorrelateOne);
		}
		_dialog.setVisible(true);
	}

	/**
	 * Make contents of correlate dialog
	 * @return JPanel containing gui elements
	 */
	private JPanel makeDialogContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Card panel in the middle
		_cards = new CardStack();

		// First panel (not required by photo correlator)
		JPanel card1 = makeFirstPanel();
		if (card1 == null) {card1 = new JPanel();}
		_cards.addCard(card1);

		// Second panel for selection of linked media
		_cards.addCard(makeSecondPanel());

		// Third panel for options and preview
		_cards.addCard(makeThirdPanel());
		mainPanel.add(_cards, BorderLayout.CENTER);

		// Button panel at the bottom
		JPanel buttonPanel = new JPanel();
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard(-1);
			}
		});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_nextButton = new JButton(I18nManager.getText("button.next"));
		_nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard(1);
			}
		});
		buttonPanel.add(_nextButton);
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					finishCorrelation();
					_dialog.dispose();
				}
			});
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * Construct a table model for the photo / audio selection table
	 * @return table model
	 */
	protected MediaSelectionTableModel makeSelectionTableModel()
	{
		MediaList mediaList = getMediaList();
		MediaSelectionTableModel model = new MediaSelectionTableModel(
			"dialog.correlate.select." + getMediaTypeKey() + "name",
			"dialog.correlate.select." + getMediaTypeKey() + "later");
		int numMedia = mediaList.getNumMedia();
		for (int i=0; i<numMedia; i++)
		{
			MediaObject media = mediaList.getMedia(i);
			// For working out time differences, can't use media which already had point information
			if (media.getDataPoint() != null && media.getDataPoint().hasTimestamp()
				&& media.getOriginalStatus() == MediaObject.Status.NOT_CONNECTED)
			{
				// Calculate time difference, add to table model
				long timeDiff = getMediaTimestamp(media).getSecondsSince(media.getDataPoint().getTimestamp(), _timezone);
				model.addMedia(media, timeDiff);
			}
		}
		return model;
	}

	/**
	 * Group the two radio buttons together with a ButtonGroup
	 * @param inButton1 first radio button
	 * @param inButton2 second radio button
	 */
	protected static void groupRadioButtons(JRadioButton inButton1, JRadioButton inButton2)
	{
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(inButton1);
		buttonGroup.add(inButton2);
		inButton1.setSelected(true);
	}


	/**
	 * Try to parse the given string
	 * @param inText String to parse
	 * @return value if parseable, 0 otherwise
	 */
	protected static int getValue(String inText)
	{
		int value = 0;
		try {
			value = Integer.parseInt(inText);
		}
		catch (NumberFormatException nfe) {}
		return value;
	}


	/**
	 * Calculate the median index to select from the table
	 * @param inModel table model
	 * @return index of entry to select from table
	 */
	protected static int getMedianIndex(MediaSelectionTableModel inModel)
	{
		// make sortable list
		TreeSet<TimeIndexPair> set = new TreeSet<TimeIndexPair>();
		// loop through rows of table adding to list
		int numRows = inModel.getRowCount();
		int i;
		for (i=0; i<numRows; i++)
		{
			MediaSelectionTableRow row = inModel.getRow(i);
			set.add(new TimeIndexPair(row.getTimeDiff().getTotalSeconds(), i));
		}
		// pull out middle entry and return index
		TimeIndexPair pair = null;
		Iterator<TimeIndexPair> iterator = set.iterator();
		for (i=0; i<(numRows+1)/2; i++)
		{
			pair = iterator.next();
		}
		return pair.getIndex();
	}


	/**
	 * Disable the ok button
	 */
	public void disableOkButton()
	{
		if (_okButton != null) {
			_okButton.setEnabled(false);
		}
	}

	/**
	 * @return gui components for first panel, or null if empty
	 */
	protected JPanel makeFirstPanel() {
		return null;
	}

	/**
	 * Make the second panel for the selection screen
	 * @return JPanel object containing gui elements
	 */
	private JPanel makeSecondPanel()
	{
		JPanel card = new JPanel();
		card.setLayout(new BorderLayout(10, 10));
		JLabel introLabel = new JLabel(I18nManager.getText(
			"dialog.correlate." + getMediaTypeKey() + "select.intro"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		card.add(introLabel, BorderLayout.NORTH);
		// table doesn't have model yet - that will be attached later
		_selectionTable = new JTable();
		JScrollPane photoScrollPane = new JScrollPane(_selectionTable);
		photoScrollPane.setPreferredSize(new Dimension(400, 100));
		card.add(photoScrollPane, BorderLayout.CENTER);
		return card;
	}


	/**
	 * Make contents of third panel including options and preview
	 * @return JPanel containing gui elements
	 */
	private JPanel makeThirdPanel()
	{
		OptionsChangedListener optionsChangedListener = new OptionsChangedListener(this);
		// Second panel for options
		JPanel card2 = new JPanel();
		card2.setLayout(new BorderLayout());
		JPanel card2Top = new JPanel();
		card2Top.setLayout(new BoxLayout(card2Top, BoxLayout.Y_AXIS));
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.correlate.options.intro"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(8, 6, 5, 6));
		card2Top.add(introLabel);
		// time offset section
		JPanel offsetPanel = new JPanel();
		offsetPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.correlate.options.offsetpanel")));
		offsetPanel.setLayout(new BoxLayout(offsetPanel, BoxLayout.Y_AXIS));
		JPanel offsetPanelTop = new JPanel();
		offsetPanelTop.setLayout(new FlowLayout());
		offsetPanelTop.setBorder(null);
		offsetPanelTop.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset") + ": "));
		_offsetHourBox = new JTextField(3);
		_offsetHourBox.addKeyListener(optionsChangedListener);
		offsetPanelTop.add(_offsetHourBox);
		offsetPanelTop.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.hours")));
		_offsetMinBox = new JTextField(3);
		_offsetMinBox.addKeyListener(optionsChangedListener);
		offsetPanelTop.add(_offsetMinBox);
		offsetPanelTop.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.minutes")));
		_offsetSecBox = new JTextField(3);
		_offsetSecBox.addKeyListener(optionsChangedListener);
		offsetPanelTop.add(_offsetSecBox);
		offsetPanelTop.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.seconds")));
		offsetPanel.add(offsetPanelTop);

		// radio buttons for photo / point later
		JPanel offsetPanelBot = new JPanel();
		offsetPanelBot.setLayout(new FlowLayout());
		offsetPanelBot.setBorder(null);
		_mediaLaterOption = new JRadioButton(I18nManager.getText("dialog.correlate.options." + getMediaTypeKey() + "later"));
		_pointLaterOption = new JRadioButton(I18nManager.getText("dialog.correlate.options.pointlater" + getMediaTypeKey()));
		_mediaLaterOption.addItemListener(optionsChangedListener);
		_pointLaterOption.addItemListener(optionsChangedListener);
		ButtonGroup laterGroup = new ButtonGroup();
		laterGroup.add(_mediaLaterOption);
		laterGroup.add(_pointLaterOption);
		offsetPanelBot.add(_mediaLaterOption);
		offsetPanelBot.add(_pointLaterOption);
		offsetPanel.add(offsetPanelBot);
		offsetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card2Top.add(offsetPanel);

		// listener for radio buttons
		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableEditBoxes();
			}
		};
		// time limits section
		JPanel limitsPanel = new JPanel();
		limitsPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.correlate.options.limitspanel")));
		limitsPanel.setLayout(new BoxLayout(limitsPanel, BoxLayout.Y_AXIS));
		JPanel timeLimitPanel = new JPanel();
		timeLimitPanel.setLayout(new FlowLayout());
		JRadioButton noTimeLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.notimelimit"));
		noTimeLimitRadio.addItemListener(optionsChangedListener);
		noTimeLimitRadio.addActionListener(radioListener);
		timeLimitPanel.add(noTimeLimitRadio);
		_timeLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.timelimit") + ": ");
		_timeLimitRadio.addItemListener(optionsChangedListener);
		_timeLimitRadio.addActionListener(radioListener);
		timeLimitPanel.add(_timeLimitRadio);
		groupRadioButtons(noTimeLimitRadio, _timeLimitRadio);
		_limitMinBox = new JTextField(3);
		_limitMinBox.addKeyListener(optionsChangedListener);
		timeLimitPanel.add(_limitMinBox);
		timeLimitPanel.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.minutes")));
		_limitSecBox = new JTextField(3);
		_limitSecBox.addKeyListener(optionsChangedListener);
		timeLimitPanel.add(_limitSecBox);
		timeLimitPanel.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.seconds")));
		limitsPanel.add(timeLimitPanel);
		// distance limits
		JPanel distLimitPanel = new JPanel();
		distLimitPanel.setLayout(new FlowLayout());
		JRadioButton noDistLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.nodistancelimit"));
		noDistLimitRadio.addItemListener(optionsChangedListener);
		noDistLimitRadio.addActionListener(radioListener);
		distLimitPanel.add(noDistLimitRadio);
		_distLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.distancelimit") + ": ");
		_distLimitRadio.addItemListener(optionsChangedListener);
		_distLimitRadio.addActionListener(radioListener);
		distLimitPanel.add(_distLimitRadio);
		groupRadioButtons(noDistLimitRadio, _distLimitRadio);
		_limitDistBox = new JTextField(4);
		_limitDistBox.addKeyListener(optionsChangedListener);
		distLimitPanel.add(_limitDistBox);
		String[] distUnitsOptions = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.metres"),
			I18nManager.getText("units.miles")};
		_distUnitsDropdown = new JComboBox<String>(distUnitsOptions);
		_distUnitsDropdown.addItemListener(optionsChangedListener);
		distLimitPanel.add(_distUnitsDropdown);
		limitsPanel.add(distLimitPanel);
		limitsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card2Top.add(limitsPanel);

		// preview button
		JButton previewButton = new JButton(I18nManager.getText("button.preview"));
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createPreview(true);
			}
		});
		card2Top.add(previewButton);
		card2.add(card2Top, BorderLayout.NORTH);
		// preview
		_previewTable = new JTable(new MediaPreviewTableModel("dialog.correlate.select." + getMediaTypeKey() + "name"));
		JScrollPane previewScrollPane = new JScrollPane(_previewTable);
		previewScrollPane.setPreferredSize(new Dimension(300, 100));
		card2.add(previewScrollPane, BorderLayout.CENTER);
		return card2;
	}


	/**
	 * Go to the next or previous card in the stack
	 * @param increment 1 for next, -1 for previous card
	 */
	private void showCard(int increment)
	{
		int currCard = _cards.getCurrentCardIndex();
		int next = currCard + increment;
		if (!isCardEnabled(next)) {
			next += increment;
		}
		setupCard(next);
		_backButton.setEnabled(next > 0 && (isCardEnabled(next-1) || isCardEnabled(next-2)));
		_nextButton.setEnabled(next < (_cards.getNumCards()-1));
		_cards.showCard(next);
	}

	/**
	 * @param inCardNum index of card
	 * @return true if specified card is enabled
	 */
	private boolean isCardEnabled(int inCardNum)
	{
		if (_cardEnabled == null) {
			_cardEnabled = getCardEnabledFlags();
		}
		return (inCardNum >= 0 && inCardNum < _cardEnabled.length && _cardEnabled[inCardNum]);
	}

	/**
	 * @return array of boolean flags denoting availability of cards
	 */
	protected boolean[] getCardEnabledFlags()
	{
		// by default first is off and third is always on; second depends on selection table
		return new boolean[] {false, makeSelectionTableModel().getRowCount() > 0, true};
	}

	/**
	 * Set up the specified card
	 * @param inCardNum index of card
	 */
	protected void setupCard(int inCardNum)
	{
		_previewEnabled = false;
		if (inCardNum == 1)
		{
			// set up photo selection card
			MediaSelectionTableModel model = makeSelectionTableModel();
			_selectionTable.setModel(model);
			for (int i=0; i<model.getColumnCount(); i++) {
				_selectionTable.getColumnModel().getColumn(i).setPreferredWidth(i==3?50:150);
			}
			// Calculate median time difference, select corresponding row of table
			int preselectedIndex = model.getRowCount() < 3 ? 0 : getMedianIndex(model);
			_selectionTable.getSelectionModel().setSelectionInterval(preselectedIndex, preselectedIndex);
			_nextButton.requestFocus();
		}
		else if (inCardNum == 2)
		{
			// set up the options/preview card - first check for given time difference
			TimeDifference timeDiff = null;
			if (isCardEnabled(1))
			{
				int rowNum = _selectionTable.getSelectedRow();
				if (rowNum < 0) {rowNum = 0;}
				MediaSelectionTableRow selectedRow =
					((MediaSelectionTableModel) _selectionTable.getModel()).getRow(rowNum);
				timeDiff = selectedRow.getTimeDiff();
			}
			setupPreviewCard(timeDiff, getMediaList().getMedia(0));
		}
		// enable ok button if any photos have been selected
		_okButton.setEnabled(inCardNum == 2 && ((MediaPreviewTableModel) _previewTable.getModel()).hasAnySelected());
	}

	/**
	 * Enable or disable the edit boxes according to the radio button selections
	 */
	private void enableEditBoxes()
	{
		// enable/disable text field for distance input
		_limitDistBox.setEnabled(_distLimitRadio.isSelected());
		// and for time limits
		_limitMinBox.setEnabled(_timeLimitRadio.isSelected());
		_limitSecBox.setEnabled(_timeLimitRadio.isSelected());
	}

	/**
	 * Parse the time limit values entered and validate them
	 * @return TimeDifference object describing limit
	 */
	protected TimeDifference parseTimeLimit()
	{
		if (!_timeLimitRadio.isSelected()) {return null;}
		int mins = getValue(_limitMinBox.getText());
		_limitMinBox.setText("" + mins);
		int secs = getValue(_limitSecBox.getText());
		_limitSecBox.setText("" + secs);
		if (mins <= 0 && secs <= 0) {return null;}
		return new TimeDifference(0, mins, secs, true);
	}

	/**
	 * Parse the distance limit value entered and validate
	 * @return angular distance in radians
	 */
	protected double parseDistanceLimit()
	{
		double value = -1.0;
		if (_distLimitRadio.isSelected())
		{
			try {
				value = Double.parseDouble(_limitDistBox.getText());
			}
			catch (NumberFormatException nfe) {}
		}
		if (value <= 0.0) {
			_limitDistBox.setText("0");
			return -1.0;
		}
		_limitDistBox.setText("" + value);
		return Distance.convertDistanceToRadians(value, getSelectedDistanceUnits());
	}


	/**
	 * @return the selected distance units from the dropdown
	 */
	protected Unit getSelectedDistanceUnits()
	{
		final Unit[] distUnits = {UnitSetLibrary.UNITS_KILOMETRES, UnitSetLibrary.UNITS_METRES, UnitSetLibrary.UNITS_MILES};
		return distUnits[_distUnitsDropdown.getSelectedIndex()];
	}

	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inFromButton true if triggered from button press, false if automatic
	 */
	public void createPreview(boolean inFromButton)
	{
		// Exit if still on first panel
		if (!_previewEnabled) {
			return;
		}
		// Create a TimeDifference based on the edit boxes
		int numHours = getValue(_offsetHourBox.getText());
		int numMins = getValue(_offsetMinBox.getText());
		int numSecs = getValue(_offsetSecBox.getText());
		boolean isPos = _mediaLaterOption.isSelected();
		createPreview(new TimeDifference(numHours, numMins, numSecs, isPos), inFromButton);
	}

	/**
	 * Set up the final card using the given time difference and show it
	 * @param inTimeDiff time difference to use for time offsets
	 * @param inFirstMedia first media object to use for calculating timezone
	 */
	protected void setupPreviewCard(TimeDifference inTimeDiff, MediaObject inFirstMedia)
	{
		_previewEnabled = false;
		TimeDifference timeDiff = inTimeDiff;
		if (timeDiff == null)
		{
			// No time difference available, so try with zero
			timeDiff = new TimeDifference(0L);
		}
		// Use time difference to set edit boxes
		_offsetHourBox.setText("" + timeDiff.getNumHours());
		_offsetMinBox.setText("" + timeDiff.getNumMinutes());
		_offsetSecBox.setText("" + timeDiff.getNumSeconds());
		_mediaLaterOption.setSelected(timeDiff.getIsPositive());
		_pointLaterOption.setSelected(!timeDiff.getIsPositive());
		_previewEnabled = true;
		enableEditBoxes();
		createPreview(timeDiff, true);
	}

	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inTimeDiff TimeDifference to use for preview
	 * @param inShowWarning true to show warning if all points out of range
	 */
	protected abstract void createPreview(TimeDifference inTimeDiff, boolean inShowWarning);


	/**
	 * Get the timestamp of the given media
	 * @param inMedia media object
	 * @return normally just returns the media timestamp, overridden by audio correlator
	 */
	protected Timestamp getMediaTimestamp(MediaObject inMedia)
	{
		return inMedia.getTimestamp();
	}

	/**
	 * Get the point pair surrounding the given media item
	 * @param inTrack track object
	 * @param inMedia media object
	 * @param inOffset time offset to apply
	 * @return point pair resulting from correlation
	 */
	protected PointMediaPair getPointPairForMedia(Track inTrack, MediaObject inMedia, TimeDifference inOffset)
	{
		PointMediaPair pair = new PointMediaPair(inMedia);
		if (inMedia.hasTimestamp())
		{
			// Add/subtract offset to media timestamp
			Timestamp mediaStamp = getMediaTimestamp(inMedia);
			int numPoints = inTrack.getNumPoints();
			for (int i=0; i<numPoints; i++)
			{
				DataPoint point = inTrack.getPoint(i);
				if (point.getPhoto() == null && point.getAudio() == null)
				{
					Timestamp pointStamp = point.getTimestamp();
					if (pointStamp != null && pointStamp.isValid())
					{
						long numSeconds = pointStamp.getSecondsSince(mediaStamp, _timezone)
							+ inOffset.getTotalSeconds();
						pair.addPoint(point, numSeconds);
					}
				}
			}
		}
		return pair;
	}


	/**
	 * Finish the correlation
	 */
	protected abstract void finishCorrelation();

	/**
	 * Construct an array of the point pairs to use for correlation
	 * @return array of PointMediaPair objects
	 */
	protected PointMediaPair[] getPointPairs()
	{
		MediaPreviewTableModel model = (MediaPreviewTableModel) _previewTable.getModel();
		int numMedia = model.getRowCount();
		PointMediaPair[] pairs = new PointMediaPair[numMedia];
		// Loop over items in preview table model
		for (int i=0; i<numMedia; i++)
		{
			MediaPreviewTableRow row = model.getRow(i);
			// add all selected pairs to array (other elements remain null)
			if (row.getCorrelateFlag().booleanValue()) {
				pairs[i] = row.getPointPair();
			}
		}
		return pairs;
	}
}
