package tim.prune.function.srtm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.undo.UndoLookupSrtm;

/**
 * Class to provide a lookup function for point altitudes
 * using the Space Shuttle's SRTM data files.
 * HGT files are downloaded into memory via HTTP and point altitudes
 * can then be interpolated from the 3m grid data.
 */
public class LookupSrtmFunction extends GenericFunction implements Runnable
{
	/** function dialog */
	private JDialog _dialog = null;
	/** Progress bar for function */
	private JProgressBar _progressBar = null;
	/** Cancel flag */
	private boolean _cancelled = false;

	/** Expected size of hgt file in bytes */
	private static final long HGT_SIZE = 2884802L;
	/** Altitude below which is considered void */
	private static final int VOID_VAL = -32768;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public LookupSrtmFunction(App inApp)
	{
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.lookupsrtm";
	}

	/**
	 * Begin the lookup
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), false);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(20);
		_cancelled = false;
		// start new thread for time-consuming part
		new Thread(this).start();
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new JLabel(I18nManager.getText("confirm.running")), BorderLayout.NORTH);
		_progressBar = new JProgressBar();
		_progressBar.setPreferredSize(new Dimension(250, 30));
		dialogPanel.add(_progressBar, BorderLayout.CENTER);
		// Cancel button at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_cancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Run method using separate thread
	 */
	public void run()
	{
		// Compile list of tiles to get
		Track track = _app.getTrackInfo().getTrack();
		ArrayList<SrtmTile> tileList = new ArrayList<SrtmTile>();
		boolean hasZeroAltitudePoints = false;
		boolean hasNonZeroAltitudePoints = false;
		// First, loop to see what kind of points we have
		for (int i=0; i<track.getNumPoints(); i++)
		{
			if (track.getPoint(i).hasAltitude())
			{
				if (track.getPoint(i).getAltitude().getValue() == 0) {
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

		_dialog.setVisible(true);
		// Now loop again to extract the required tiles
		for (int i=0; i<track.getNumPoints(); i++)
		{
			// Consider points which don't have altitudes or have zero values
			if (!track.getPoint(i).hasAltitude() || (overwriteZeros && track.getPoint(i).getAltitude().getValue() == 0))
			{
				SrtmTile tile = new SrtmTile(track.getPoint(i));
				boolean alreadyGot = false;
				for (int t=0; t<tileList.size(); t++) {
					if (tileList.get(t).equals(tile)) {
						alreadyGot = true;
					}
				}
				if (!alreadyGot) {tileList.add(tile);}
			}
		}
		lookupValues(tileList, overwriteZeros);
	}

	/**
	 * Lookup the values from SRTM data
	 * @param inTileList list of tiles to get
	 * @param inOverwriteZeros true to overwrite zero altitude values
	 */
	private void lookupValues(ArrayList<SrtmTile> inTileList, boolean inOverwriteZeros)
	{
		Track track = _app.getTrackInfo().getTrack();
		UndoLookupSrtm undo = new UndoLookupSrtm(_app.getTrackInfo());
		int numAltitudesFound = 0;
		// Update progress bar
		_progressBar.setMaximum(inTileList.size());
		_progressBar.setIndeterminate(inTileList.size() <= 1);
		_progressBar.setValue(0);
		String errorMessage = null;
		// Get urls for each tile
		URL[] urls = TileFinder.getUrls(inTileList);
		for (int t=0; t<inTileList.size() && !_cancelled; t++)
		{
			if (urls[t] != null)
			{
				SrtmTile tile = inTileList.get(t);
				try
				{
					_progressBar.setValue(t);
					final int ARRLENGTH = 1201*1201;
					int[] heights = new int[ARRLENGTH];
					// Open zipinputstream on url and check size
					ZipInputStream inStream = new ZipInputStream(urls[t].openStream());
					ZipEntry entry = inStream.getNextEntry();
					boolean entryOk = (entry.getSize() == HGT_SIZE);
					if (entryOk)
					{
						// Read entire file contents into one byte array
						for (int i=0; i<ARRLENGTH; i++) {
							heights[i] = inStream.read()*256 + inStream.read();
							if (heights[i] >= 32768) {heights[i] -= 65536;}
						}
					}
					//else {
					//	System.out.println("length not ok: " + entry.getSize());
					//}
					// Close stream from url
					inStream.close();

					if (entryOk)
					{
						// Loop over all points in track, try to apply altitude from array
						for (int p=0; p<track.getNumPoints(); p++)
						{
							DataPoint point = track.getPoint(p);
							if (!point.hasAltitude() || (inOverwriteZeros && point.getAltitude().getValue() == 0)) {
								if (new SrtmTile(point).equals(tile))
								{
									double x = (point.getLongitude().getDouble() - tile.getLongitude()) * 1200;
									double y = 1201 - (point.getLatitude().getDouble() - tile.getLatitude()) * 1200;
									int idx1 = ((int)y)*1201 + (int)x;
									try {
										int[] fouralts = {heights[idx1], heights[idx1+1], heights[idx1-1201], heights[idx1-1200]};
										int numVoids = (fouralts[0]==VOID_VAL?1:0) + (fouralts[1]==VOID_VAL?1:0)
											+ (fouralts[2]==VOID_VAL?1:0) + (fouralts[3]==VOID_VAL?1:0);
										// if (numVoids > 0) System.out.println(numVoids + " voids found");
										double altitude = 0.0;
										switch (numVoids) {
											case 0:	altitude = bilinearInterpolate(fouralts, x, y); break;
											case 1: altitude = bilinearInterpolate(fixVoid(fouralts), x, y); break;
											case 2:
											case 3: altitude = averageNonVoid(fouralts); break;
											default: altitude = VOID_VAL;
										}
										if (altitude != VOID_VAL) {
											point.setFieldValue(Field.ALTITUDE, ""+altitude, false);
											numAltitudesFound++;
										}
									}
									catch (ArrayIndexOutOfBoundsException obe) {
										//System.err.println("lat=" + point.getLatitude().getDouble() + ", x=" + x + ", y=" + y + ", idx=" + idx1);
									}
								}
							}
						}
					}
				}
				catch (IOException ioe) {
					errorMessage = ioe.getClass().getName() + " - " + ioe.getMessage();
				}
			}
		}
		_dialog.dispose();
		if (_cancelled) {return;}
		if (numAltitudesFound > 0)
		{
			// Inform app including undo information
			track.requestRescale();
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_ADDED_OR_REMOVED);
			_app.completeFunction(undo, I18nManager.getText("confirm.lookupsrtm1") + " " + numAltitudesFound
				+ " " + I18nManager.getText("confirm.lookupsrtm2"));
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
		for (int i=0; i<inAltitudes.length; i++) {
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
		for (int i=0; i<inAltitudes.length; i++) {
			if (inAltitudes[i] != VOID_VAL) {
				totalAltitude += inAltitudes[i];
				numAlts++;
			}
		}
		if (numAlts < 1) {return VOID_VAL;}
		return totalAltitude / numAlts;
	}
}
