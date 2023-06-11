package tim.prune.function.edit;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Edit table with special colouring
 */
public class EditTable extends JTable
{
	/** Constructor */
	public EditTable(TableModel inModel) {
		super(inModel);
	}

	// Paint the changed fields orange
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component comp = super.prepareRenderer(renderer, row, column);
		boolean changed = ((EditFieldsTableModel) getModel()).getChanged(row);
		if (row != getSelectedRow()) {
			comp.setBackground(changed ? Color.orange : getBackground());
		}
		return comp;
	}
}
