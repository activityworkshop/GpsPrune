package tim.prune.function.filesleuth.gui;

import java.io.File;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import tim.prune.I18nManager;
import tim.prune.function.filesleuth.SearchResult;


/** Table model to hold the results of the search */
public class ResultsTableModel extends DefaultTableModel
{
	private List<SearchResult> _results = null;


	public void setResults(List<SearchResult> inResults)
	{
		_results = inResults;
		fireTableStructureChanged();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int inColNum) {
		return I18nManager.getText("dialog.findfile.resultscolumn."
			+ (inColNum == 0 ? "file" : "contents"));
	}

	@Override
	public int getRowCount() {
		return _results == null ? 0 : _results.size();
	}

	@Override
	public Object getValueAt(int inRowNum, int inColNum)
	{
		if (inRowNum >= getRowCount()) {
			return "";
		}
		if (inColNum == 0) {
			return _results.get(inRowNum).getFilename();
		}
		return _results.get(inRowNum).getContents();
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	/** @return the file at the given index */
	public File getFile(int inIndex) {
		return _results.get(inIndex).getTrackFile().getFile();
	}
}
