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

	/** @return the delimiter character */
	public char getDelimiter()
	{
		return _delimiter;
	}

	/** @return the max number of fields */
	public int getMaxFields()
	{
		return _maxFields;
	}

	/** @param inNumFields number of fields */
	public void updateMaxFields(int inNumFields)
	{
		if (inNumFields > _maxFields)
			_maxFields = inNumFields;
	}

	/** @return the number of records */
	public int getNumRecords()
	{
		return _numRecords;
	}

	/** Increment the number of records */
	public void incrementNumRecords()
	{
		_numRecords++;
	}

	/** @return the number of times this delimiter has won */
	public int getNumWinningRecords()
	{
		return _numWinningRecords;
	}

	/** Increment the number of times this delimiter has won */
	public void incrementNumWinningRecords()
	{
		_numWinningRecords++;
	}

	/** @return String for debug */
	public String toString()
	{
		return "(delim:" + _delimiter + " fields:" + _maxFields + ", records:" + _numRecords + ")";
	}
}
