package tim.prune.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tim.prune.I18nManager;

public class FieldXmlTest
{
	@BeforeEach
	public void setup() {
		I18nManager.init(Locale.UK);
	}

	@AfterAll
	public static void tearDown() {
		FieldRecogniser.clearMap();
	}

	@Test
	public void testNames()
	{
		Field field = new FieldXml(FileType.GPX, "gpxtag", new ArrayList<>());
		Assertions.assertEquals("gpxtag", field.getName());

		field = new FieldXml(FileType.GPX, "gpxtag:hr", new ArrayList<>());
		Assertions.assertEquals("Heart rate", field.getName());
	}

	@Test
	public void testgetTag()
	{
		FieldXml field = new FieldXml(FileType.GPX, "gpxtag", new ArrayList<>());
		Assertions.assertEquals("<gpxtag>120</gpxtag>", field.getTag("120"));

		field = new FieldXml(FileType.GPX, "gpxtag:hr", new ArrayList<>());
		Assertions.assertEquals("<gpxtag:hr>77.4</gpxtag:hr>", field.getTag("77.4"));
	}

	@Test
	public void testBuiltIn()
	{
		Field field = new FieldXml(FileType.GPX, "gpxtag", new ArrayList<>());
		Assertions.assertFalse(field.isBuiltIn());
	}

	@Test
	public void testMatchesFileType()
	{
		Field field = new FieldXml(FileType.GPX, "gpxtag", new ArrayList<>());
		Assertions.assertFalse(field.matchesFileType(FileType.TEXT));
		Assertions.assertTrue(field.matchesFileType(FileType.GPX));
		Assertions.assertFalse(field.matchesFileType(FileType.KML));
	}

	@Test
	public void testEquals()
	{
		Field field1 = new FieldXml(FileType.GPX, "gpxtag", List.of("TrackPointExtensions"));
		Field field2 = new FieldXml(FileType.GPX, "gpxtag", List.of("TrackPointExtensions"));
		Field field3 = new FieldXml(FileType.GPX, "gpxtag", List.of("DifferentExtension"));
		Assertions.assertTrue(field1.equals(field2));
		Assertions.assertTrue(field2.equals(field1));
		Assertions.assertTrue(field2.equals(field2));
		Assertions.assertFalse(field2.equals(field3));
		Assertions.assertFalse(field3.equals(field2));
	}

	@Test
	public void testEqualsWithField()
	{
		Field field1 = Field.PHOTO;
		Field field2 = new FieldXml(FileType.GPX, "fieldname.photo", new ArrayList<>());
		Assertions.assertFalse(field1.equals(field2));
		Assertions.assertFalse(field2.equals(field1));
	}
}
