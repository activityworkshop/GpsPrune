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
	private final GpxCacher[] _cacherList;

	/**
	 * Constructor
	 * @param inInfo file info object
	 */
	public GpxCacherList(FileInfo inInfo)
	{
		int numFiles = inInfo.getNumFiles();
		_cacherList = new GpxCacher[numFiles];
		for (int i=0; i<numFiles; i++)
		{
			SourceInfo info = inInfo.getSource(i);
			if (info.getFileType() == SourceInfo.FileType.GPX) {
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
		// Loop over sources
		for (GpxCacher cacher : _cacherList)
		{
			String src = (cacher == null ? null : cacher.getSourceString(inPoint));
			if (src != null) {
				return src;
			}
		}
		return null;
	}

	/**
	 * @return the first non-empty header from the list
	 */
	public String getFirstHeader()
	{
		// Loop over sources
		for (GpxCacher cacher : _cacherList)
		{
			String str = (cacher == null ? null : cacher.getHeaderString());
			if (str != null && !str.isEmpty()) {
				return str;
			}
		}
		return null;
	}
}
