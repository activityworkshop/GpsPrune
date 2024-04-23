package tim.prune.function.weather;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import tim.prune.gui.IconManager;

/**
 * Class to render the weather icons in the table
 */
public class IconRenderer extends JLabel implements TableCellRenderer
{
	private final IconManager _iconManager;

	/** @param inIconManager icon manager for weather symbols */
	public IconRenderer(IconManager inIconManager) {
		_iconManager = inIconManager;
	}

	/** Get the renderer component for the given row, column and value */
	public Component getTableCellRendererComponent(JTable inTable, Object inValue, boolean inIsSelected,
		boolean inHasFocus, int inRow, int inColumn)
	{
		if (inValue != null) {
			setIcon(_iconManager.getImageIcon("weather/" + inValue.toString()));
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		else {
			setIcon(null);
			setText("");
		}
		return this;
	}

	/** Override the minimum size method */
	public Dimension getMinimumSize() {
		return new Dimension(52, 52);
	}
}
