package tim.prune.jpeg;

import java.io.File;

import javax.swing.JOptionPane;

import tim.prune.I18nManager;

/**
 * Skeleton gateway to the Exif functions.
 * This is required by Debian to divert Exif handling
 * to the external libmetadata-extractor-java library
 * instead of the included modified routines.
 *
 * To use the internal routines, set the USE_INTERNAL_LIBRARY flag to true
 * and include the internal classes in the compiled jar.
 * To use the external library, set the USE_INTERNAL_LIBRARY flag to false
 * and do not export the internal classes.
 */
public abstract class ExifGateway
{
	// *********************************************************
	// TODO: Check this exif library flag before releasing!
	/** Flag to specify internal or external library */
	private static final boolean USE_INTERNAL_LIBRARY = true;
	// *********************************************************

	/** Library object to call */
	private static ExifLibrary _exifLibrary = null;
	/** Flag to set whether failure warning has already been shown */
	private static boolean _exifFailWarned = false;

	/** Static block to initialise library */
	static
	{
		String libraryClass = USE_INTERNAL_LIBRARY?"InternalExifLibrary":"ExternalExifLibrary";
		try
		{
			_exifLibrary = (ExifLibrary) Class.forName("tim.prune.jpeg." + libraryClass).newInstance();
		}
		catch (Throwable nolib) {_exifLibrary = null;}
	}


	/**
	 * Get the Jpeg data from the given file
	 * @param inFile file to read
	 * @return jpeg data, or null if none found
	 */
	public static JpegData getJpegData(File inFile)
	{
		try
		{
			// Call library (if found)
			if (_exifLibrary != null) {
				JpegData data = _exifLibrary.getJpegData(inFile);
				return data;
			}
		}
		catch (LinkageError nolib) {}
		// Not successful - warn if necessary
		if (!_exifFailWarned)
		{
			JOptionPane.showMessageDialog(null, I18nManager.getText("error.jpegload.exifreadfailed"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.WARNING_MESSAGE);
			_exifFailWarned = true;
		}
		return null;
	}

	/**
	 * @return key to use to describe library, matching key for about dialog
	 */
	public static String getDescriptionKey()
	{
		String key = USE_INTERNAL_LIBRARY?"internal":"external";
		if (_exifLibrary == null || !_exifLibrary.looksOK()) {key = key + ".failed";}
		return key;
	}
}
