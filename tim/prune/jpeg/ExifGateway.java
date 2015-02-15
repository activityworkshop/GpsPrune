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
 * Switching between internal and external libraries is
 * handled by the ExifLibrarySwitch
 */
public abstract class ExifGateway
{
	/** Library object to call */
	private static ExifLibrary _exifLibrary = null;
	/** Flag to set whether failure warning has already been shown */
	private static boolean _exifFailWarned = false;

	/** Static block to initialise library */
	static
	{
		String libraryClass = ExifLibrarySwitch.USE_INTERNAL_LIBRARY?"InternalExifLibrary":"ExternalExifLibrary";
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
		String key = ExifLibrarySwitch.USE_INTERNAL_LIBRARY?"internal":"external";
		if (_exifLibrary == null || !_exifLibrary.looksOK()) {key = key + ".failed";}
		return key;
	}



	/**
	 * @param inNumerator numerator from Rational
	 * @param inDenominator denominator from Rational
	 * @return the value of the specified number as a positive <code>double</code>.
	 * Prevents interpretation of 32 bit numbers as negative, and forces a positive answer
	 */
	public static final double convertToPositiveValue(int inNumerator, int inDenominator)
	{
		if (inDenominator == 0) return 0.0;
		double numeratorDbl = inNumerator;
		double denomDbl = inDenominator;
		if (inNumerator >= 0)
			return numeratorDbl / denomDbl;
		final double correction = Math.pow(2.0, 32);
		numeratorDbl += correction;
		if (inDenominator < 0) denomDbl += correction;
		return numeratorDbl / denomDbl;
	}
}
