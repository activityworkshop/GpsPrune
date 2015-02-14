package tim.prune.save.xml;

import tim.prune.data.DataPoint;
import tim.prune.data.FileInfo;
import tim.prune.data.SourceInfo;

/**
 * Class to hold a list of GpxCacher objects
 * and get the original source xml for data points
 */
public class GpxCacherList
{
	/** Array of Gpx Cachers */
	private GpxCacher[] _cacherList = null;

	/**
	 * Constructor
	 * @param inInfo file info object
	 */
	public GpxCacherList(FileInfo inInfo)
	{
		int numFiles = inInfo.getNumFiles();
		_cacherList = new GpxCacher[numFiles];
		for (int i=0; i<numFiles; i++) {
			SourceInfo info = inInfo.getSource(i);
			if (info.getFileType() == SourceInfo.FILE_TYPE.GPX) {
				_cacherList[i] = new GpxCacher(info);
			}
		}
	}

	/**
	 * Get the source for the given data point
	 * @param inPoint point to look for
	 * @return source string or null if not found
	 */
	public String getSourceString(DataPoint inPoint)
	{
		String str = null;
		// Loop over sources
		for (int i=0; i<_cacherList.length && (str == null); i++) {
			GpxCacher cacher = _cacherList[i];
			if (cacher != null) {
				str = cacher.getSourceString(inPoint);
			}
		}
		return str;
	}

	/**
	 * @return the first non-empty header from the list
	 */
	public String getFirstHeader()
	{
		String str = null;
		// Loop over sources
		for (int i=0; i<_cacherList.length && (str == null || str.equals("")); i++)
		{
			GpxCacher cacher = _cacherList[i];
			if (cacher != null) {
				str = cacher.getHeaderString();
			}
		}
		return str;
	}
}
