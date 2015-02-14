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

	// Photos
	private JList _photoList = null;
	private PhotoListModel _photoListModel = null;
	// Waypoints
	private JList _waypointList = null;
	private WaypointListModel _waypointListModel = null;

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
		trackDetailsPanel.add(_filenameLabel);

		// Scroll bar
		_scroller = new JScrollBar(JScrollBar.HORIZONTAL, 0, SCROLLBAR_INTERVAL, 0, 100);
		_scroller.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				selectPoint(e.getValue());
			}
		});
		_scroller.setEnabled(false);

		// Add panel for waypoints / photos
		JPanel listsPanel = new JPanel();
		listsPanel.setLayout(new GridLayout(0, 1));
		listsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		_waypointListModel = new WaypointListModel(_trackInfo.getTrack());
		_waypointList = new JList(_waypointListModel);
		_waypointList.setVisibleRowCount(NUM_LIST_ENTRIES);
		_waypointList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_waypointList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) selectWaypoint(_waypointList.getSelectedIndex());
			}});
		JPanel waypointListPanel = new JPanel();
		waypointListPanel.setLayout(new BorderLayout());
		waypointListPanel.add(new JLabel(I18nManager.getText("details.waypointsphotos.waypoints")), BorderLayout.NORTH);
		waypointListPanel.add(new JScrollPane(_waypointList), BorderLayout.CENTER);
		listsPanel.add(waypointListPanel);
		// photo list
		_photoListModel = new PhotoListModel(_trackInfo.getPhotoList());
		_photoList = new JList(_photoListModel);
		_photoList.setVisibleRowCount(NUM_LIST_ENTRIES);
		_photoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_photoList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting()) {
					selectPhoto(_photoList.getSelectedIndex());
				}
			}});
		JPanel photoListPanel = new JPanel();
		photoListPanel.setLayout(new BorderLayout());
		photoListPanel.add(new JLabel(I18nManager.getText("details.waypointsphotos.photos")), BorderLayout.NORTH);
		photoListPanel.add(new JScrollPane(_photoList), BorderLayout.CENTER);
		listsPanel.add(photoListPanel);
		listsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// add the controls to the main panel
		mainPanel.add(trackDetailsPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(_scroller);
		mainPanel.add(Box.createVerticalStrut(5));

		// add the main panel at the top
		add(mainPanel, BorderLayout.NORTH);
		// and lists in the centre
		add(listsPanel, BorderLayout.CENTER);
		// set preferred width to be small
		setPreferredSize(new Dimension(100, 100));
	}


	/**
	 * Select the specified point
	 * @param inValue value to select
	 */
	private void selectPoint(int inValue)
	{
		if (_track != null && !_ignoreScrollEvents)
		{
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
	 * Select the specified waypoint
	 * @param inWaypointIndex index of selected waypoint
	 */
	private void selectWaypoint(int inWaypointIndex)
	{
		if (inWaypointIndex >= 0)
		{
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
		}
		else
		{
			_trackpointsLabel.setText(I18nManager.getText("details.track.points") + ": "
				+ _track.getNumPoints());
			int numFiles = _trackInfo.getFileInfo().getNumFiles();
			if (numFiles == 1)
			{
				_filenameLabel.setText(I18nManager.getText("details.track.file") + ": "
					+ _trackInfo.getFileInfo().getFilename());
			}
			else if (numFiles > 1)
			{
				_filenameLabel.setText(I18nManager.getText("details.track.numfiles") + ": "
					+ numFiles);
			}
			else _filenameLabel.setText("");
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
		if ((inUpdateType |
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.PHOTOS_MODIFIED)) > 0)
		{
			_photoListModel.fireChanged();
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
	}
}
