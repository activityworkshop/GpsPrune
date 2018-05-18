package tim.prune.function.settings;

import javax.swing.AbstractListModel;

import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;

/**
 * Class to act as list model for the map source list
 */
public class MapSourceListModel extends AbstractListModel<String>
{
	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize()
	{
		return MapSourceLibrary.getNumSources();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public String getElementAt(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getSize()) return "";
		return MapSourceLibrary.getSource(inIndex).getName();
	}

	/**
	 * @param inIndex index in list
	 * @return corresponding map source object
	 */
	public MapSource getSource(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getSize()) return null;
		return MapSourceLibrary.getSource(inIndex);
	}

	/**
	 * Fire event to notify that contents have changed
	 */
	public void fireChanged()
	{
		this.fireContentsChanged(this, 0, getSize()-1);
	}
}
