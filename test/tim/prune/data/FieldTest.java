package tim.prune.data;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tim.prune.I18nManager;

public class FieldTest
{
	@BeforeEach
	public void setup() {
		I18nManager.init(Locale.UK);
	}

	@Test
	public void testNames()
	{
		Assertions.assertEquals("Longitude", Field.LONGITUDE.getName());
		Assertions.assertEquals("Latitude", Field.LATITUDE.getName());
	}

	@Test
	public void testBuiltIn()
	{
		Assertions.assertTrue(Field.LONGITUDE.isBuiltIn());
		Assertions.assertTrue(Field.LATITUDE.isBuiltIn());
	}

	@Test
	public void testMatchesFileType()
	{
		Assertions.assertTrue(Field.LONGITUDE.matchesFileType(FileType.TEXT));
		Assertions.assertTrue(Field.LONGITUDE.matchesFileType(FileType.GPX));
		Assertions.assertTrue(Field.LONGITUDE.matchesFileType(FileType.KML));
	}

	@Test
	public void testEquals()
	{
		Assertions.assertTrue(Field.LONGITUDE.equals(Field.LONGITUDE));
		Assertions.assertTrue(Field.LATITUDE.equals(Field.LATITUDE));
		Assertions.assertFalse(Field.LONGITUDE.equals(Field.LATITUDE));
		Assertions.assertFalse(Field.LATITUDE.equals(Field.LONGITUDE));
	}

	@Test
	public void testGetField()
	{
		Assertions.assertEquals(Field.LONGITUDE, Field.getField("Longitude"));
		Assertions.assertEquals(Field.LATITUDE, Field.getField("Latitude"));
		Assertions.assertNull(Field.getField("unknown name"));
	}
}
