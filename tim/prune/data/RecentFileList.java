package tim.prune.data;

/**
 * Class to hold and manage the list of recently used files
 */
public class RecentFileList
{
	private RecentFile[] _files = null;
	private static final int DEFAULT_SIZE = 6;
	private static final int MAX_SIZE = 20;

	/**
	 * Default constructor
	 */
	public RecentFileList()
	{
		_files = new RecentFile[DEFAULT_SIZE];
	}

	/**
	 * Constructor
	 * @param inString String from config
	 */
	public RecentFileList(String inString)
	{
		_files = null;
		int pos = 0;
		if (inString != null && inString.length() > 0)
		{
			for (String s : inString.split(";"))
			{
				if (pos == 0)
				{
					int listSize = DEFAULT_SIZE;
					try
					{
						listSize = Integer.parseInt(s);
						if (listSize < 1 || listSize > MAX_SIZE) {
							listSize = DEFAULT_SIZE;
						}
					}
					catch (NumberFormatException nfe) {}
					_files = new RecentFile[listSize];
					pos++;
				}
				else if (pos <= _files.length)
				{
					RecentFile rf = new RecentFile(s);
					if (rf.isValid())
					{
						_files[pos-1] = rf;
						pos++;
					}
				}
			}
		}
		if (_files == null) {
			_files = new RecentFile[DEFAULT_SIZE];
		}
	}

	/**
	 * @return size of list (may not have this many entries yet)
	 */
	public int getSize()
	{
		if (_files == null) return 0;
		return _files.length;
	}

	/**
	 * @return the number of valid entries in the list
	 */
	public int getNumEntries()
	{
		if (_files == null) return 0;
		int numFound = 0;
		for (RecentFile rf : _files) {
			if (rf != null && rf.isValid())
				numFound++;
		}
		return numFound;
	}

	/**
	 * @return string to save in config
	 */
	public String getConfigString()
	{
		StringBuilder builder = new StringBuilder(100);
		int size = getSize();
		builder.append("" + size);
		for (RecentFile f : _files)
		{
			builder.append(';');
			if (f != null) builder.append(f.getConfigString());
		}
		return builder.toString();
	}

	/**
	 * Add the given file to the top of the list
	 * @param inRF file to add
	 */
	public void addFile(RecentFile inRF)
	{
		// Build a new array with the latest file at the top
		RecentFile[] files = new RecentFile[_files.length];
		int rfIndex = 0;
		if (inRF != null && inRF.isValid())
		{
			files[rfIndex] = inRF;
			rfIndex++;
		}
		// Loop, copying the other files
		for (RecentFile rf : _files)
		{
			if (rf != null && rf.isValid() && (inRF==null || !rf.isSameFile(inRF)))
			{
				files[rfIndex] = rf;
				rfIndex++;
				if (rfIndex >= files.length) break;
			}
		}
		_files = files;
	}

	/**
	 * Verify all the entries and remove the invalid ones
	 */
	public void verifyAll() {
		addFile(null);
	}

	/**
	 * Get the RecentFile object at the given index
	 * @param inIndex index, starting at 0
	 * @return RecentFile object or null if out of range
	 */
	public RecentFile getFile(int inIndex)
	{
		if (inIndex < 0 || inIndex >= _files.length) return null;
		return _files[inIndex];
	}

	/**
	 * Resize the list to the new size
	 * @param inNewSize new size of list
	 */
	public void resizeList(int inNewSize)
	{
		// don't do anything if size doesn't make sense
		if (inNewSize > 0 && inNewSize <= MAX_SIZE)
		{
			RecentFile[] files = new RecentFile[inNewSize];
			int numToCopy = _files.length;
			if (inNewSize < numToCopy) {
				numToCopy = inNewSize;
			}
			System.arraycopy(_files, 0, files, 0, numToCopy);
			_files = files;
		}
	}
}
