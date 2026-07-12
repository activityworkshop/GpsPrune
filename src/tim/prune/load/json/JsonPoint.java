package tim.prune.load.json;

/**
 * Structure for holding a single point extracted from the Json
 */
record JsonPoint(String latitude, String longitude, String altitude, boolean newSegment) {
}
