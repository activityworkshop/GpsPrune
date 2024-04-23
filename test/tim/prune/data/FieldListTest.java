package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for field list
 */
class FieldListTest
{
	@Test
	void testEmptyList()
	{
		FieldList empty = new FieldList();
		assertEquals(0, empty.getNumFields());
		assertEquals(-1, empty.getFieldIndex(null));
		assertEquals(-1, empty.getFieldIndex(Field.LATITUDE));
		assertFalse(empty.contains(Field.LATITUDE));
		assertNull(empty.getField(0));
		assertNull(empty.getField(1));
		assertEquals("()", empty.toString());
	}

	@Test
	void testAddSingleField()
	{
		FieldList fields = new FieldList();
		assertEquals(0, fields.getNumFields());
		assertEquals("()", fields.toString());
		fields.addField(Field.LATITUDE);

		assertEquals(1, fields.getNumFields());
		assertEquals(-1, fields.getFieldIndex(null));
		assertEquals(0, fields.getFieldIndex(Field.LATITUDE));
		assertEquals(-1, fields.getFieldIndex(Field.LONGITUDE));
		assertTrue(fields.contains(Field.LATITUDE));
		assertFalse(fields.contains(Field.LONGITUDE));
		assertNull(fields.getField(-1));
		assertEquals(Field.LATITUDE, fields.getField(0));
		assertNull(fields.getField(1));
		assertNotEquals("()", fields.toString());
	}

	@Test
	void testAddSingleField_alreadyPresent()
	{
		FieldList fields = new FieldList();
		assertEquals(0, fields.getNumFields());
		fields.addField(Field.LATITUDE);
		fields.addField(Field.LATITUDE);
		fields.addField(Field.LATITUDE);

		assertEquals(1, fields.getNumFields());
		assertEquals(0, fields.getFieldIndex(Field.LATITUDE));
		assertEquals(-1, fields.getFieldIndex(Field.LONGITUDE));
		assertTrue(fields.contains(Field.LATITUDE));
		assertFalse(fields.contains(Field.LONGITUDE));
		assertEquals(Field.LATITUDE, fields.getField(0));
		assertNull(fields.getField(1));
		assertNotEquals("()", fields.toString());
	}

	@Test
	void testAddMultipleFields()
	{
		FieldList fields = new FieldList();
		assertEquals(0, fields.getNumFields());
		fields.addFields(Field.LATITUDE, Field.LONGITUDE, Field.LATITUDE);

		assertEquals(2, fields.getNumFields());
		assertEquals(0, fields.getFieldIndex(Field.LATITUDE));
		assertEquals(1, fields.getFieldIndex(Field.LONGITUDE));
		assertTrue(fields.contains(Field.LATITUDE));
		assertTrue(fields.contains(Field.LONGITUDE));
		assertEquals(Field.LATITUDE, fields.getField(0));
		assertEquals(Field.LONGITUDE, fields.getField(1));
		assertNotEquals("()", fields.toString());
	}

	@Test
	void testMergeFromEmpty()
	{
		FieldList fields1 = new FieldList();
		FieldList fields2 = new FieldList(Field.ALTITUDE, Field.DESCRIPTION);

		FieldList combined = fields1.merge(fields2);
		assertEquals(2, combined.getNumFields());
		assertEquals(0, combined.getFieldIndex(Field.ALTITUDE));
		assertEquals(1, combined.getFieldIndex(Field.DESCRIPTION));
		assertEquals(-1, combined.getFieldIndex(Field.LONGITUDE));

		FieldList combined2 = fields2.merge(fields1);
		assertEquals(2, combined2.getNumFields());
		assertEquals(0, combined2.getFieldIndex(Field.ALTITUDE));
		assertEquals(1, combined2.getFieldIndex(Field.DESCRIPTION));
		assertEquals(-1, combined2.getFieldIndex(Field.LONGITUDE));
	}

	@Test
	void testMergeWithOverlap()
	{
		FieldList fields1 = new FieldList(Field.SPEED, Field.ALTITUDE, Field.TIMESTAMP);
		FieldList fields2 = new FieldList(Field.ALTITUDE, Field.SPEED, Field.DESCRIPTION);

		FieldList combined = fields1.merge(fields2);
		assertEquals(4, combined.getNumFields());
		assertEquals(0, combined.getFieldIndex(Field.SPEED));
		assertEquals(1, combined.getFieldIndex(Field.ALTITUDE));
		assertEquals(2, combined.getFieldIndex(Field.TIMESTAMP));
		assertEquals(3, combined.getFieldIndex(Field.DESCRIPTION));
		assertEquals(-1, combined.getFieldIndex(Field.LONGITUDE));

		// Field order is different when merging the other way
		FieldList combined2 = fields2.merge(fields1);
		assertEquals(4, combined2.getNumFields());
		assertEquals(0, combined2.getFieldIndex(Field.ALTITUDE));
		assertEquals(1, combined2.getFieldIndex(Field.SPEED));
		assertEquals(2, combined2.getFieldIndex(Field.DESCRIPTION));
		assertEquals(3, combined2.getFieldIndex(Field.TIMESTAMP));
		assertEquals(-1, combined2.getFieldIndex(Field.LONGITUDE));
	}
}