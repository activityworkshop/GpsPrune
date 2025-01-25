package tim.prune.data;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensionInfoTest
{
	@Test
	public void testNamespace()
	{
		ExtensionInfo info = new ExtensionInfo();
		Assertions.assertNull(info.getNamespace());
		info.setNamespace("mynamespace");
		Assertions.assertEquals("mynamespace", info.getNamespace());
	}

	@Test
	public void testAddNamespaces()
	{
		ExtensionInfo info = new ExtensionInfo();
		info.addNamespace("myprefix", "myurl");
		info.addNamespace("secondprefix", "anotherurl");

		List<String> extensions = info.getExtensions();
		Assertions.assertEquals(2, extensions.size());
		Assertions.assertEquals("myurl", extensions.get(0));
		Assertions.assertEquals("anotherurl", extensions.get(1));

		List<String> namespaces = info.getExtraNamespaces();
		Assertions.assertEquals(2, namespaces.size());
		Assertions.assertEquals("xmlns:myprefix=\"myurl\"", namespaces.get(0));
		Assertions.assertEquals("xmlns:secondprefix=\"anotherurl\"", namespaces.get(1));
	}

	@Test
	public void testGetLocations()
	{
		ExtensionInfo info = new ExtensionInfo();
		info.setNamespace("mynamespace");
		info.setXsi("http://www.w3.org/2001/XMLSchema-instance");
		info.addXsiAttribute("mynamespace");
		info.addXsiAttribute("mainurl");
		info.addXsiAttribute("myidentifier");
		info.addXsiAttribute("myurl");
		info.addXsiAttribute("anotheridentifier");
		info.addXsiAttribute("anotherurl");
		info.addNamespace("myprefix", "myidentifier");
		info.addNamespace("secondprefix", "anotheridentifier");

		String locations = info.getSchemaLocations();
		Assertions.assertEquals("mynamespace mainurl myidentifier myurl anotheridentifier anotherurl", locations);
	}

	@Test
	public void testGetNamespaceName()
	{
		ExtensionInfo info = new ExtensionInfo();
		info.setNamespace("mynamespace");
		info.setXsi("http://www.w3.org/2001/XMLSchema-instance");
		info.addNamespace("ns3", "http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
		info.addNamespace("nov", "http://www.garmin.com/xmlschemas/TrackPointExtension/bandcamp");
		info.addNamespace("noslash", "not even a url");
		info.addNamespace("justoneslash", "TrackPointExtension/v2");

		Assertions.assertNull(info.getNamespaceName("ns2"));
		Assertions.assertNull(info.getNamespaceName("nov"));
		Assertions.assertNull(info.getNamespaceName("noslash"));
		Assertions.assertNull(info.getNamespaceName("justoneslash"));

		Assertions.assertEquals("TrackPointExtension", info.getNamespaceName("ns3"));
	}
}
