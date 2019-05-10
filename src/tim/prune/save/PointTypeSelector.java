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
 * including checkboxes for track points, waypoints, photo points, audio points
 * and also a checkbox for the current selection
 */
public class PointTypeSelector extends JPanel
{
	/** Array of checkboxes */
	private JCheckBox[] _checkboxes = new JCheckBox[5];
	/** Grid panel for top row */
	private JPanel _gridPanel = null;


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
		add(new JLabel(I18nManager.getText("dialog.pointtype.desc")), BorderLayout.NORTH);
		// panel for the checkboxes
		_gridPanel = new JPanel();
		_gridPanel.setLayout(new GridLayout(0, 3, 15, 3));
		final String[] keys = {"track", "waypoint", "photo", "audio"};
		for (int i=0; i<4; i++)
		{
			_checkboxes[i] = new JCheckBox(I18nManager.getText("dialog.pointtype." + keys[i]));
			_checkboxes[i].setSelected(true);
			if (i<3) {_gridPanel.add(_checkboxes[i]);}
		}
		add(_gridPanel, BorderLayout.CENTER);
		_checkboxes[4] = new JCheckBox(I18nManager.getText("dialog.pointtype.selection"));
		add(_checkboxes[4], BorderLayout.SOUTH);
	}


	/**
	 * Initialize the checkboxes from the given data
	 * @param inTrackInfo TrackInfo object
	 */
	public void init(TrackInfo inTrackInfo)
	{
		// Get whether track has track points, waypoints, photos
		boolean[] dataFlags = {inTrackInfo.getTrack().hasTrackPoints(),
				inTrackInfo.getTrack().hasWaypoints(),
				inTrackInfo.getPhotoList().getNumPhotos() > 0,
				inTrackInfo.getAudioList().getNumAudios() > 0
		};
		// Rearrange grid to just show the appropriate entries
		final boolean[] showFlags = {true, true, dataFlags[2] || !dataFlags[3], dataFlags[3]};
		_gridPanel.removeAll();
		for (int i=0; i<4; i++) {
			if (showFlags[i]) {_gridPanel.add(_checkboxes[i]);}
		}
		// Enable or disable checkboxes according to data present
		for (int i=0; i<4; i++)
		{
			if (dataFlags[i]) {
				if (!_checkboxes[i].isEnabled()) {_checkboxes[i].setSelected(true);}
				_checkboxes[i].setEnabled(true);
			}
			else {
				_checkboxes[i].setSelected(false);
				_checkboxes[i].setEnabled(false);
			}
		}
		_checkboxes[4].setEnabled(inTrackInfo.getSelection().hasRangeSelected());
		_checkboxes[4].setSelected(false);
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
	 * @return true if audio points selected
	 */
	public boolean getAudiopointsSelected()
	{
		return _checkboxes[3].isSelected();
	}

	/**
	 * @return true if only the current selection should be saved
	 */
	public boolean getJustSelection()
	{
		return _checkboxes[4].isSelected();
	}

	/**
	 * @return true if at least one type selected
	 */
	public boolean getAnythingSelected()
	{
		return getTrackpointsSelected() || getWaypointsSelected()
			|| getPhotopointsSelected() || getAudiopointsSelected();
	}
}
