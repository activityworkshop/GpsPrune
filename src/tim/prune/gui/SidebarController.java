package tim.prune.gui;

import java.awt.Component;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * Class to control the showing and hiding of the sidebars
 * (left panel, right panel and profile display)
 */
public class SidebarController
{
	/** array of hideable components */
	private final Component[] _components;
	/** array of splitter panes */
	private final JSplitPane[] _splitters;
	/** array of splitter positions */
	private final int[] _positions;


	/**
	 * Constructor
	 * @param inComponents array of components to hide/show
	 * @param inSplitters array of splitter panes
	 */
	public SidebarController(Component[] inComponents, JSplitPane[] inSplitters)
	{
		_components = inComponents;
		_splitters = inSplitters;
		_positions = new int[inSplitters.length];
	}

	/**
	 * Toggle full screen mode on or off
	 */
	public void toggle()
	{
		if (_components != null && _components.length > 0)
		{
			boolean visible = _components[0].isVisible();
			if (visible) {
				// Store divider locations
				for (int i=0; i<_components.length; i++) {
					_positions[i] = _splitters[i].getDividerLocation();
				}
			}
			// Set visibility of components
			for (Component component : _components) {
				component.setVisible(!visible);
			}
			if (!visible)
			{
				// Restore divider locations
				for (int i=0; i<_components.length; i++) {
					_splitters[i].setDividerLocation(_positions[i]);
				}
			}
			// Hiding of panels has to occur in separate thread to update properly
			if (visible)
			{
				SwingUtilities.invokeLater(() -> {
					for (int i=0; i<_components.length; i++)
					{
						_splitters[i].setDividerLocation(i == 0 ? 0.0 : 1.0);
						_splitters[i].invalidate();
					}
				});
			}
		}
	}
}
