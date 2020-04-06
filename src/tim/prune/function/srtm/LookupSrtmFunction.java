package tim.prune.function.srtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.ProgressDialog;
import tim.prune.tips.TipManager;
import tim.prune.undo.UndoLookupSrtm;

/**
 * Class to provide a lookup function for point altitudes using the Space
 * Shuttle's SRTM data files. HGT files are downloaded into memory via HTTP and
 * point altitudes can then be interpolated from the 3m grid data.
 */
public class LookupSrtmFunction extends GenericFunction implements Runnable
{
	/** Progress dialog */
	private ProgressDialog _progress = null;
	/** Track to process */
	private Track _track = null;
	/** Flag for whether this is a real track or a terrain one */
	private boolean _normalTrack = true;
	/** Flag set when any tiles had to be downloaded (rather than just loaded locally) */
	private boolean _hadToDownload = false;
	/** Flag to check whether this function is currently running or not */
	private boolean _running = false;

	/** Expected size of hgt file in bytes */
	private static final long HGT_SIZE = 2884802L;
	/** Altitude below which is considered void */
	private static final int VOID_VAL = -32768;

	/**
	 * Constructor
	 * @param inApp  App object
	 */
	public LookupSrtmFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.lookupsrtm";
	}

	/**
	 * Begin the lookup using the normal track
	 */
	public void begin() {
		begin(_app.getTrackInfo().getTrack(), true);
	}

	/**
	 * Begin the lookup with an alternative track
	 * @param inAlternativeTrack
	 */
	public void begin(Track inAlternativeTrack) {
		begin(inAlternativeTrack, false);
	}

	/**
	 * Begin the function with the given parameters
	 * @param inTrack track to process
	 * @param inNormalTrack true if this is a "normal" track, false for an artificially constructed one such as for terrain
	 */
	private void begin(Track inTrack, boolean inNormalTrack)
	{
		_running = true;
		_hadToDownload = false;
		if (_progress == null) {
			_progress = new ProgressDialog(_parentFrame, getNameKey());
		}
		_progress.show();
		_track = inTrack;
		_normalTrack = inNormalTrack;
		// start new thread for time-consuming part
		new Thread(this).start();
	}

	/**
	 * Run method using separate thread
	 */
	public void run()
	{
		// Compile list of tiles to get
		ArrayList<SrtmTile> tileList = new ArrayList<SrtmTile>();
		boolean hasZeroAltitudePoints = false;
		boolean hasNonZeroAltitudePoints = false;
		// First, loop to see what kind of points we have
		for (int i = 0; i < _track.getNumPoints(); i++)
		{
			if (_track.getPoint(i).hasAltitude())
			{
				if (_track.getPoint(i).getAltitude().getValue() == 0) {
					hasZeroAltitudePoints = true;
				}
				else {
					hasNonZeroAltitudePoints = true;
				}
			}
		}
		// Should we overwrite the zero altitude values?
		boolean overwriteZeros = hasZeroAltitudePoints && !hasNonZeroAltitudePoints;
		// If non-zero values present as well, ask user whether to overwrite the zeros or not
		if (hasNonZeroAltitudePoints && hasZeroAltitudePoints && JOptionPane.showConfirmDialog(_parentFrame,
			I18nManager.getText("dialog.lookupsrtm.overwritezeros"), I18nManager.getText(getNameKey()),
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			overwriteZeros = true;
		}

		// Now loop again to extract the required tiles
		for (int i = 0; i < _track.getNumPoints(); i++)
		{
			// Consider points which don't have altitudes or have zero values
			if (!_track.getPoint(i).hasAltitude()
				|| (overwriteZeros && _track.getPoint(i).getAltitude().getValue() == 0))
			{
				SrtmTile tile = new SrtmTile(_track.getPoint(i));
				boolean alreadyGot = false;
				for (int t = 0; t < tileList.size(); t++)
				{
					if (tileList.get(t).equals(tile)) {
						alreadyGot = true;
					}
				}
				if (!alreadyGot) {tileList.add(tile);}
			}
		}
		lookupValues(tileList, overwriteZeros);
		// Finished
		_running = false;
		// Show tip if lots of online lookups were necessary
		if (_hadToDownload) {
			_app.showTip(TipManager.Tip_DownloadSrtm);
		}
	}


	/**
	 * Lookup the values from SRTM data
	 * @param inTileList list of tiles to get
	 * @param inOverwriteZeros true to overwrite zero altitude values
	 */
	private void lookupValues(ArrayList<SrtmTile> inTileList, boolean inOverwriteZeros)
	{
		UndoLookupSrtm undo = new UndoLookupSrtm(_app.getTrackInfo());
		int numAltitudesFound = 0;
		// Update progress bar
		if (_progress != null)
		{
			_progress.setMaximum(inTileList.size());
			_progress.setValue(0);
		}
		String errorMessage = null;
		// Get urls for each tile
		URL[] urls = TileFinder.getUrls(inTileList);
		for (int t=0; t<inTileList.size() && !_progress.isCancelled() && urls != null; t++)
		{
			if (urls[t] != null)
			{
				SrtmTile tile = inTileList.get(t);
				try
				{
					// Set progress
					_progress.setValue(t);
					final int ARRLENGTH = 1201 * 1201;
					int[] heights = new int[ARRLENGTH];
					// Open zipinputstream on url and check size
					ZipInputStream inStream = getStreamToHgtFile(urls[t]);
					boolean entryOk = false;
					if (inStream != null)
					{
						ZipEntry entry = inStream.getNextEntry();
						entryOk = (entry != null && entry.getSize() == HGT_SIZE);
						if (entryOk)
						{
							// Read entire file contents into one byte array
							for (int i = 0; i < ARRLENGTH; i++)
							{
								heights[i] = inStream.read() * 256 + inStream.read();
								if (heights[i] >= 32768) {heights[i] -= 65536;}
							}
						}
						// else {
						//	System.out.println("length not ok: " + entry.getSize());
						// }
						// Close stream from url
						inStream.close();
					}

					if (entryOk)
					{
						numAltitudesFound += applySrtmTileToWholeTrack(tile, heights, inOverwriteZeros);
					}
				}
				catch (IOException ioe) {
					errorMessage = ioe.getClass().getName() + " - " + ioe.getMessage();
				}
			}
		}

		_progress.dispose();
		if (_progress.isCancelled()) {
			return;
		}

		if (numAltitudesFound > 0)
		{
			// Inform app including undo information
			_track.requestRescale();
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_ADDED_OR_REMOVED);
			// Don't update app if we're doing another track
			if (_normalTrack)
			{
				_app.completeFunction(undo,
					I18nManager.getTextWithNumber("confirm.lookupsrtm", numAltitudesFound));
			}
		}
		else if (errorMessage != null) {
			_app.showErrorMessageNoLookup(getNameKey(), errorMessage);
		}
		else if (inTileList.size() > 0) {
			_app.showErrorMessage(getNameKey(), "error.lookupsrtm.nonefound");
		}
		else {
			_app.showErrorMessage(getNameKey(), "error.lookupsrtm.nonerequired");
		}
	}

	/**
	 * See whether the SRTM file is already available locally first, then try online
	 * @param inUrl URL for online resource
	 * @return ZipInputStream either on the local file or on the downloaded zip file
	 */
	private ZipInputStream getStreamToHgtFile(URL inUrl)
	throws IOException
	{
		String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (diskCachePath != null)
		{
			File srtmDir = new File(diskCachePath, "srtm");
			if (srtmDir.exists() && srtmDir.isDirectory() && srtmDir.canRead())
			{
				File srtmFile = new File(srtmDir, new File(inUrl.getFile()).getName());
				if (srtmFile.exists() && srtmFile.isFile() && srtmFile.canRead()
					&& srtmFile.length() > 400)
				{
					// System.out.println("Lookup: Using file " + srtmFile.getAbsolutePath());
					// File found, use this one
					return new ZipInputStream(new FileInputStream(srtmFile));
				}
			}
		}
		// System.out.println("Lookup: Trying online: " + inUrl.toString());
		_hadToDownload = true;
		// MAYBE: Only download if we're in online mode?
		return new ZipInputStream(inUrl.openStream());
	}

	/**
	 * Given the height data read in from file, apply the given tile to all points
	 * in the track with missing altitude
	 * @param inTile tile being applied
	 * @param inHeights height data read in from file
	 * @param inOverwriteZeros true to overwrite zero altitude values
	 * @return number of altitudes found
	 */
	private int applySrtmTileToWholeTrack(SrtmTile inTile, int[] inHeights, boolean inOverwriteZeros)
	{
		int numAltitudesFound = 0;
		// Loop over all points in track, try to apply altitude from array
		for (int p = 0; p < _track.getNumPoints(); p++)
		{
			DataPoint point = _track.getPoint(p);
			if (!point.hasAltitude()
				|| (inOverwriteZeros && point.getAltitude().getValue() == 0))
			{
				if (new SrtmTile(point).equals(inTile))
				{
					double x = (point.getLongitude().getDouble() - inTile.getLongitude()) * 1200;
					double y = 1201 - (point.getLatitude().getDouble() - inTile.getLatitude()) * 1200;
					int idx1 = ((int)y)*1201 + (int)x;
					try
					{
						int[] fouralts = {inHeights[idx1], inHeights[idx1+1], inHeights[idx1-1201], inHeights[idx1-1200]};
						int numVoids = (fouralts[0]==VOID_VAL?1:0) + (fouralts[1]==VOID_VAL?1:0)
							+ (fouralts[2]==VOID_VAL?1:0) + (fouralts[3]==VOID_VAL?1:0);
						// if (numVoids > 0) System.out.println(numVoids + " voids found");
						double altitude = 0.0;
						switch (numVoids)
						{
							case 0:	altitude = bilinearInterpolate(fouralts, x, y); break;
							case 1: altitude = bilinearInterpolate(fixVoid(fouralts), x, y); break;
							case 2:
							case 3: altitude = averageNonVoid(fouralts); break;
							default: altitude = VOID_VAL;
						}
						// Special case for terrain tracks, don't interpolate voids yet
						if (!_normalTrack && numVoids > 0) {
							altitude = VOID_VAL;
						}
						if (altitude != VOID_VAL)
						{
							point.setFieldValue(Field.ALTITUDE, ""+altitude, false);
							// depending on settings, this value may have been added as feet, we need to force metres
							point.getAltitude().reset(new Altitude((int)altitude, UnitSetLibrary.UNITS_METRES));
							numAltitudesFound++;
						}
					}
					catch (ArrayIndexOutOfBoundsException obe) {
						// System.err.println("lat=" + point.getLatitude().getDouble() + ", x=" + x + ", y=" + y + ", idx=" + idx1);
					}
				}
			}
		}
		return numAltitudesFound;
	}

	/**
	 * Perform a bilinear interpolation on the given altitude array
	 * @param inAltitudes array of four altitude values on corners of square (bl, br, tl, tr)
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @return interpolated altitude
	 */
	private static double bilinearInterpolate(int[] inAltitudes, double inX, double inY)
	{
		double alpha = inX - (int) inX;
		double beta  = 1 - (inY - (int) inY);
		double alt = (1-alpha)*(1-beta)*inAltitudes[0] + alpha*(1-beta)*inAltitudes[1]
			+ (1-alpha)*beta*inAltitudes[2] + alpha*beta*inAltitudes[3];
		return alt;
	}

	/**
	 * Fix a single void in the given array by replacing it with the average of the others
	 * @param inAltitudes array of altitudes containing one void
	 * @return fixed array without voids
	 */
	private static int[] fixVoid(int[] inAltitudes)
	{
		int[] fixed = new int[inAltitudes.length];
		for (int i = 0; i < inAltitudes.length; i++)
		{
			if (inAltitudes[i] == VOID_VAL) {
				fixed[i] = (int) Math.round(averageNonVoid(inAltitudes));
			}
			else {
				fixed[i] = inAltitudes[i];
			}
		}
		return fixed;
	}

	/**
	 * Calculate the average of the non-void altitudes in the given array
	 * @param inAltitudes array of altitudes with one or more voids
	 * @return average of non-void altitudes
	 */
	private static final double averageNonVoid(int[] inAltitudes)
	{
		double totalAltitude = 0.0;
		int numAlts = 0;
		for (int i = 0; i < inAltitudes.length; i++)
		{
			if (inAltitudes[i] != VOID_VAL)
			{
				totalAltitude += inAltitudes[i];
				numAlts++;
			}
		}
		if (numAlts < 1) {return VOID_VAL;}
		return totalAltitude / numAlts;
	}

	/**
	 * @return true if a thread is currently running
	 */
	public boolean isRunning()
	{
		return _running;
	}
}
