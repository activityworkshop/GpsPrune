package tim.prune.save;

/**
 * Settings for controlling what gets exported,
 * for example by the Gpx export functions
 */
public class SettingsForExport
{
	private boolean _exportTrackPoints = true;
	private boolean _exportWaypoints   = true;
	private boolean _exportJustSelection = false;
	private boolean _exportTimestamps  = true;
	private boolean _exportMissingAltitudes = false;
	private boolean _exportPhotoPoints = true;
	private boolean _exportAudioPoints = true;

	/** Set to export track points or not */
	public void setExportTrackPoints(boolean inExport) {
		_exportTrackPoints = inExport;
	}

	/** Set to export waypoints or not */
	public void setExportWaypoints(boolean inExport) {
		_exportWaypoints = inExport;
	}

	/** Set to export just the selection or everything */
	public void setExportJustSelection(boolean inExport) {
		_exportJustSelection = inExport;
	}

	/** Set to export timestamps or not */
	public void setExportTimestamps(boolean inExport) {
		_exportTimestamps = inExport;
	}

	/** Set to export missing altitudes as zero or not */
	public void setExportMissingAltitudesAsZero(boolean inExport) {
		_exportMissingAltitudes = inExport;
	}

	/** Set to export photo points or not */
	public void setExportPhotoPoints(boolean inExport) {
		_exportPhotoPoints = inExport;
	}

	/** Set to export audio points or not */
	public void setExportAudiopoints(boolean inExport) {
		_exportAudioPoints = inExport;
	}

	/** @return true to export track points */
	public boolean getExportTrackPoints() {return _exportTrackPoints;}
	/** @return true to export waypoints */
	public boolean getExportWaypoints() {return _exportWaypoints;}
	/** @return true to export just the selection */
	public boolean getExportJustSelection() {return _exportJustSelection;}
	/** @return true to export timestamps */
	public boolean getExportTimestamps() {return _exportTimestamps;}
	/** @return true to export zeroes for missing altitudes */
	public boolean getExportMissingAltitudesAsZero() {return _exportMissingAltitudes;}
	/** @return true to export photo points */
	public boolean getExportPhotoPoints() {return _exportPhotoPoints;}
	/** @return true to export audio points */
	public boolean getExportAudioPoints() {return _exportAudioPoints;}
}
