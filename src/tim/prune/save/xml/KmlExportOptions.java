package tim.prune.save.xml;

import java.awt.Color;
import java.util.TimeZone;

/** Hold the options for the Kml/Kmz export */
public class KmlExportOptions
{
	private boolean _exportTrackPoints;
	private boolean _exportWaypoints;
	private boolean _exportPhotos;
	private boolean _exportAudios;
	private boolean _exportJustSelection;

	private boolean _absoluteAltitudes;

	private String _title;
	private Color _trackColour;
	private TimeZone _timezone;

	KmlExportOptions setExportTrackPoints(boolean inExport) {
		_exportTrackPoints = inExport;
		return this;
	}

	KmlExportOptions setExportWaypoints(boolean inExport) {
		_exportWaypoints = inExport;
		return this;
	}

	KmlExportOptions setExportPhotos(boolean inExport) {
		_exportPhotos = inExport;
		return this;
	}

	KmlExportOptions setExportAudios(boolean inExport) {
		_exportAudios = inExport;
		return this;
	}

	KmlExportOptions setExportJustSelection(boolean inExport) {
		_exportJustSelection = inExport;
		return this;
	}

	KmlExportOptions setAbsoluteAltitudes(boolean inAbsolute) {
		_absoluteAltitudes = inAbsolute;
		return this;
	}

	KmlExportOptions setTitle(String inTitle) {
		_title = inTitle;
		return this;
	}

	KmlExportOptions setTrackColour(Color inColour) {
		_trackColour = inColour;
		return this;
	}

	KmlExportOptions setTimezone(TimeZone inZone) {
		_timezone = inZone;
		return this;
	}

	boolean getExportTrackPoints() {
		return _exportTrackPoints;
	}

	boolean getExportWaypoints() {
		return _exportWaypoints;
	}

	boolean getExportPhotos() {
		return _exportPhotos;
	}

	boolean getExportAudios() {
		return _exportAudios;
	}

	boolean getExportJustSelection() {
		return _exportJustSelection;
	}

	boolean getAbsoluteAltitudes() {
		return _absoluteAltitudes;
	}

	String getTitle() {
		return _title;
	}

	Color getTrackColour() {
		return _trackColour;
	}

	TimeZone getTimezone() {
		return _timezone;
	}
}
