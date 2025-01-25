package tim.prune.save.xml;

import java.util.ArrayList;

import tim.prune.data.ExtensionInfo;
import tim.prune.data.FileType;
import tim.prune.data.SourceInfo;

/**
 * Class to combine the xml header information including namespaces
 * from multiple xml sources
 */
public class HeaderCombiner
{
	private final GpxVersion _targetVersion;
	private final GpxVersion _ignoreVersion;
	private boolean _foundLocations = false;
	private final ArrayList<String> _namespaces = new ArrayList<>();
	private final ArrayList<String> _locationPairs = new ArrayList<>();

	/** Constructor giving target version of Gpx1.0 or Gpx1.1 */
	HeaderCombiner(GpxVersion inTargetVersion)
	{
		_targetVersion = inTargetVersion;
		_ignoreVersion = (inTargetVersion == GpxVersion.GPX_1_0 ? GpxVersion.GPX_1_1 : GpxVersion.GPX_1_0);
	}

	/** Add a single source info to the collection */
	void addSourceInfo(SourceInfo inInfo)
	{
		if (inInfo == null || inInfo.getFileType() != FileType.GPX) {
			return;
		}
		ExtensionInfo extnInfo = inInfo.getExtensionInfo();
		if (extnInfo == null) {
			return;
		}
		// Deal with namespaces
		for (String s : extnInfo.getExtraNamespaces()) {
			addNamespace(s);
		}
		// Deal with locations of schemas
		final String[] locations = extnInfo.getSchemaLocations().split(" ");
		final int numPairs = locations.length / 2;
		for (int i=0; i<numPairs; i++) {
			addLocationPair(locations[i*2], locations[i*2 + 1]);
		}
	}

	/** Add a namespace to the collection */
	private void addNamespace(String inNamespace)
	{
		if (inNamespace == null || inNamespace.isEmpty()) {
			return;
		}
		for (String ns : _namespaces)
		{
			if (ns.equalsIgnoreCase(inNamespace)) {
				return; // already got it
			}
		}
		_namespaces.add(inNamespace);
	}

	/** Add a location pair (id and url) to the collection */
	private void addLocationPair(String inId, String inUrl)
	{
		if (inId == null || inUrl == null) {
			return;
		}
		if (_ignoreVersion.matches(inId) && _ignoreVersion.matches(inUrl)) {
			// Found header for another Gpx version, ignore it
			return;
		}
		if (_targetVersion.matches(inId) && _targetVersion.matches(inUrl))
		{
			// Found header for target Gpx version, use it
			if (_foundLocations) {
				return;
			}
			_foundLocations = true;
		}
		String pairString = inId + " " + inUrl;
		for (String pair : _locationPairs)
		{
			if (pair.equalsIgnoreCase(pairString)) {
				return;
			}
		}
		_locationPairs.add(pairString);
	}

	/** @return a space-separated list of all location pairs */
	String getAllLocations(String inDefaultPair)
	{
		StringBuilder builder = new StringBuilder();
		boolean needSpace = !_foundLocations;
		if (!_foundLocations) {
			builder.append(inDefaultPair);
		}
		for (String pair : _locationPairs)
		{
			if (needSpace) {
				builder.append(' ');
			}
			builder.append(pair);
			needSpace = true;
		}
		return builder.toString();
	}

	/** @return a linefeed-separated list of all namespaces */
	String getNamespaces()
	{
		StringBuilder builder = new StringBuilder();
		for (String ns : _namespaces) {
			builder.append(' ').append(ns).append('\n');
		}
		return builder.toString();
	}
}
