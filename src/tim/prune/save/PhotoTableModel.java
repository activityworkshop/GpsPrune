package tim.prune.save;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;

/**
 * Class to hold table model information for save exif dialog
 */
public class PhotoTableModel extends AbstractTableModel
{
	private PhotoTableEntry[] _photos = null;
	private int _addIndex = 0;


	/**
	 * Constructor giving list size
	 * @param inSize number of photos
	 */
	public PhotoTableModel(int inSize)
	{
		_photos = new PhotoTableEntry[inSize];
	}


	/**
	 * Set the given PhotoTableEntry object in the array
	 * @param inEntry PhotoTableEntry object describing the photo
	 */
	public void addPhotoInfo(PhotoTableEntry inEntry)
	{
		if (_addIndex < _photos.length && inEntry != null
			&& inEntry.getStatus() != null)
		{
			_photos[_addIndex] = inEntry;
			_addIndex++;
		}
	}

	/**
	 * @return the number of photos in the list whose status has changed
	 */
	public int getNumSaveablePhotos()
	{
		return _addIndex;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 3;
	}


	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return _addIndex;
	}


	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 0)
		{
			return _photos[inRowIndex].getName();
		}
		else if (inColumnIndex == 1)
		{
			return _photos[inRowIndex].getStatus();
		}
		return Boolean.valueOf(_photos[inRowIndex].getSaveFlag());
	}


	/**
	 * @return true if cell is editable
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		// only the save column is editable
		return inColumnIndex == 2;
	}


	/**
	 * Set the given cell value
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		// ignore edits to other columns
		if (inColumnIndex == 2)
			_photos[inRowIndex].setSaveFlag(((Boolean) inValue).booleanValue());
	}


	/**
	 * @return Class of cell data
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex < 2) return String.class;
		return Boolean.class;
	}


	/**
	 * Get the name of the column
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return I18nManager.getText("dialog.saveexif.table.photoname");
		else if (inColNum == 1) return I18nManager.getText("dialog.saveexif.table.status");
		return I18nManager.getText("dialog.saveexif.table.save");
	}


	/**
	 * Retrieve the object at the given index
	 * @param inIndex index, starting at 0
	 * @return PhotoTableEntry object at this position
	 */
	public PhotoTableEntry getPhotoTableEntry(int inIndex)
	{
		if (inIndex < 0 || inIndex >= _photos.length)
		{
			return null;
		}
		return _photos[inIndex];
	}
}
