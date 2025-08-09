package tim.prune.function.compress;

import tim.prune.data.MarkingData;

/** Interface used by each panel to communicate to their container */
public interface MethodPanelContainer
{
	void movePanelUp(int inIndex);
	void movePanelDown(int inIndex);
	void deletePanel(int inIndex);

	MarkingData recalculateAll();
}
