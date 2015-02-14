package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * GUI element to allow the selection of point types for saving,
 * including checkboxes for track points, waypoints, photo points
 * and also a checkbox for the current selection
 */
public class PointTypeSelector extends JPanel
{
	/** Array of checkboxes */
	private JCheckBox[] _checkboxes = new JCheckBox[4];


	/**
	 * Constructor
	 */
	public PointTypeSelector()
	{
		createComponents();
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
	}

	/**
	 * Create the GUI components
	 */
	private void createComponents()
	{
		setLayout(new BorderLayout());
		// Need label to explain what it is
		add(new JLabel(I18nManager.getText("dialog.pointtype.desc")), BorderLayout.NORTH);
		// panel for the checkboxes
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(0, 3, 15, 3));
		final String[] keys = {"track", "waypoint", "photo"};
		for (int i=0; i<3; i++)
		{
			_checkboxes[i] = new JCheckBox(I18nManager.getText("dialog.pointtype." + keys[i]));
			_checkboxes[i].setSelected(true);
			gridPanel.add(_checkboxes[i]);
		}
		add(gridPanel, BorderLayout.CENTER);
		_checkboxes[3] = new JCheckBox(I18nManager.getText("dialog.pointtype.selection"));
		add(_checkboxes[3], BorderLayout.SOUTH);
	}

	/**
	 * Initialize the checkboxes from the given data
	 * @param inTrackInfo TrackInfo object
	 */
	public void init(TrackInfo inTrackInfo)
	{
		// Get whether track has track points, waypoints, photos
		boolean[] flags = {inTrackInfo.getTrack().hasTrackPoints(),
				inTrackInfo.getTrack().hasWaypoints(),
				inTrackInfo.getPhotoList().getNumPhotos() > 0
		};
		// Enable or disable checkboxes according to data present
		for (int i=0; i<3; i++)
		{
			if (flags[i]) {
				_checkboxes[i].setEnabled(true);
			}
			else {
				_checkboxes[i].setSelected(false);
				_checkboxes[i].setEnabled(false);
			}
		}
		_checkboxes[3].setEnabled(inTrackInfo.getSelection().hasRangeSelected());
		_checkboxes[3].setSelected(false);
	}

	/**
	 * @return true if trackpoints selected
	 */
	public boolean getTrackpointsSelected()
	{
		return _checkboxes[0].isSelected();
	}

	/**
	 * @return true if waypoints selected
	 */
	public boolean getWaypointsSelected()
	{
		return _checkboxes[1].isSelected();
	}

	/**
	 * @return true if photo points selected
	 */
	public boolean getPhotopointsSelected()
	{
		return _checkboxes[2].isSelected();
	}

	/**
	 * @return true if only the current selection should be saved
	 */
	public boolean getJustSelection()
	{
		return _checkboxes[3].isSelected();
	}

	/**
	 * @return true if at least one type selected
	 */
	public boolean getAnythingSelected()
	{
		return getTrackpointsSelected() || getWaypointsSelected()
			|| getPhotopointsSelected();
	}
}
