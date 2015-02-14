package tim.prune.correlate;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.TimeDifference;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Class to manage the automatic correlation of photos to points
 * including the GUI stuff to control the correlation options
 */
public class PhotoCorrelator
{
	private App _app;
	private JFrame _parentFrame;
	private JDialog _dialog;
	private JButton _nextButton = null, _backButton = null;
	private JButton _okButton = null;
	private JPanel _cards = null;
	private JTable _photoSelectionTable = null;
	private JLabel _tipLabel = null;
	private JTextField _offsetHourBox = null, _offsetMinBox = null, _offsetSecBox = null;
	private JRadioButton _photoLaterOption = null, _pointLaterOption = null;
	private JRadioButton _timeLimitRadio = null, _distLimitRadio = null;
	private JTextField _limitMinBox = null, _limitSecBox = null;
	private JTextField _limitDistBox = null;
	private JComboBox _distUnitsDropdown = null;
	private JTable _previewTable = null;
	private boolean _firstTabAvailable = false;
	private boolean _previewEnabled = false; // flag required to enable preview function on second panel


	/**
	 * Constructor
	 * @param inApp App object to report actions to
	 * @param inFrame parent frame for dialogs
	 */
	public PhotoCorrelator(App inApp, JFrame inFrame)
	{
		_app = inApp;
		_parentFrame = inFrame;
		_dialog = new JDialog(inFrame, I18nManager.getText("dialog.correlate.title"), true);
		_dialog.setLocationRelativeTo(inFrame);
		_dialog.getContentPane().add(makeDialogContents());
		_dialog.pack();
	}


	/**
	 * Reset dialog and show it
	 */
	public void begin()
	{
		// Check whether track has timestamps, exit if not
		if (!_app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP))
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.correlate.notimestamps"),
				I18nManager.getText("dialog.correlate.title"), JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Check for any non-correlated photos, show warning continue/cancel
		if (!trackHasUncorrelatedPhotos())
		{
			Object[] buttonTexts = {I18nManager.getText("button.continue"), I18nManager.getText("button.cancel")};
			if (JOptionPane.showOptionDialog(_parentFrame, I18nManager.getText("dialog.correlate.nouncorrelatedphotos"),
					I18nManager.getText("dialog.correlate.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
				== JOptionPane.NO_OPTION)
			{
				return;
			}
		}
		PhotoSelectionTableModel model = makePhotoSelectionTableModel(_app.getTrackInfo());
		_firstTabAvailable = model != null && model.getRowCount() > 0;
		CardLayout cl = (CardLayout) _cards.getLayout();
		if (_firstTabAvailable)
		{
			cl.first(_cards);
			_nextButton.setEnabled(true);
			_backButton.setEnabled(false);
			_tipLabel.setVisible(false);
			_photoSelectionTable.setModel(model);
			_previewEnabled = false;
			for (int i=0; i<model.getColumnCount(); i++) {
				_photoSelectionTable.getColumnModel().getColumn(i).setPreferredWidth(i==3?50:150);
			}
			// Calculate median time difference, select corresponding row of table
			int preselectedIndex = model.getRowCount() < 3 ? 0 : getMedianIndex(model);
			_photoSelectionTable.getSelectionModel().setSelectionInterval(preselectedIndex, preselectedIndex);
			_nextButton.requestFocus();
		}
		else
		{
			_tipLabel.setVisible(true);
			setupSecondCard(null);
		}
		_dialog.show();
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
		_cards = new JPanel();
		_cards.setLayout(new CardLayout());

		// First panel for photo selection table
		JPanel card1 = new JPanel();
		card1.setLayout(new BorderLayout(10, 10));
		card1.add(new JLabel(I18nManager.getText("dialog.correlate.photoselect.intro")), BorderLayout.NORTH);
		_photoSelectionTable = new JTable();
		JScrollPane photoScrollPane = new JScrollPane(_photoSelectionTable);
		photoScrollPane.setPreferredSize(new Dimension(400, 100));
		card1.add(photoScrollPane, BorderLayout.CENTER);
		_cards.add(card1, "card1");

		OptionsChangedListener optionsChangedListener = new OptionsChangedListener(this);
		// Second panel for options
		JPanel card2 = new JPanel();
		card2.setLayout(new BorderLayout());
		JPanel card2Top = new JPanel();
		card2Top.setLayout(new BoxLayout(card2Top, BoxLayout.Y_AXIS));
		_tipLabel = new JLabel(I18nManager.getText("dialog.correlate.options.tip"));
		card2Top.add(_tipLabel);
		card2Top.add(new JLabel(I18nManager.getText("dialog.correlate.options.intro")));
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
		_photoLaterOption = new JRadioButton(I18nManager.getText("dialog.correlate.options.photolater"));
		_pointLaterOption = new JRadioButton(I18nManager.getText("dialog.correlate.options.pointlater"));
		_photoLaterOption.addItemListener(optionsChangedListener);
		_pointLaterOption.addItemListener(optionsChangedListener);
		ButtonGroup laterGroup = new ButtonGroup();
		laterGroup.add(_photoLaterOption);
		laterGroup.add(_pointLaterOption);
		offsetPanelBot.add(_photoLaterOption);
		offsetPanelBot.add(_pointLaterOption);
		offsetPanel.add(offsetPanelBot);
		offsetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card2Top.add(offsetPanel);

		// time limits section
		JPanel limitsPanel = new JPanel();
		limitsPanel.setBorder(BorderFactory.createTitledBorder(I18nManager.getText("dialog.correlate.options.limitspanel")));
		limitsPanel.setLayout(new BoxLayout(limitsPanel, BoxLayout.Y_AXIS));
		JPanel timeLimitPanel = new JPanel();
		timeLimitPanel.setLayout(new FlowLayout());
		JRadioButton noTimeLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.notimelimit"));
		noTimeLimitRadio.addItemListener(optionsChangedListener);
		timeLimitPanel.add(noTimeLimitRadio);
		_timeLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.timelimit") + " : ");
		_timeLimitRadio.addItemListener(optionsChangedListener);
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
		distLimitPanel.add(noDistLimitRadio);
		_distLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.distancelimit"));
		_distLimitRadio.addItemListener(optionsChangedListener);
		distLimitPanel.add(_distLimitRadio);
		groupRadioButtons(noDistLimitRadio, _distLimitRadio);
		_limitDistBox = new JTextField(4);
		_limitDistBox.addKeyListener(optionsChangedListener);
		distLimitPanel.add(_limitDistBox);
		String[] distUnitsOptions = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.metres"),
			I18nManager.getText("units.miles")};
		_distUnitsDropdown = new JComboBox(distUnitsOptions);
		_distUnitsDropdown.addItemListener(optionsChangedListener);
		distLimitPanel.add(_distUnitsDropdown);
		limitsPanel.add(distLimitPanel);
		limitsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		card2Top.add(limitsPanel);

		// preview button
		JButton previewButton = new JButton(I18nManager.getText("button.preview"));
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				createPreview(true);
			}
		});
		card2Top.add(previewButton);
		card2.add(card2Top, BorderLayout.NORTH);
		// preview
		_previewTable = new JTable();
		JScrollPane previewScrollPane = new JScrollPane(_previewTable);
		previewScrollPane.setPreferredSize(new Dimension(300, 100));
		card2.add(previewScrollPane, BorderLayout.CENTER);
		_cards.add(card2, "card2");
		mainPanel.add(_cards, BorderLayout.CENTER);

		// Button panel at the bottom
		JPanel buttonPanel = new JPanel();
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					CardLayout cl = (CardLayout) _cards.getLayout();
					cl.previous(_cards);
					_backButton.setEnabled(false);
					_nextButton.setEnabled(true);
					_okButton.setEnabled(false);
					_previewEnabled = false;
				}
			});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_nextButton = new JButton(I18nManager.getText("button.next"));
		_nextButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int rowNum = _photoSelectionTable.getSelectedRow();
					if (rowNum < 0) {rowNum = 0;}
					PhotoSelectionTableRow selectedRow = ((PhotoSelectionTableModel) _photoSelectionTable.getModel())
						.getRow(rowNum);
					setupSecondCard(selectedRow.getTimeDiff());
				}
			});
		buttonPanel.add(_nextButton);
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_app.finishCorrelatePhotos(getPointPairs());
					_dialog.dispose();
				}
			});
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_dialog.dispose();
				}
			});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}


	/**
	 * Construct a table model for the photo selection table
	 * @param inTrackInfo track info object
	 * @return table model
	 */
	private static PhotoSelectionTableModel makePhotoSelectionTableModel(TrackInfo inTrackInfo)
	{
		PhotoSelectionTableModel model = new PhotoSelectionTableModel();
		int numPhotos = inTrackInfo.getPhotoList().getNumPhotos();
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = inTrackInfo.getPhotoList().getPhoto(i);
			if (photo.getDataPoint() != null && photo.getDataPoint().hasTimestamp())
			{
				// Calculate time difference, add to table model
				long timeDiff = photo.getTimestamp().getSecondsSince(photo.getDataPoint().getTimestamp());
				model.addPhoto(photo, timeDiff);
			}
		}
		return model;
	}


	/**
	 * Group the two radio buttons together with a ButtonGroup
	 * @param inButton1 first radio button
	 * @param inButton2 second radio button
	 */
	private static void groupRadioButtons(JRadioButton inButton1, JRadioButton inButton2)
	{
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(inButton1);
		buttonGroup.add(inButton2);
		inButton1.setSelected(true);
	}


	/**
	 * Set up the second card using the given time difference and show it
	 * @param inTimeDiff time difference to use for photo time offsets
	 */
	private void setupSecondCard(TimeDifference inTimeDiff)
	{
		_previewEnabled = false;
		boolean hasTimeDiff = inTimeDiff != null;
		if (!hasTimeDiff)
		{
			// No time difference available, so calculate based on computer's time zone
			inTimeDiff = getTimezoneOffset();
		}
		// Use time difference to set edit boxes
		_offsetHourBox.setText("" + inTimeDiff.getNumHours());
		_offsetMinBox.setText("" + inTimeDiff.getNumMinutes());
		_offsetSecBox.setText("" + inTimeDiff.getNumSeconds());
		_photoLaterOption.setSelected(inTimeDiff.getIsPositive());
		_pointLaterOption.setSelected(!inTimeDiff.getIsPositive());
		createPreview(inTimeDiff, true);
		CardLayout cl = (CardLayout) _cards.getLayout();
		cl.next(_cards);
		_backButton.setEnabled(hasTimeDiff);
		_nextButton.setEnabled(false);
		// enable ok button if any photos have been selected
		_okButton.setEnabled(((PhotoPreviewTableModel) _previewTable.getModel()).hasPhotosSelected());
		_previewEnabled = true;
	}


	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inFromButton true if triggered from button press, false if automatic
	 */
	public void createPreview(boolean inFromButton)
	{
		// Exit if still on first panel
		if (!_previewEnabled) {return;}
		// Create a TimeDifference based on the edit boxes
		int numHours = getValue(_offsetHourBox.getText());
		int numMins = getValue(_offsetMinBox.getText());
		int numSecs = getValue(_offsetSecBox.getText());
		boolean isPos = _photoLaterOption.isSelected();
		createPreview(new TimeDifference(numHours, numMins, numSecs, isPos), inFromButton);
	}


	/**
	 * Create a preview of the correlate action using the selected time difference
	 * @param inTimeDiff TimeDifference to use for preview
	 * @param inShowWarning true to show warning if all points out of range
	 */
	private void createPreview(TimeDifference inTimeDiff, boolean inShowWarning)
	{
		TimeDifference timeLimit = parseTimeLimit();
		double angDistLimit = parseDistanceLimit();
		PhotoPreviewTableModel model = new PhotoPreviewTableModel();
		PhotoList photos = _app.getTrackInfo().getPhotoList();
		// Loop through photos deciding whether to set correlate flag or not
		int numPhotos = photos.getNumPhotos();
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = photos.getPhoto(i);
			PointPair pair = getPointPairForPhoto(_app.getTrackInfo().getTrack(), photo, inTimeDiff);
			PhotoPreviewTableRow row = new PhotoPreviewTableRow(pair);
			// Don't try to correlate photos which don't have points either side
			boolean correlatePhoto = pair.isValid();
			// Check time limits, distance limits
			if (timeLimit != null && correlatePhoto) {
				long numSecs = pair.getMinSeconds();
				correlatePhoto = (numSecs <= timeLimit.getTotalSeconds());
			}
			if (angDistLimit > 0.0 && correlatePhoto)
			{
				final double angDistPair = DataPoint.calculateRadiansBetween(pair.getPointBefore(), pair.getPointAfter());
				double frac = pair.getFraction();
				if (frac > 0.5) {frac = 1 - frac;}
				final double angDistPhoto = angDistPair * frac;
				correlatePhoto = (angDistPhoto < angDistLimit);
			}
			// Don't select photos which are already correlated to the same point
			if (pair.getSecondsBefore() == 0L && pair.getPointBefore().getPhoto() != null
				&& pair.getPointBefore().getPhoto().equals(photo)) {
				correlatePhoto = false;
			}
			row.setCorrelateFlag(correlatePhoto);
			model.addPhotoRow(row);
		}
		_previewTable.setModel(model);
		// Set distance units
		model.setDistanceUnits(getSelectedDistanceUnits());
		// Set column widths
		_previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		final int[] colWidths = {150, 160, 100, 100, 50};
		for (int i=0; i<model.getColumnCount(); i++) {
			_previewTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
		}
		// check if any photos found
		_okButton.setEnabled(model.hasPhotosSelected());
		if (inShowWarning && !model.hasPhotosSelected())
		{
			JOptionPane.showMessageDialog(_dialog, I18nManager.getText("dialog.correlate.alloutsiderange"),
				I18nManager.getText("dialog.correlate.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Parse the time limit values entered and validate them
	 * @return TimeDifference object describing limit
	 */
	private TimeDifference parseTimeLimit()
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
	private double parseDistanceLimit()
	{
		double value = -1.0;
		if (_distLimitRadio.isSelected())
		{
			try
			{
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
	private int getSelectedDistanceUnits()
	{
		final int[] distUnits = {Distance.UNITS_KILOMETRES, Distance.UNITS_METRES, Distance.UNITS_MILES};
		return distUnits[_distUnitsDropdown.getSelectedIndex()];
	}


	/**
	 * Try to parse the given string
	 * @param inText String to parse
	 * @return value if parseable, 0 otherwise
	 */
	private static int getValue(String inText)
	{
		int value = 0;
		try {
			value = Integer.parseInt(inText);
		}
		catch (NumberFormatException nfe) {}
		return value;
	}


	/**
	 * Get the point pair surrounding the given photo
	 * @param inTrack track object
	 * @param inPhoto photo object
	 * @param inOffset time offset to apply to photos
	 * @return point pair resulting from correlation
	 */
	private static PointPair getPointPairForPhoto(Track inTrack, Photo inPhoto, TimeDifference inOffset)
	{
		PointPair pair = new PointPair(inPhoto);
		// Add offet to photo timestamp
		Timestamp photoStamp = inPhoto.getTimestamp().subtractOffset(inOffset);
		int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			Timestamp pointStamp = point.getTimestamp();
			if (pointStamp != null && pointStamp.isValid())
			{
				long numSeconds = pointStamp.getSecondsSince(photoStamp);
				pair.addPoint(point, numSeconds);
			}
		}
		return pair;
	}


	/**
	 * Construct an array of the point pairs to use for correlation
	 * @return array of PointPair objects
	 */
	private PointPair[] getPointPairs()
	{
		PhotoPreviewTableModel model = (PhotoPreviewTableModel) _previewTable.getModel();
		int numPhotos = model.getRowCount();
		PointPair[] pairs = new PointPair[numPhotos];
		// Loop over photos in preview table model
		for (int i=0; i<numPhotos; i++)
		{
			PhotoPreviewTableRow row = model.getRow(i);
			// add all selected pairs to array (other elements remain null)
			if (row.getCorrelateFlag().booleanValue())
			{
				pairs[i] = row.getPointPair();
			}
		}
		return pairs;
	}

	/**
	 * @return time difference of local time zone from UTC when the first photo was taken
	 */
	private TimeDifference getTimezoneOffset()
	{
		Calendar cal = null;
		// Base time difference on DST when first photo was taken
		Photo firstPhoto = _app.getTrackInfo().getPhotoList().getPhoto(0);
		if (firstPhoto != null && firstPhoto.getTimestamp() != null) {
			cal = firstPhoto.getTimestamp().getCalendar();
		}
		else {
			// No photo or no timestamp, just use current time
			cal = Calendar.getInstance();
		}
		// Both time zone offset and dst offset are based on milliseconds, so convert to seconds
		TimeDifference timeDiff = new TimeDifference((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 1000);
		return timeDiff;
	}


	/**
	 * Calculate the median index to select from the table
	 * @param inModel table model
	 * @return index of entry to select from table
	 */
	private static int getMedianIndex(PhotoSelectionTableModel inModel)
	{
		// make sortable list
		TreeSet set = new TreeSet();
		// loop through rows of table adding to list
		int numRows = inModel.getRowCount();
		int i;
		for (i=0; i<numRows; i++)
		{
			PhotoSelectionTableRow row = inModel.getRow(i);
			set.add(new TimeIndexPair(row.getTimeDiff().getTotalSeconds(), i));
		}
		// pull out middle entry and return index
		TimeIndexPair pair = null;
		Iterator iterator = set.iterator();
		for (i=0; i<(numRows+1)/2; i++)
		{
			pair = (TimeIndexPair) iterator.next();
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
	 * Check if the track has any uncorrelated photos
	 * @return true if there are any photos which are not connected to points
	 */
	private boolean trackHasUncorrelatedPhotos()
	{
		PhotoList photoList = _app.getTrackInfo().getPhotoList();
		int numPhotos = photoList.getNumPhotos();
		// loop over photos
		for (int i=0; i<numPhotos; i++)
		{
			Photo photo = photoList.getPhoto(i);
			if (photo != null && photo.getDataPoint() == null) {
				return true;
			}
		}
		// no uncorrelated photos found
		return false;
	}
}
