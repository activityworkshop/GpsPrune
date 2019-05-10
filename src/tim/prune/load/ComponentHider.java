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
	 * Inner class to hold each Component and its Field
	 */
	static class ComponentPair
	{
		public Component _component = null;
		public Field     _field     = null;
		/** Constructor */
		public ComponentPair(Component inComponent, Field inField)
		{
			_component = inComponent;
			_field     = inField;
		}
	}

	/** list itself */
	private ArrayList<ComponentPair> _componentList = new ArrayList<ComponentPair>(20);

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
			if (pair != null && pair._field == inField) {
				pair._component.setEnabled(inEnabled);
			}
		}
	}
}
