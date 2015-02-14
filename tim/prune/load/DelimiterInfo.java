package tim.prune.load;

/**
 * Class to hold information about the contents of a file
 * given a delimiter character
 */
public class DelimiterInfo
{
	private char _delimiter = '\0';
	private int _numRecords = 0;
	private int _numWinningRecords = 0;
	private int _maxFields = 0;


	/**
	 * Constructor
	 * @param inChar delimiter character
	 */
	public DelimiterInfo(char inChar)
	{
		_delimiter = inChar;
	}

	public char getDelimiter()
	{
		return _delimiter;
	}

	public int getMaxFields()
	{
		return _maxFields;
	}

	public void updateMaxFields(int inNumields)
	{
		if (inNumields > _maxFields)
			_maxFields = inNumields;
	}


	public int getNumRecords()
	{
		return _numRecords;
	}
	public void incrementNumRecords()
	{
		_numRecords++;
	}

	public int getNumWinningRecords()
	{
		return _numWinningRecords;
	}
	public void incrementNumWinningRecords()
	{
		_numWinningRecords++;
	}

	public String toString()
	{
		return "(delim:" + _delimiter + " fields:" + _maxFields + ", records:" + _numRecords + ")";
	}
}
