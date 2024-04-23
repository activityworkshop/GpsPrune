package tim.prune.data;

import java.io.File;

/**
 * Class to hold the source of the point data, including the original file
 * and file type
 */
public class SourceInfo
{
	/** File type of source file */
	public enum FileType {TEXT, GPX, KML, NMEA, GPSBABEL, JSON}

	/** Source file */
	private final File _sourceFile;
	/** Name of source */
	private final String _sourceName;
	/** File type */
	private final FileType _fileType;
	/** File title, if any */
	private String _fileTitle = null;
	/** File description, if any */
	private String _fileDescription = null;
	/** Number of points */
	private int _numPoints = 0;


	/**
	 * Constructor
	 * @param inFile source file
	 * @param inType type of file
	 */
	public SourceInfo(File inFile, FileType inType)
	{
		_sourceFile = inFile;
		_sourceName = inFile.getName();
		_fileType = inType;
	}

	/**
	 * Constructor
	 * @param inName name of source (without file)
	 * @param inType type of file
	 */
	public SourceInfo(String inName, FileType inType)
	{
		_sourceFile = null;
		_sourceName = inName;
		_fileType = inType;
	}

	/**
	 * @param inTitle title of file, eg from <name> tag in gpx
	 */
	public void setFileTitle(String inTitle) {
		_fileTitle = inTitle;
	}

	/**
	 * @param inDesc description of file, eg from <desc> tag in gpx
	 */
	public void setFileDescription(String inDesc) {
		_fileDescription = inDesc;
	}

	/**
	 * @return source file
	 */
	public File getFile() {
		return _sourceFile;
	}

	/**
	 * @return source name
	 */
	public String getName() {
		return _sourceName;
	}

	/**
	 * @return file type of source
	 */
	public FileType getFileType() {
		return _fileType;
	}

	/**
	 * @return title of file
	 */
	public String getFileTitle() {
		return _fileTitle;
	}

	/**
	 * @return description of file
	 */
	public String getFileDescription() {
		return _fileDescription;
	}

	/**
	 * @param inNumPoints the number of points loaded from this source
	 */
	public void setNumPoints(int inNumPoints) {
		_numPoints = inNumPoints;
	}

	/**
	 * @return number of points from this source
	 */
	public int getNumPoints() {
		return _numPoints;
	}
}
