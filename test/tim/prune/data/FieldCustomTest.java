package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldCustomTest
{
	@Test
	public void testNames()
	{
		Field field = new FieldCustom("my fieldname");
		Assertions.assertEquals("my fieldname", field.getName());
	}

	@Test
	public void testBuiltIn()
	{
		Field field = new FieldCustom("my fieldname");
		Assertions.assertFalse(field.isBuiltIn());
	}

	@Test
	public void testMatchesFileType()
	{
		Field field = new FieldCustom("my fieldname");
		Assertions.assertTrue(field.matchesFileType(FileType.TEXT));
		Assertions.assertFalse(field.matchesFileType(FileType.GPX));
		Assertions.assertFalse(field.matchesFileType(FileType.KML));
	}

	@Test
	public void testEquals()
	{
		Field field = new FieldCustom("my fieldname");
		Assertions.assertFalse(Field.LONGITUDE.equals(field));
		Assertions.assertFalse(field.equals(Field.LONGITUDE));
		Assertions.assertTrue(field.equals(field));
	}
}
