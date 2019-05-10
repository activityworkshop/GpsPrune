package tim.prune.load;

/**
 * Class to manage the list of file formats supported by Gpsbabel
 * (older versions of gpsbabel might not support all of these, of course).
 * Certain supported formats such as txt, csv are not included here
 * as GpsPrune can already load them directly.
 */
public abstract class BabelFileFormats
{
	/**
	 * @return an object array for the format descriptions
	 */
	public static String[] getDescriptions() {
		return getColumn(0);
	}

	/**
	 * Find the (first) appropriate file format for the given file suffix
	 * @param inSuffix end of filename including .
	 * @return matching index or -1 if not found
	 */
	public static int getIndexForFileSuffix(String inSuffix)
	{
		if (inSuffix != null && inSuffix.length() > 1)
		{
			final String[] suffixes = getColumn(2);
			for (int i=0; i<suffixes.length; i++)
			{
				if (suffixes[i] != null && suffixes[i].equalsIgnoreCase(inSuffix))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Get the format name for the selected row
	 * @param inIndex index of selected format
	 * @return name of this format to give to gpsbabel
	 */
	public static String getFormat(int inIndex)
	{
		String[] formats = getColumn(1);
		if (inIndex >= 0 && inIndex < formats.length)
			return formats[inIndex];
		return null;
	}

	/**
	 * Extract the specified column of the data array
	 * @param inIndex column index from 0 to 2
	 * @return string array containing required data
	 */
	private static String[] getColumn(int inIndex)
	{
		final String[] allFormats = {
			"Alan Map500 tracklogs", "alantrl", ".trl",
			"Alan Map500 waypoints and routes", "alanwpr", ".wpr",
			"Brauniger IQ Series Barograph Download", "baroiq", null,
			"Bushnell GPS Trail file", "bushnell_trl", null,
			"Bushnell GPS Waypoint file", "bushnell", null,
			"Cambridge/Winpilot glider software", "cambridge", null,
			"CarteSurTable data file", "cst", null,
			"Columbus/Visiontac V900 files", "v900", ".csv",
			"CompeGPS data files", "compegps", ".wpt/.trk/.rte",
			"Data Logger iBlue747 csv", "iblue747", null,
			"Data Logger iBlue757 csv", "iblue757", null,
			"DeLorme .an1 (drawing) file", "an1", null,
			"DeLorme GPL", "gpl", null,
			"DeLorme PN-20/PN-30/PN-40 USB protocol", "delbin", null,
			"DeLorme Street Atlas Plus", "saplus", null,
			"DeLorme Street Atlas Route", "saroute", null,
			"DeLorme XMap HH Native .WPT", "xmap", null,
			"DeLorme XMap/SAHH 2006 Native .TXT", "xmap2006", null,
			"DeLorme XMat HH Street Atlas USA .WPT (PPC)", "xmapwpt", null,
			"Destinator Itineraries", "destinator_itn", ".dat",
			"Destinator Points of Interest", "destinator_poi", ".dat",
			"Destinator TrackLogs", "destinator_trl", ".dat",
			"EasyGPS binary format", "easygps", null,
			"Energym training watch", "energympro", null,
			"Enigma binary waypoint file", "enigma", ".ert",
			"F90G log file", "f90g", null,
			"FAI/IGC Flight Recorder Data Format", "igc", null,
			"FlySight", "flysight", null,
			"Franson GPSGate Simulation", "gpssim", null,
			"Fugawi", "fugawi", null,
			"G7ToWin data files", "g7towin", ".g7t",
			"Garmin 301 Custom position and heartrate", "garmin301", null,
			"Garmin FIT activity file", "garmin_fit", ".fit",
			"Garmin Logbook XML", "glogbook", null,
			"Garmin MapSource - gdb", "gdb", null,
			"Garmin MapSource - mps", "mapsource", null,
			"Garmin Mobile XT Track files", "garmin_xt", null,
			"Garmin PCX5", "pcx", null,
			"Garmin POI database", "garmin_poi", null,
			"Garmin Points of Interest", "garmin_gpi", ".gpi",
			"Garmin Training Center", "gtrnctr", ".tcx",
			"Geocaching.com .loc", "geo", null,
			"GeocachingDB for Palm/OS", "gcdb", null,
			"Geogrid-Viewer ascii overlay file", "ggv_ovl", ".ovl",
			"Geogrid-Viewer tracklogs", "ggv_log", ".log",
			"GEOnet Names Server (GNS)", "geonet", null,
			"GlobalSat DG-100/BT-335", "dg-100", null,
			"GlobalSat DG-200", "dg-200", null,
			"Google Directions", "googledir", null,
			"Google Maps XML", "google", null,
			"Google Navigator Tracklines", "gnav_trl", ".trl",
			"GoPal GPS track log", "gopal", ".trk",
			"GPS TrackMaker", "gtm", null,
			"GPSman", "gpsman", null,
			"gpsutil", "gpsutil", null,
			"GPX", "gpx", ".gpx",
			"HikeTech", "hiketech", null,
			"Holux (gm-100) .wpo Format", "holux", null,
			"Holux M-241 (MTK based) Binary File Format", "m241-bin", null,
			"Holux M-241 (MTK based) download", "m241", null,
			"Honda/Acura Navigation System VP Log File Format", "vpl", null,
			"Humminbird tracks", "humminbird_ht", ".ht",
			"Humminbird waypoints and routes", "humminbird", ".hwr",
			"IGN Rando track files", "ignrando", null,
			"iGO2008 points of interest", "igo2008_poi", ".upoi",
			"IGO8 .trk", "igo8", null,
			"Jelbert GeoTagger data file", "jtr", null,
			"Jogmap.de XML format", "jogmap", null,
			"Kompass (DAV) Track", "kompass_tk", ".tk",
			"Kompass (DAV) Waypoints", "kompass_wp", ".wp",
			"KuDaTa PsiTrex text", "psitrex", null,
			"Lowrance USR", "lowranceusr", null,
			"Lowrance USR version 4", "lowranceusr4", null,
			"Magellan Explorist Geocaching", "maggeo", null,
			"Magellan Mapsend", "mapsend", null,
			"Magellan SD files (as for eXplorist)", "magellanx", null,
			"Magellan SD files (as for Meridian)", "magellan", null,
			"Magellan serial protocol", "magellan", null,
			"MagicMaps IK3D project file", "ik3d", ".ikt",
			"Map&Guide 'TourExchangeFormat' XML", "tef", null,
			"MapAsia track file", "mapasia_tr7", ".tr7",
			"Mapopolis.com Mapconverter CSV", "mapconverter", null,
			"MapTech Exchange Format", "mxf", null,
			"MediaTek Locus", "mtk_locus", null,
			"Memory-Map Navigator overlay files", "mmo", ".mmo",
			"Microsoft Streets and Trips 2002-2007", "s_and_t", null,
			"Motorrad Routenplaner (Map&Guide) .bcr files", "bcr", null,
			"MTK Logger (iBlue 747,...) Binary File Format", "mtk-bin", null,
			"MTK Logger (iBlue 747,Qstarz BT-1000,...) download", "mtk", null,
			"MyNav TRC format", "mynav", null,
			"National Geographic Topo .tpg (waypoints)", "tpg", null,
			"National Geographic Topo 2.x .tpo", "tpo2", null,
			"National Geographic Topo 3.x/4.x .tpo", "tpo3", null,
			"Navicache.com XML", "navicache", null,
			"Navigon Mobile Navigator .rte files", "nmn4", null,
			"Navigon Waypoints", "navigonwpt", null,
			"NaviGPS GT-11/BGT-11 Download", "navilink", null,
			"NaviGPS GT-31/BGT-31 datalogger", "sbp", ".sbp",
			"NaviGPS GT-31/BGT-31 SiRF binary logfile", "sbn", ".sbn",
			"Naviguide binary route file", "naviguide", ".twl",
			"Navitel binary track", "navitel_trk", ".bin",
			"Navitrak DNA marker format", "dna", null,
			"NetStumbler Summary File", "netstumbler", "text",
			"NIMA/GNIS Geographic Names File", "nima", null,
			"Nokia Landmark Exchange", "lmx", null,
			"OpenStreetMap data files", "osm", ".osm",
			"OziExplorer", "ozi", null,
			"PocketFMS breadcrumbs", "pocketfms_bc", null,
			"PocketFMS flightplan", "pocketfms_fp", ".xml",
			"PocketFMS waypoints", "pocketfms_wp", ".txt",
			"Quovadis", "quovadis", null,
			"Raymarine Waypoint File", "raymarine", ".rwf",
			"Ricoh GPS Log File", "ricoh", null,
			"See You flight analysis data", "cup", null,
			"Skymap / KMD150 ascii files", "skyforce", null,
			"SkyTraq Venus based loggers (download)", "skytraq", null,
			"SkyTraq Venus based loggers Binary File Format", "skytraq-bin", null,
			"Sportsim track files (part of zipped .ssz files)", "sportsim", null,
			"SubRip subtitles for video mapping", "subrip", ".srt",
			"Suunto Trek Manager (STM) .sdf files", "stmsdf", null,
			"Suunto Trek Manager (STM) WaypointPlus files", "stmwpp", null,
			"Swiss Map 25/50/100", "xol", ".xol",
			"TomTom Itineraries", "tomtom_itn", ".itn",
			"TomTom Places Itineraries", "tomtom_itn_places", ".itn",
			"TomTom POI file", "tomtom_asc", ".asc",
			"TomTom POI file", "tomtom", ".ov2",
			"TopoMapPro Places File", "tmpro", null,
			"TrackLogs digital mapping", "dmtlog", ".trl",
			"U.S. Census Bureau Tiger Mapping Service", "tiger", null,
			"Vcard Output (for iPod)", "vcard", null,
			"VidaOne GPS for Pocket PC", "vidaone", ".gpb",
			"Vito Navigator II tracks", "vitosmt", null,
			"Vito SmartMap tracks", "vitovtt", ".vtt",
			"WiFiFoFum 2.0 for PocketPC XML", "wfff", null,
			"Wintec TES file", "wintec_tes", null,
			"Wintec WBT-100/200 Binary File Format", "wbt-bin", null,
			"Wintec WBT-100/200 GPS Download", "wbt", null,
			"Wintec WBT-201/G-Rays 2 Binary File Format", "wbt-tk1", null,
			"XAiOX iTrackU Logger", "itracku", null,
			"XAiOX iTrackU Logger Binary File Format", "itracku-bin", null,
			"Yahoo Geocode API data", "yahoo", null,
		};
		// Copy elements into new array
		final int numRows = allFormats.length / 3;
		String[] result = new String[numRows];
		for (int i=0; i<numRows; i++) {
			result[i] = allFormats[i*3 + inIndex];
		}
		return result;
	}
}
