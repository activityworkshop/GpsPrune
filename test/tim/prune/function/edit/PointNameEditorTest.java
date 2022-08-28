package tim.prune.function.edit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PointNameEditorTest
{
	@Test
	void testTitleCaseEmpty()
	{
		assertEquals("", PointNameEditor.titleCase(null));
		assertEquals("", PointNameEditor.titleCase(""));
	}

	@Test
	void testTitleCaseNoChange()
	{
		final String[] names = {"A", "1", "-", " ", " A", "  A", "Name", "Name.surname"};
		for (String name : names) {
			assertEquals(name, PointNameEditor.titleCase(name));
		}
	}

	@Test
	void testTitleCaseChange()
	{
		assertEquals("Abc", PointNameEditor.titleCase("abc"));
		assertEquals(" Abc", PointNameEditor.titleCase(" aBc"));
		assertEquals("Name Surname", PointNameEditor.titleCase("nAME sURNAME"));
		assertEquals("Name:surname", PointNameEditor.titleCase("nAME:sURNAME"));
		assertEquals("Point  Type", PointNameEditor.titleCase("point  type"));
	}
}
