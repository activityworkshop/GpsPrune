package tim.prune.load;

import java.io.File;

/**
 * Container class to hold media link information from a loaded file
 * including whether the media files are actual files or inside a kmz / zip
 */
public class MediaLinkInfo
{
	/** zip file (or kmz file) containing media files */
	private File _zipFile = null;
	/** array of URLs */
	private String[] _linkArray = null;


	/**
	 * Constructor for regular files
	 * @param inLinkArray array of links to files
	 */
	public MediaLinkInfo(String[] inLinkArray)
	{
		_zipFile = null;
		_linkArray = inLinkArray;
	}

	/**
	 * Constructor for media files inside a zip / kmz file
	 * @param inZipFile archive file
	 * @param inLinkArray array of file links
	 */
	public MediaLinkInfo(File inZipFile, String[] inLinkArray)
	{
		_zipFile = inZipFile;
		_linkArray = inLinkArray;
	}

	/**
	 * @return true if these media files are inside a zip / kmz
	 */
	public boolean insideArchive() {
		return _zipFile != null && _zipFile.exists();
	}

	/**
	 * @return zip file
	 */
	public File getZipFile() {
		return _zipFile;
	}

	/**
	 * @return link array
	 */
	public String[] getLinkArray() {
		return _linkArray;
	}
}
