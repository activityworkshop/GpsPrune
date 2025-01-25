package tim.prune.save.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.ExtensionInfo;
import tim.prune.data.FileType;
import tim.prune.data.SourceInfo;

public class HeaderCombinerTest
{
	@Test
	public void testEmpty()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(null);
		String locations = combiner.getAllLocations("a b");
		Assertions.assertEquals("a b", locations);
		String namespaces = combiner.getNamespaces();
		Assertions.assertEquals("", namespaces);
	}

	@Test
	public void testSingleNamespace()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(makeSourceInfoWithNamespace("myextn", "https://example.com/myextn"));
		String namespaces = combiner.getNamespaces();
		// Space is deliberately added before, line feed after
		Assertions.assertEquals(" xmlns:myextn=\"https://example.com/myextn\"\n", namespaces);
	}

	@Test
	public void testTwoNamespaces()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(makeSourceInfoWithNamespace("myextn", "https://example.com/myextn"));
		combiner.addSourceInfo(makeSourceInfoWithNamespace("otherext", "https://example.com/otherext"));
		String namespaces = combiner.getNamespaces();
		// Space is deliberately added before, line feed after
		String expected = " xmlns:myextn=\"https://example.com/myextn\"\n"
			+ " xmlns:otherext=\"https://example.com/otherext\"\n";
		Assertions.assertEquals(expected, namespaces);
	}

	@Test
	public void testDuplicateNamespaces()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(makeSourceInfoWithNamespace("myextn", "https://example.com/myextn"));
		combiner.addSourceInfo(makeSourceInfoWithNamespace("myExtn", "https://Example.com/MyExtn"));
		String namespaces = combiner.getNamespaces();
		// Just keeps the first one because they're equivalent ignoring case
		Assertions.assertEquals(" xmlns:myextn=\"https://example.com/myextn\"\n", namespaces);
	}

	private static SourceInfo makeSourceInfoWithNamespace(String inId, String inUrl)
	{
		SourceInfo info = new SourceInfo("file name", FileType.GPX);
		ExtensionInfo extensionInfo = new ExtensionInfo();
		extensionInfo.addNamespace(inId, inUrl);
		info.setExtensionInfo(extensionInfo);
		return info;
	}

	@Test
	public void testDefaultNamespace()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		String locations = combiner.getAllLocations("default1 default2");
		Assertions.assertEquals("default1 default2", locations);
	}

	@Test
	public void testUseGivenNamespace()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(new SourceInfoBuilder()
				.setGpx("https://example.com/GPX/1/0", "https://example.com/GPX/1/0/gpx.xsd")
				.build());
		String locations = combiner.getAllLocations("default1 default2");
		// Header information is used because it matches GPX 1.0
		Assertions.assertEquals("https://example.com/GPX/1/0 https://example.com/GPX/1/0/gpx.xsd", locations);
	}

	@Test
	public void testIgnoreOtherNamespace()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		combiner.addSourceInfo(new SourceInfoBuilder()
				.setGpx("https://example.com/GPX/1/1", "https://example.com/GPX/1/1/gpx.xsd")
				.build());
		String locations = combiner.getAllLocations("default1 default2");
		// The given fields are for GPX 1.1, but we want 1.0, so they are ignored
		Assertions.assertEquals("default1 default2", locations);
	}

	@Test
	public void testCombineNamespaces()
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_1);
		combiner.addSourceInfo(new SourceInfoBuilder()
				.setGpx("https://example.com/GPX/1/1", "https://example.com/GPX/1/1/gpx.xsd")
				.build());
		combiner.addSourceInfo(new SourceInfoBuilder()
				.setGpx("https://example.com/GPX/1/1", "https://example.com/GPX/1/1/gpx.xsd")
				.addExtension("tp", "https://example.com/trackpoint", "https://example.com/trackpoint.xsd")
				.build());
		combiner.addSourceInfo(new SourceInfoBuilder()
				.setGpx("https://example.com/GPX/1/1", "https://example.com/GPX/1/1/gpx.xsd")
				.addExtension("tp", "https://example.com/TrackPoint", "https://example.com/TrackPoint.Xsd")
				.addExtension("fit", "https://example.com/fitness", "https://example.com/fitness/fitness.xsd")
				.build());
		String locations = combiner.getAllLocations("default1 default2");
		String expected = "https://example.com/GPX/1/1 https://example.com/GPX/1/1/gpx.xsd"
				+ " https://example.com/trackpoint https://example.com/trackpoint.xsd"
				+ " https://example.com/fitness https://example.com/fitness/fitness.xsd";
		Assertions.assertEquals(expected, locations);
	}

	private static class SourceInfoBuilder
	{
		private ExtensionInfo _extn = new ExtensionInfo();
		/** Build into a SourceInfo */
		SourceInfo build()
		{
			SourceInfo info = new SourceInfo("file name",  FileType.GPX);
			info.setExtensionInfo(_extn);
			return info;
		}
		SourceInfoBuilder setGpx(String inId, String inUrl)
		{
			_extn.setNamespace(inId);
			_extn.addXsiAttribute(inId);
			_extn.addXsiAttribute(inUrl);
			return this;
		}
		SourceInfoBuilder addExtension(String inPrefix, String inId, String inUrl)
		{
			_extn.addNamespace(inPrefix, inId);
			_extn.addXsiAttribute(inId);
			_extn.addXsiAttribute(inUrl);
			return this;
		}
	}
}
