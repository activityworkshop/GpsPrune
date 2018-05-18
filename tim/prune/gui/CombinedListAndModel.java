package tim.prune.gui;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import tim.prune.I18nManager;


/**
 * Listbox class which also contains its own string model.
 * Also has the ability to limit its size and show a single
 * text instead of a huge list
 */
public class CombinedListAndModel extends JList<String>
{
	private DefaultListModel<String> _model = null;
	private final int _key;
	private int _maxNumEntries = 0;
	private boolean _tooManyEntries = false;
	private boolean _unlimited = false;


	/**
	 * Constructor
	 */
	public CombinedListAndModel(int inKey)
	{
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_model = new DefaultListModel<String>();
		setModel(_model);
		_key = inKey;
	}

	/**
	 * @param inMaxNum maximum number of entries to allow
	 */
	public void setMaxNumEntries(int inMaxNum)
	{
		_maxNumEntries = inMaxNum;
	}

	/**
	 * @param inUnlimited true if list is temporarily unlimited
	 */
	public void setUnlimited(boolean inUnlimited)
	{
		_unlimited = inUnlimited;
	}

	/**
	 * @return key
	 */
	public int getKey()
	{
		return _key;
	}

	/**
	 * @param inItem String to add to the list
	 */
	public void addItem(String inItem)
	{
		if (!_tooManyEntries)
		{
			_model.addElement(inItem);
			if (_maxNumEntries > 0 && !_unlimited
				&& _model.getSize() > _maxNumEntries)
			{
				_tooManyEntries = true;
				_model.clear();
				_model.addElement(I18nManager.getText("dialog.settimezone.list.toomany"));
			}
		}
	}

	/**
	 * @return the selected String, or null
	 */
	public String getSelectedItem()
	{
		final int selectedIndex = getSelectedIndex();
		if (_tooManyEntries || selectedIndex < 0)
		{
			return null;
		}
		return _model.getElementAt(selectedIndex);
	}

	/**
	 * Clear the list
	 */
	public void clear()
	{
		_model.clear();
		_tooManyEntries = false;
		_unlimited = false;
	}

	/**
	 * @param inItem item to select
	 */
	public void selectItem(String inItem)
	{
		if (!_tooManyEntries && inItem != null)
		{
			this.setSelectedValue(inItem, true);
		}
	}
}
