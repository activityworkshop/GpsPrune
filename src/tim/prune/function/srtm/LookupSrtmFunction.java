package tim.prune.function.srtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
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
 * Shuttle's SRTM data files. HGT files are downloaded via HTTP and
 * point altitudes can then be interpolated from the grid data.
 */
public class LookupSrtmFunction extends GenericFunction implements Runnable
{
	/** Progress dialog */
	private ProgressDialog _progress = null;
	/** Track to process */
	private Track _track = null;
	/** Flag for whether this is a real track or a terrain one */
	private boolean _normalTrack = true;
	/** Flag set when any tiles had to be downloaded (but not cached) */
	private boolean _hadToDownload = false;
	/** Count the number of tiles downloaded and cached */
	private int _numCached = 0;
	/** Flag to check whether this function is currently running or not */
	private boolean _running = false;


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
		HashSet<SrtmTile> tileSet = new HashSet<SrtmTile>();
		for (int i = 0; i < _track.getNumPoints(); i++)
		{
			// Consider points which don't have altitudes or have zero values
			if (!_track.getPoint(i).hasAltitude()
				|| (overwriteZeros && _track.getPoint(i).getAltitude().getValue() == 0))
			{
				tileSet.add(new SrtmTile(_track.getPoint(i)));
			}
		}

		CookieHandler regularCookieHandler = CookieHandler.getDefault();
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		lookupValues(tileSet, overwriteZeros);
		CookieHandler.setDefault(regularCookieHandler);

		// Finished
		_running = false;
		// Show tip if lots of online lookups were necessary
		if (_hadToDownload) {
			_app.showTip(TipManager.Tip_DownloadSrtm);
		}
		else if (_numCached > 0) {
			showConfirmMessage(_numCached);
		}
	}


	/**
	 * Lookup the values from SRTM data
	 * @param inTileSet set of tiles to get
	 * @param inOverwriteZeros true to overwrite zero altitude values
	 */
	private void lookupValues(HashSet<SrtmTile> inTileSet, boolean inOverwriteZeros)
	{
		UndoLookupSrtm undo = new UndoLookupSrtm(_app.getTrackInfo());
		int numAltitudesFound = 0;
		SrtmSource[] tileSources = new SrtmSource[] {
			new SrtmHighResSource(),
			new SrtmLowResSource()};
		String errorMessage = null;
		final int numTiles = inTileSet.size();

		// Update progress bar
		if (_progress != null)
		{
			_progress.setMaximum(numTiles);
			_progress.setValue(0);
		}
		int currentTileIndex = 0;
		_numCached = 0;
		for (SrtmTile tile : inTileSet)
		{
			// Set progress
			_progress.setValue(currentTileIndex++);
			int[] heights = null;
			for (SrtmSource tileSource : tileSources)
			{
				if (heights == null)
				{
					try
					{
						heights = getHeightsForTile(tile, tileSource);
						numAltitudesFound += applySrtmTileToWholeTrack(tile, heights, inOverwriteZeros,
							tileSource.getTilePixels());
					}
					catch (IOException ioe) {
						errorMessage = ioe.getClass().getName() + " - " + ioe.getMessage();
					} catch (SrtmAuthException authExc) {
						errorMessage = I18nManager.getText("error.srtm.authenticationfailed") + " - " + authExc.getMessage();
					}
				}
			}
		}

		_progress.dispose();
		if (_progress.isCancelled()) {
			return;
		}

		if (errorMessage != null) {
			_app.showErrorMessageNoLookup(getNameKey(), errorMessage);
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
		else if (numTiles > 0) {
			_app.showErrorMessage(getNameKey(), "error.lookupsrtm.nonefound");
		}
		else {
			_app.showErrorMessage(getNameKey(), "error.lookupsrtm.nonerequired");
		}
	}

	/**
	 * Get the height array for the given tile, using the given source
	 * @param inTile tile to get data for
	 * @param inTileSource tile source to use
	 * @return int array containing heights
	 * @throws IOException on IO failure
	 * @throws SrtmAuthException on authentication failure
	 */
	private int[] getHeightsForTile(SrtmTile inTile, SrtmSource inTileSource)
		throws IOException, SrtmAuthException
	{
		int[] heights = null;
		// Open zipinputstream on url and check size
		ZipInputStream inStream = getStreamToSrtmData(inTile, inTileSource);
		boolean entryOk = false;
		if (inStream != null)
		{
			ZipEntry entry = inStream.getNextEntry();
			entryOk = (entry != null && entry.getSize() == inTileSource.getTileSizeBytes());
			if (entryOk)
			{
				final int ARRLENGTH = inTileSource.getTilePixels() * inTileSource.getTilePixels();
				heights = new int[ARRLENGTH];

				// Read entire file contents into one byte array
				for (int i = 0; i < ARRLENGTH; i++)
				{
					heights[i] = inStream.read() * 256 + inStream.read();
					if (heights[i] >= 32768) {heights[i] -= 65536;}
				}
			}
			// Close stream from url
			inStream.close();
		}

		if (!entryOk) {
			heights = null;
		}
		return heights;
	}

	/**
	 * See whether the SRTM file is already available locally first, then try online
	 * @param inTile tile to get
	 * @param inSrtmSource source of data to use
	 * @return ZipInputStream either on the local file or on the downloaded zip file
	 * @throws SrtmAuthException if authentication failed
	 */
	private ZipInputStream getStreamToSrtmData(SrtmTile inTile, SrtmSource inSrtmSource)
	throws IOException, SrtmAuthException
	{
		ZipInputStream localData = null;
		try {
			localData = getStreamToLocalHgtFile(inSrtmSource.getCacheDir(),
				inSrtmSource.getFilename(inTile));
		}
		catch (IOException ioe) {
			localData = null;
		}
		if (localData != null)
		{
			return localData;
		}
		// try to download to cache
		SrtmSource.Result result = inSrtmSource.downloadTile(inTile);
		if (result == SrtmSource.Result.DOWNLOADED)
		{
			_numCached++;
			return getStreamToLocalHgtFile(inSrtmSource.getCacheDir(), inSrtmSource.getFilename(inTile));
		}
		if (result == SrtmSource.Result.NOT_ENABLED) {
			return null;
		}
		// If we don't have a cache, we may be able to download it temporarily
		if (result != SrtmSource.Result.DOWNLOAD_FAILED)
		{
			_hadToDownload = true;
			URL tileUrl = inSrtmSource.getUrl(inTile);
			if (tileUrl == null) {
				return null;
			}
			URLConnection conn = tileUrl.openConnection();
			conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
			return new ZipInputStream(conn.getInputStream());
		}
		// everything failed
		return null;
	}

	/**
	 * Get the SRTM file from the local cache, if available
	 * @param inFilename filename to look for
	 * @return ZipInputStream on the local file or null if not there
	 */
	private ZipInputStream getStreamToLocalHgtFile(File inCacheDir, String inFilename)
	throws IOException
	{
		if (inCacheDir != null && inCacheDir.exists()
			&& inCacheDir.isDirectory() && inCacheDir.canRead())
		{
			File srtmFile = new File(inCacheDir, inFilename);
			if (srtmFile.exists() && srtmFile.isFile() && srtmFile.canRead()
				&& srtmFile.length() > 400)
			{
				// File found, use this one
				return new ZipInputStream(new FileInputStream(srtmFile));
			}
		}
		return null;
	}

	/**
	 * Given the height data read in from file, apply the given tile to all points
	 * in the track with missing altitude
	 * @param inTile tile being applied
	 * @param inHeights height data read in from file
	 * @param inOverwriteZeros true to overwrite zero altitude values
	 * @param inTilePixelsPerSide number of pixels on side of tile
	 * @return number of altitudes found
	 */
	private int applySrtmTileToWholeTrack(SrtmTile inTile, int[] inHeights,
		boolean inOverwriteZeros, int inTilePixelsPerSide)
	{
		if (inHeights == null) {return 0;}
		int numAltitudesFound = 0;
		// Loop over all points in track, try to apply altitude from array
		for (int p = 0; p < _track.getNumPoints(); p++)
		{
			DataPoint point = _track.getPoint(p);
			final boolean doCalculation = !point.hasAltitude()
				|| (inOverwriteZeros && point.getAltitude().getValue() == 0);
			if (doCalculation && inTile.contains(point))
			{
				final double altitude = Interpolator.calculateAltitude(point.getLongitude().getDouble(),
					point.getLatitude().getDouble(), inHeights, _normalTrack, inTilePixelsPerSide);
				if (altitude != SrtmSource.VOID_VAL)
				{
					point.setFieldValue(Field.ALTITUDE, ""+altitude, false);
					// depending on settings, this value may have been added as feet, we need to force metres
					point.getAltitude().reset(new Altitude((int)altitude, UnitSetLibrary.UNITS_METRES));
					numAltitudesFound++;
				}
			}
		}
		return numAltitudesFound;
	}

	/**
	 * @return true if a thread is currently running
	 */
	public boolean isRunning()
	{
		return _running;
	}

	private void showConfirmMessage(int numDownloaded)
	{
		if (numDownloaded == 1)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getTextWithNumber("confirm.downloadsrtm.1", numDownloaded),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
		else if (numDownloaded > 1)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getTextWithNumber("confirm.downloadsrtm", numDownloaded),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
	}
}

