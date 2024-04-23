package tim.prune.function.filesleuth.gui;

import tim.prune.function.filesleuth.data.DateRange;

/** Used by the DateRange edit dialog to pass its result back to its caller */
public interface DateRangeUser {
	void updateDateRange(DateRange inRange);
}
