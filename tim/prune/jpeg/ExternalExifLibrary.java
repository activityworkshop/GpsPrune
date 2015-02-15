package tim.prune.jpeg;

import java.io.File;

import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.GpsDirectory;

/**
 * Class to act as a gateway into the external exif library functions.
 * This should be the only class with dependence on the lib-metadata-extractor-java
 * classes (which are NOT delivered with GpsPrune).
 * This class will not compile without this extra dependency (but is not required if
 * the ExifGateway uses the InternalExifLibrary instead).
 * Should not be included if the internal library will be used (from jpeg.drew package).
 */
public class ExternalExifLibrary implements ExifLibrary
{
	/**
	 * Use the _external_ exif library to get the data from the given file
	 * @param inFile file to access
	 * @return Jpeg data if available, otherwise null
	 */
	public JpegData getJpegData(File inFile)
	{
		JpegData data = new JpegData();
		// Read exif data from picture
		try
		{
			Metadata metadata = new Metadata();
			new ExifReader(inFile).extract(metadata);
			if (metadata.containsDirectory(GpsDirectory.class))
			{
				Directory gpsdir = metadata.getDirectory(GpsDirectory.class);
				if (gpsdir.containsTag(GpsDirectory.TAG_GPS_LATITUDE)
					&& gpsdir.containsTag(GpsDirectory.TAG_GPS_LONGITUDE)
					&& gpsdir.containsTag(GpsDirectory.TAG_GPS_LATITUDE_REF)
					&& gpsdir.containsTag(GpsDirectory.TAG_GPS_LONGITUDE_REF))
				{
					data.setLatitudeRef(gpsdir.getString(GpsDirectory.TAG_GPS_LATITUDE_REF));
					Rational[] latRats = gpsdir.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
					double seconds = ExifGateway.convertToPositiveValue(latRats[2].getNumerator(), latRats[2].getDenominator());
					data.setLatitude(new double[] {latRats[0].doubleValue(),
						latRats[1].doubleValue(), seconds});
					data.setLongitudeRef(gpsdir.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF));
					Rational[] lonRats = gpsdir.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
					seconds = ExifGateway.convertToPositiveValue(lonRats[2].getNumerator(), lonRats[2].getDenominator());
					data.setLongitude(new double[] {lonRats[0].doubleValue(),
						lonRats[1].doubleValue(), seconds});
				}

				// Altitude (if present)
				if (gpsdir.containsTag(GpsDirectory.TAG_GPS_ALTITUDE) && gpsdir.containsTag(GpsDirectory.TAG_GPS_ALTITUDE_REF))
				{
					data.setAltitude(gpsdir.getRational(GpsDirectory.TAG_GPS_ALTITUDE).intValue());
					byte altRef = (byte) gpsdir.getInt(GpsDirectory.TAG_GPS_ALTITUDE_REF);
					data.setAltitudeRef(altRef);
				}

				try
				{
					// Timestamp and datestamp (if present)
					final int TAG_GPS_DATESTAMP = 0x001d;
					if (gpsdir.containsTag(GpsDirectory.TAG_GPS_TIME_STAMP) && gpsdir.containsTag(TAG_GPS_DATESTAMP))
					{
						Rational[] times = gpsdir.getRationalArray(GpsDirectory.TAG_GPS_TIME_STAMP);
						data.setGpsTimestamp(new int[] {times[0].intValue(), times[1].intValue(),
							times[2].intValue()});
						Rational[] dates = gpsdir.getRationalArray(TAG_GPS_DATESTAMP);
						if (dates != null) {
							data.setGpsDatestamp(new int[] {dates[0].intValue(), dates[1].intValue(), dates[2].intValue()});
						}
					}
				}
				catch (MetadataException me) {} // ignore, use other tags instead

				// Image bearing (if present)
				if (gpsdir.containsTag(GpsDirectory.TAG_GPS_IMG_DIRECTION) && gpsdir.containsTag(GpsDirectory.TAG_GPS_IMG_DIRECTION_REF))
				{
					Rational bearing = gpsdir.getRational(GpsDirectory.TAG_GPS_IMG_DIRECTION);
					if (bearing != null) {
						data.setBearing(bearing.doubleValue());
					}
				}
			}

			// Tags from Exif directory
			if (metadata.containsDirectory(ExifDirectory.class))
			{
				Directory exifdir = metadata.getDirectory(ExifDirectory.class);

				// Take time and date from exif tags
				if (exifdir.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL)) {
					data.setOriginalTimestamp(exifdir.getString(ExifDirectory.TAG_DATETIME_ORIGINAL));
				}
				// Also take "digitized" timestamp
				if (exifdir.containsTag(ExifDirectory.TAG_DATETIME_DIGITIZED)) {
					data.setDigitizedTimestamp(exifdir.getString(ExifDirectory.TAG_DATETIME_DIGITIZED));
				}

				// Photo rotation code
				if (exifdir.containsTag(ExifDirectory.TAG_ORIENTATION)) {
					data.setOrientationCode(exifdir.getInt(ExifDirectory.TAG_ORIENTATION));
					// NOTE: this presumably takes the _last_ orientation value found, not the first.
				}

				// Thumbnail
				if (exifdir.containsTag(ExifDirectory.TAG_THUMBNAIL_DATA))
				{
					// Make a copy of the byte data rather than keeping a reference to extracted array
					byte[] tdata = exifdir.getByteArray(ExifDirectory.TAG_THUMBNAIL_DATA);
					byte[] thumb = new byte[tdata.length];
					System.arraycopy(tdata, 0, thumb, 0, tdata.length);
					data.setThumbnailImage(thumb);
				}
			}

		}
		catch (Exception e) {
			// Exception reading metadata, just ignore it
			//System.err.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
		}
		return data;
	}


	/**
	 * Check whether the exifreader class can be correctly resolved
	 * @return true if it looks ok
	 */
	public boolean looksOK()
	{
		try {
			String test = ExifReader.class.getName();
			if (test != null) return true;
		}
		catch (LinkageError le) {}
		return false;
	}
}
