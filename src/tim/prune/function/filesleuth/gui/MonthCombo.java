package tim.prune.function.filesleuth.gui;

import javax.swing.JComboBox;

import tim.prune.I18nManager;

public class MonthCombo extends JComboBox<String>
{
	public MonthCombo() {
		super(getItems());
	}

	private static String[] getItems()
	{
		String[] keys = new String[] {"all", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
		String[] items = new String[keys.length];
		for (int i=0; i<keys.length; i++) {
			items[i] = I18nManager.getText("dialog.editdaterange.month." + keys[i]);
		}
		return items;
	}

	public boolean isAllMonths() {
		return getSelectedIndex() == 0;
	}

	public int getMonthNum() {
		return isAllMonths() ? -1 : getSelectedIndex();
	}
}
