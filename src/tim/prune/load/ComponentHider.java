package tim.prune.load;

import java.awt.Component;
import java.util.ArrayList;

import tim.prune.data.Field;

/**
 * Class to hold a list of Components and fields,
 * and then enable or disable them (setEnabled) according
 * to whether those fields are available or not
 */
public class ComponentHider
{
	/**
	 * Record to hold each Component and its Field
	 */
	private record ComponentPair(Component component, Field field) {
	}

	/** list itself */
	private final ArrayList<ComponentPair> _componentList = new ArrayList<ComponentPair>(20);

	/**
	 * Add a new component to be controlled
	 * @param inComponent component to enable/disable
	 * @param inField associated field
	 */
	public void addComponent(Component inComponent, Field inField)
	{
		if (inComponent != null && inField != null) {
			_componentList.add(new ComponentPair(inComponent, inField));
		}
	}

	/**
	 * Enable or disable the components for the given field
	 * @param inField field
	 * @param inEnabled true for enabled, false for disabled
	 */
	public void enableComponents(Field inField, boolean inEnabled)
	{
		for (ComponentPair pair : _componentList)
		{
			if (pair != null && pair.field() == inField) {
				pair.component().setEnabled(inEnabled);
			}
		}
	}
}
