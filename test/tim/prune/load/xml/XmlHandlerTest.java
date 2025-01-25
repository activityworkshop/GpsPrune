package tim.prune.load.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.ExtensionInfo;

public class XmlHandlerTest
{
	private static class DummyHandler extends XmlHandler
	{
		public String[][] getDataArray() {
			return null;
		}
		public String getFileTitle() {
			return null;
		}
		@Override
		public ExtensionInfo getExtensionInfo() {
			return null;
		}
	}

	@Test
	public void testFileVersionNumber()
	{
		DummyHandler handler = new DummyHandler();
		Assertions.assertEquals("", handler.getFileVersion());

		// Unrecognised version numbers are ignored
		handler.setFileVersion(null);
		Assertions.assertEquals("", handler.getFileVersion());
		handler.setFileVersion("");
		Assertions.assertEquals("", handler.getFileVersion());
		handler.setFileVersion("10");
		Assertions.assertEquals("", handler.getFileVersion());
		handler.setFileVersion("a.b");
		Assertions.assertEquals("", handler.getFileVersion());

		// If in the correct format, then it's stored as an int
		handler.setFileVersion("1.0");
		Assertions.assertEquals("1.0", handler.getFileVersion());
		handler.setFileVersion("2.2");
		Assertions.assertEquals("2.2", handler.getFileVersion());
	}
}
