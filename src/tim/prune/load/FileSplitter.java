package tim.prune.load;

/**
 * Class responsible for splitting the file contents into an array
 * based on the selected delimiter character
 */
public class FileSplitter
{
	private FileCacher _cacher = null;
	private int _numRows = 0;
	private int _numColumns = 0;
	private boolean[] _columnStates = null;
	private String[] _firstFullRow = null;


	/**
	 * Constructor
	 * @param inCacher FileCacher object holding file contents
	 */
	public FileSplitter(FileCacher inCacher)
	{
		_cacher = inCacher;
	}

	/**
	 * Split the FileCacher's contents into a 2d array
	 * @param inDelim delimiter character
	 * @return 2d Object array
	 */
	public String[][] splitFieldData(char inDelim)
	{
		_firstFullRow = null;
		if (_cacher == null) return null;
		String[] contents = _cacher.getContents();
		if (contents == null || contents.length == 0) return null;
		String delimStr = checkDelimiter(inDelim);
		// Count non-blank rows and max field count
		_numRows = 0;
		int maxFields = 0;
		for (int i=0; i<contents.length; i++)
		{
			if (contents[i] != null && !contents[i].trim().equals(""))
			{
				_numRows++;
				String[] splitLine = contents[i].split(delimStr);
				if (splitLine != null && splitLine.length > maxFields)
				{
					maxFields = splitLine.length;
					_firstFullRow = splitLine;
				}
			}
		}
		_numColumns = maxFields;
		_columnStates = new boolean[maxFields];

		// Create array and populate it
		// Note that array will be rectangular even if data is ragged
		String[][] result = new String[_numRows][];
		for (int i=0; i<contents.length; i++)
		{
			result[i] = new String[maxFields];
			if (contents[i] != null)
			{
				String wholeLine = contents[i];
				if (!wholeLine.trim().equals(""))
				{
					// Don't use trimmed string here because you'll lose empty fields at beginning
					// if separated by spaces or tabs
					String[] splitLine = wholeLine.split(delimStr);
					if (splitLine != null)
					{
						System.arraycopy(splitLine, 0, result[i], 0, splitLine.length);
						// Check if columns are blank or not
						for (int j=0; j<splitLine.length; j++)
						{
							if (!_columnStates[j] && splitLine[j].trim().length() > 0)
							{
								_columnStates[j] = true;
							}
						}
					}
				}
			}
		}
		return result;
	}


	/**
	 * @return the number of rows in the data
	 */
	public int getNumRows()
	{
		return _numRows;
	}


	/**
	 * @return the number of columns in the data
	 */
	public int getNumColumns()
	{
		return _numColumns;
	}

	/**
	 * @return the fields in the first full row
	 */
	public String[] getFirstFullRow()
	{
		return _firstFullRow;
	}


	/**
	 * Check if the specified column of the data is blank
	 * @param inColumnNum number of column, starting with 0
	 * @return true if no data exists in this column
	 */
	public boolean isColumnBlank(int inColumnNum)
	{
		// Should probably trap out of range values
		return !_columnStates[inColumnNum];
	}

	/**
	 * Check the delimiter for proper regular expression matching
	 * @param inDelim character selected as delimiter
	 * @return regular expression for splitting
	 */
	private static String checkDelimiter(char inDelim)
	{
		String result = "" + inDelim;
		// Don't pass asterisks or dots without escaping them for RE
		if (inDelim == '*' || inDelim == '.') {
			result = "\\" + result;
		}
		return result;
	}
}
