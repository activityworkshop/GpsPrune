package tim.prune.save.xml;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensionTreeTest
{
	@Test
	public void testEmptyTree()
	{
		ExtensionTree tree = new ExtensionTree();
		List<String> tags = tree.getAllTags(0);
		Assertions.assertTrue(tags.isEmpty());

		tree.addTag(null, null);
		tree.addTag(null, "");
		tags = tree.getAllTags(0);
		Assertions.assertTrue(tags.isEmpty());
	}

	@Test
	public void testSingleLevel()
	{
		ExtensionTree tree = new ExtensionTree();
		tree.addTag(new String[] {}, "firstTag");
		tree.addTag(new String[] {}, "secondTag");
		List<String> tags = tree.getAllTags(0);
		Assertions.assertEquals(2, tags.size());
		// Need to trim results because indentation is added before each string
		Assertions.assertEquals("firstTag", tags.get(0).trim());
		Assertions.assertEquals("secondTag", tags.get(1).trim());
	}

	@Test
	public void testTwoLevels()
	{
		ExtensionTree tree = new ExtensionTree();
		tree.addTag(new String[] {}, "first base tag");
		tree.addTag(new String[] {"group1"}, "first tag in group1");
		tree.addTag(new String[] {"group1"}, "second tag in group1");
		tree.addTag(new String[] {}, "second base tag");
		List<String> tags = tree.getAllTags(0);
		Assertions.assertEquals(6, tags.size());
		// Need to trim results because indentation is added before each string
		Assertions.assertEquals("first base tag", tags.get(0).trim());
		Assertions.assertEquals("second base tag", tags.get(1).trim());
		Assertions.assertEquals("<group1>", tags.get(2).trim());
		Assertions.assertEquals("first tag in group1", tags.get(3).trim());
		Assertions.assertEquals("second tag in group1", tags.get(4).trim());
		Assertions.assertEquals("</group1>", tags.get(5).trim());
	}

	@Test
	public void testThreeLevels()
	{
		ExtensionTree tree = new ExtensionTree();
		tree.addTag(new String[] {}, "first base tag");
		tree.addTag(new String[] {"group1"}, "first tag in group1");
		tree.addTag(new String[] {"group2"}, "first tag in group2");
		tree.addTag(new String[] {"group3", "subgroup2"}, "another tag in subgroup2");
		tree.addTag(new String[] {"group2", "sub"}, "another");
		List<String> tags = tree.getAllTags(0);
		Assertions.assertEquals(15, tags.size());
		// Need to trim results because indentation is added before each string
		Assertions.assertEquals("first base tag", tags.get(0).trim());
		Assertions.assertEquals("<group1>", tags.get(1).trim());
		Assertions.assertEquals("first tag in group1", tags.get(2).trim());
		Assertions.assertEquals("</group1>", tags.get(3).trim());
		Assertions.assertEquals("<group2>", tags.get(4).trim());
		Assertions.assertEquals("first tag in group2", tags.get(5).trim());
		Assertions.assertEquals("<sub>", tags.get(6).trim());
		Assertions.assertEquals("another", tags.get(7).trim());
		Assertions.assertEquals("</sub>", tags.get(8).trim());
		Assertions.assertEquals("</group2>", tags.get(9).trim());
		Assertions.assertEquals("<group3>", tags.get(10).trim());
		Assertions.assertEquals("<subgroup2>", tags.get(11).trim());
		Assertions.assertEquals("another tag in subgroup2", tags.get(12).trim());
		Assertions.assertEquals("</subgroup2>", tags.get(13).trim());
		Assertions.assertEquals("</group3>", tags.get(14).trim());
	}
}
