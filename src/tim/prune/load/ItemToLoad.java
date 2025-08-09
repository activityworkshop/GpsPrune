package tim.prune.load;

import java.io.File;
import java.net.URL;

import tim.prune.data.DataPoint;

/**
 * Represents something to load, either a single data file,
 * or a media file within a zip archive, or a media file
 * loaded from a URL
 */
public class ItemToLoad
{
	private final File _dataFile;
	private final File _zipFile;
	private final String _itemPathWithinZip;
	private final URL _url;
	private final DataPoint _point;
	private BlockStatus _blockStatus = BlockStatus.NOT_ASKED;

	/** Remember whether this item was blocked or allowed or never asked */
	public enum BlockStatus {NOT_ASKED, ASKED, ALLOW, BLOCK};


	/**
	 * Constructor
	 */
	private ItemToLoad(File inDataFile, DataPoint inPoint, File inZipFile, String inPathWithinZip, URL inUrl)
	{
		_dataFile = inDataFile;
		_point = inPoint;
		_zipFile = inZipFile;
		_itemPathWithinZip = inPathWithinZip;
		_url = inUrl;
	}

	public static ItemToLoad dataFile(File inFile) {
		return new ItemToLoad(inFile, null, null, null, null);
	}

	public static ItemToLoad archivedFile(File inZipFile, String inItemPath, DataPoint inPoint) {
		return new ItemToLoad(null, inPoint, inZipFile, inItemPath, null);
	}

	public static ItemToLoad mediaUrl(URL inUrl, DataPoint inPoint) {
		return new ItemToLoad(null, inPoint, null, null, inUrl);
	}

	public boolean isDataFile() {
		return _dataFile != null && _dataFile.exists() && _dataFile.canRead();
	}

	public File getDataFile() {
		return _dataFile;
	}

	public boolean isArchivedFile() {
		return _zipFile != null && _zipFile.exists() && _zipFile.canRead()
			&& _itemPathWithinZip != null && !_itemPathWithinZip.isEmpty();
	}

	public File getArchiveFile() {
		return _zipFile;
	}

	public String getItemPath() {
		return _itemPathWithinZip;
	}

	public boolean isUrl() {
		return _url != null;
	}

	public URL getUrl() {
		return _url;
	}

	public DataPoint getPoint() {
		return _point;
	}

	public void setBlockStatus(BlockStatus inStatus) {
		_blockStatus = inStatus;
	}

	public BlockStatus getBlockStatus() {
		return _blockStatus;
	}
}
