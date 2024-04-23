package tim.prune.function.filesleuth;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import tim.prune.function.filesleuth.data.Filter;
import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileList;
import tim.prune.function.filesleuth.data.TrackFileStatus;
import tim.prune.function.filesleuth.data.Filter.Comparison;
import tim.prune.function.filesleuth.extract.ContentExtractor;
import tim.prune.function.filesleuth.extract.ExtractorFactory;
import tim.prune.function.filesleuth.gui.ResultsTableModel;


/** Responsible for matching texts, dates and positions in the tracks using a Filter */
public class TrackMatcher implements TrackListListener
{
	private final TrackFileList _tracks;
	private final ResultsTableModel _tableModel;
	private boolean _searching = false;
	private int _searchFromIndex = -1;
	private Filter _filter = Filter.EMPTY_FILTER;
	private final ArrayList<SearchResult> _results = new ArrayList<>();


	public TrackMatcher(TrackFileList inTracks, ResultsTableModel inModel)
	{
		_tracks = inTracks;
		_tableModel = inModel;
		_tracks.addListener(this);
	}

	public void setFilter(Filter inFilter)
	{
		if (inFilter == null) {
			inFilter = Filter.EMPTY_FILTER;
		}
		Filter.Comparison filterChange = Filter.compare(_filter, inFilter);
		if (filterChange == Filter.Comparison.SAME) {
			return;
		}
		_filter = inFilter;
		resetCheckFlags(filterChange);
		setIndexOrStart(0);
	}

	@Override
	public void reactToTrackListChange(int inIndex) {
		setIndexOrStart(inIndex);
	}

	private synchronized void setIndexOrStart(int inIndex)
	{
		// Either start new thread, or signal that current one should continue / reset
		if (_searching) {
			_searchFromIndex = _searchFromIndex < 0 ? inIndex : Math.min(_searchFromIndex, inIndex);
		}
		else
		{
			_searchFromIndex = inIndex;
			_searching = true;
			new Thread(this::run).start();
		}
	}

	private synchronized int checkContinue()
	{
		_searching = _searchFromIndex >= 0;
		return _searchFromIndex;
	}

	private synchronized int getSearchFromIndex() {
		return _searchFromIndex;
	}

	private synchronized void clearSearchFromIndex() {
		_searchFromIndex = -1;
	}

	public void run()
	{
		int startIndex = Math.min(_results.size(), getSearchFromIndex());
		startIndex = Math.max(startIndex, 0);
		do
		{
			clearSearchFromIndex();
			List<TrackFile> tracks = _tracks.getCurrentContents();
			// Loop and process
			for (int i=startIndex; i<tracks.size(); i++)
			{
				TrackFile track = tracks.get(i);
				TrackFileStatus currStatus = track.getStatus();
				final SearchResult result;
				if (i >= _results.size())
				{
					result = new SearchResult(track);
					_results.add(result);
				}
				else {
					result = _results.get(i);
				}
				if (currStatus == result.getFileStatusWhenChecked() && !result.needsRecheck()) {
					continue;
				}
				_filter.apply(result);
				// Maybe need to do a fine location search too?
				if (result.isMatch() && _filter.hasLocationFilter())
				{
					ContentExtractor extractor = ExtractorFactory.createExtractor(track.getFile());
					if (extractor == null || !extractor.matchesFilter(_filter.getLocationFilter())) {
						result.setIsMatch(false);
					}
				}
			}
			// Build new list and pass to table model
			ArrayList<SearchResult> resultsForTable = new ArrayList<>();
			for (SearchResult result : _results)
			{
				if (result.isMatch()) {
					resultsForTable.add(result);
				}
				SwingUtilities.invokeLater(() -> _tableModel.setResults(resultsForTable));
			}
			// See if another thread asked us to continue
			startIndex = checkContinue();
		}
		while (startIndex >= 0);
	}


	/** Depending on the filter change, reset the appropriate check flags to recheck those finds */
	private void resetCheckFlags(Filter.Comparison inFilterChange)
	{
		boolean matchFlagToRecheck = (inFilterChange == Comparison.NARROWER);
		for (SearchResult result : _results)
		{
			if (result.isMatch() == matchFlagToRecheck) {
				result.setNeedsRecheck();
			}
		}
	}
}
