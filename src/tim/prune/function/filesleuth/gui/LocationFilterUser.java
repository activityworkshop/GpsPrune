package tim.prune.function.filesleuth.gui;

import tim.prune.function.filesleuth.data.LocationFilter;

/** Used by the LocationFilter edit dialog to pass its result back to its caller */
public interface LocationFilterUser {
	void updateLocationFilter(LocationFilter inFilter);
}
