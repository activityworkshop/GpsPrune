package tim.prune.function.filesleuth.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTrackContents
{
	@Test
	public void testEmptyStrings()
	{
		TrackContents contents = new TrackContents(null);
		contents.addString(null);
		contents.addString("");
		contents.addString("   ");
		assertEquals(0, contents.getNumStrings());
	}

	@Test
	public void testDuplicateStrings()
	{
		TrackContents contents = new TrackContents(null);
		contents.addString("broccoli");
		contents.addString("BROCCOLI");
		contents.addString("   Broccoli");
		contents.addString("");
		assertEquals(1, contents.getNumStrings());
	}
}
