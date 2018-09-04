package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Class to allow selection of points and photos
 * as a visual component
 */
public class SelectorDisplay extends GenericDisplay
{
	// Track details
	private JLabel _trackpointsLabel = null;
	private JLabel _filenameLabel = null;
	// Scroll bar
	private JScrollBar _scroller = null;
	private boolean _ignoreScrollEvents = false;

	// Panel containing lists
	private JPanel _listsPanel = null;
	private int _visiblePanels = 1;
	// Waypoints
	private JPanel _waypointListPanel = null;
	private JList<String> _waypointList = null;
	private WaypointListModel _waypointListModel = null;
	// Photos
	private JPanel _photoListPanel = null;
	private JList<String> _photoList = null;
	private MediaListModel _photoListModel = null;
	// Audio files
	private JPanel _audioListPanel = null;
	private JList<String> _audioList = null;
	private MediaListModel _audioListModel = null;

	// scrollbar interval
	private static final int SCROLLBAR_INTERVAL = 50;
	// number of rows in lists
	private static final int NUM_LIST_ENTRIES = 7;


	/**
	 * Constructor
	 * @param inTrackInfo Track info object
	 */
	public SelectorDisplay(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		// Track details panel
		JPanel trackDetailsPanel = new JPanel();
		trackDetailsPanel.setLayout(new BoxLayout(trackDetailsPanel, BoxLayout.Y_AXIS));
		trackDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		JLabel trackDetailsLabel = new JLabel(I18nManager.getText("details.trackdetails"));
		Font biggerFont = trackDetailsLabel.getFont();
		biggerFont = biggerFont.deriveFont(Font.BOLD, biggerFont.getSize2D() + 2.0f);
		trackDetailsLabel.setFont(biggerFont);
		trackDetailsPanel.add(trackDetailsLabel);
		_trackpointsLabel = new JLabel(I18nManager.getText("details.notrack"));
		trackDetailsPanel.add(_trackpointsLabel);
		_filenameLabel = new JLabel("");
		_filenameLabel.setMinimumSize(new Dimension(120, 10));
		trackDetailsPanel.add(_filenameLabel);

		// Scroll bar
		_scroller = new JScrollBar(JScrollBar.HORIZONTAL, 0, SCROLLBAR_INTERVAL, 0, 100);
		_scroller.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				selectPoint(e.getValue());
			}
		});
		_scroller.setEnabled(false);

		// Add panel for waypoints / photos
		_listsPanel = new JPanel();
		_listsPanel.setLayout(new GridLayout(0, 1));
		_listsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		_waypointListModel = new WaypointListModel(_trackInfo.getTrack());
		_waypointList = new JList<String>(_waypointListModel);
		_waypointList.setVisibleRowCount(NUM_LIST_ENTRIES);
		_waypointList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) selectWaypoint(_waypointList.getSelectedIndex());
			}
		});
		_waypointListPanel = makeListPanel("details.lists.waypoints", _waypointList);
		_listsPanel.add(_waypointListPanel);
		// photo list
		_photoListModel = new MediaListModel(_trackInfo.getPhotoList());
		_photoList = new JList<String>(_photoListModel);
		_photoList.setVisibleRowCount(NUM_LIST_ENTRIES);
		_photoList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) {
					selectPhoto(_photoList.getSelectedIndex());
				}
			}});
		_photoListPanel = makeListPanel("details.lists.photos", _photoList);
		// don't add photo list (because there aren't any photos yet)

		// List for audio clips
		_audioListModel = new MediaListModel(_trackInfo.getAudioList());
		_audioList = new JList<String>(_audioListModel);
		_audioList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) {
					selectAudio(_audioList.getSelectedIndex());
				}
			}});
		_audioListPanel = makeListPanel("details.lists.audio", _audioList);
		// don't add audio list either
		_listsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// add the controls to the main panel
		mainPanel.add(trackDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(_scroller);
		mainPanel.add(Box.createVerticalStrut(5));

		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);
		// and lists in the centre
		add(_listsPanel, BorderLayout.CENTER);
		// set preferred width to be small
		setPreferredSize(new Dimension(100, 100));
	}


	/**
	 * Select the specified point
	 * @param inValue value to select
	 */
	private void selectPoint(int inValue)
	{
		if (_track != null && !_ignoreScrollEvents) {
			_trackInfo.selectPoint(inValue);
		}
	}


	/**
	 * Select the specified photo
	 * @param inPhotoIndex index of selected photo
	 */
	private void selectPhoto(int inPhotoIndex)
	{
		_trackInfo.selectPhoto(inPhotoIndex);
	}

	/**
	 * Select the specified audio clip
	 * @param inIndex index of selected audio clip
	 */
	private void selectAudio(int inIndex)
	{
		_trackInfo.selectAudio(inIndex);
	}

	/**
	 * Select the specified waypoint
	 * @param inWaypointIndex index of selected waypoint
	 */
	private void selectWaypoint(int inWaypointIndex)
	{
		if (inWaypointIndex >= 0) {
			_trackInfo.selectPoint(_waypointListModel.getWaypoint(inWaypointIndex));
		}
	}


	/**
	 * Notification that Track has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Update track data
		if (_track == null || _track.getNumPoints() <= 0)
		{
			_trackpointsLabel.setText(I18nManager.getText("details.notrack"));
			_filenameLabel.setText("");
			_filenameLabel.setToolTipText("");
		}
		else
		{
			_trackpointsLabel.setText(I18nManager.getText("details.track.points") + ": "
				+ _track.getNumPoints());
			int numFiles = _trackInfo.getFileInfo().getNumFiles();
			if (numFiles == 1)
			{
				final String filenameString = _trackInfo.getFileInfo().getFilename();
				_filenameLabel.setText(I18nManager.getText("details.track.file") + ": "
					+ filenameString);
				_filenameLabel.setToolTipText(filenameString);
			}
			else if (numFiles > 1)
			{
				final String labelText = I18nManager.getText("details.track.numfiles") + ": " + numFiles;
				_filenameLabel.setText(labelText);
				_filenameLabel.setToolTipText(labelText);
			}
			else
			{
				_filenameLabel.setText("");
				_filenameLabel.setToolTipText("");
			}
		}

		// Update scroller settings
		int currentPointIndex = _trackInfo.getSelection().getCurrentPointIndex();
		_ignoreScrollEvents = true;
		if (_track == null || _track.getNumPoints() < 2)
		{
			// careful to avoid event loops here
			// _scroller.setValue(0);
			_scroller.setEnabled(false);
		}
		else
		{
			_scroller.setMaximum(_track.getNumPoints() -1 + SCROLLBAR_INTERVAL);
			if (currentPointIndex >= 0)
				_scroller.setValue(currentPointIndex);
			_scroller.setEnabled(true);
		}
		_ignoreScrollEvents = false;

		// update waypoints and photos if necessary
		if ((inUpdateType |
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.WAYPOINTS_MODIFIED)) > 0)
		{
			_waypointListModel.fireChanged();
		}
		if ((inUpdateType &
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.PHOTOS_MODIFIED)) > 0)
		{
			_photoListModel.fireChanged();
			_audioListModel.fireChanged();
		}
		// Deselect selected waypoint if selected point has since changed
		if (_waypointList.getSelectedIndex() >= 0)
		{
			if (_trackInfo.getCurrentPoint() == null
			 || _waypointList.getSelectedIndex() >= _waypointListModel.getSize()
			 || !_waypointListModel.getWaypoint(_waypointList.getSelectedIndex()).equals(_trackInfo.getCurrentPoint()))
			{
				// point is selected in list but different from current point - deselect
				_waypointList.clearSelection();
			}
		}
		// Hide photo list if no photos loaded, same for audio
		redrawLists(_photoListModel.getSize() > 0, _audioListModel.getSize() > 0);

		// Make sure correct photo is selected
		if (_photoListModel.getSize() > 0)
		{
			int photoIndex = _trackInfo.getSelection().getCurrentPhotoIndex();
			int listSelection = _photoList.getSelectedIndex();
			// Change listbox selection if indexes not equal
			if (listSelection != photoIndex)
			{
				if (photoIndex < 0) {
					_photoList.clearSelection();
				}
				else {
					_photoList.setSelectedIndex(photoIndex);
				}
			}
		}
		// Same for audio clips
		if (_audioListModel.getSize() > 0)
		{
			int audioIndex = _trackInfo.getSelection().getCurrentAudioIndex();
			int listSelection = _audioList.getSelectedIndex();
			// Change listbox selection if indexes not equal
			if (listSelection != audioIndex)
			{
				if (audioIndex < 0) {
					_audioList.clearSelection();
				}
				else {
					_audioList.setSelectedIndex(audioIndex);
				}
			}
		}
	}

	/**
	 * Make one of the three list panels
	 * @param inNameKey key for heading text
	 * @param inList list object
	 * @return panel object
	 */
	private static JPanel makeListPanel(String inNameKey, JList<String> inList)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(I18nManager.getText(inNameKey)), BorderLayout.NORTH);
		inList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(inList), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Redraw the list panels in the display according to which ones should be shown
	 * @param inShowPhotos true to show photo list
	 * @param inShowAudio true to show audio list
	 */
	private void redrawLists(boolean inShowPhotos, boolean inShowAudio)
	{
		// exit if same as last time
		int panels = 1 + (inShowPhotos?2:0) + (inShowAudio?4:0);
		if (panels == _visiblePanels) return;
		_visiblePanels = panels;
		// remove all panels and re-add them
		_listsPanel.removeAll();
		_listsPanel.setLayout(new GridLayout(0, 1));
		_listsPanel.add(_waypointListPanel);
		if (inShowPhotos) {
			_listsPanel.add(_photoListPanel);
		}
		if (inShowAudio) {
			_listsPanel.add(_audioListPanel);
		}
		_listsPanel.invalidate();
		_listsPanel.getParent().validate();
	}
}
