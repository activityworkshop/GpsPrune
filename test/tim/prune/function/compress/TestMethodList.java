package tim.prune.function.compress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tim.prune.function.compress.methods.DuplicatesMethod;
import tim.prune.function.compress.methods.NearbyFactorMethod;

public class TestMethodList
{
	@Test
	public void testSerialize_emptyList()
	{
		MethodList methods = new MethodList();
		Assertions.assertEquals("", methods.toConfigString());

		methods.add(null);
		Assertions.assertEquals("", methods.toConfigString());
	}

	@Test
	public void testSerialize_singleDuplicates()
	{
		MethodList methods = new MethodList();
		methods.add(new DuplicatesMethod());
		Assertions.assertEquals("oDUP:", methods.toConfigString());

		methods.get(0).setActive(true);
		Assertions.assertEquals("xDUP:", methods.toConfigString());
	}

	@Test
	public void testSerialize_twoMethods()
	{
		MethodList methods = new MethodList();
		methods.add(new DuplicatesMethod());
		methods.add(null);
		methods.add(new NearbyFactorMethod(200));
		methods.get(2).setActive(true);

		Assertions.assertEquals("oDUP:;xNEF:200", methods.toConfigString());
	}

	@Test
	public void testDeserialize_twoMethods()
	{
		String fromConfig = "-DUP:with spaces; ;xNEF:200";
		MethodList methods = MethodList.fromConfigString(fromConfig);
		Assertions.assertEquals(2, methods.size());
		Assertions.assertTrue(methods.get(0) instanceof DuplicatesMethod);
		Assertions.assertTrue(methods.get(1) instanceof NearbyFactorMethod);
		Assertions.assertTrue(methods.get(1).isActive());
		Assertions.assertEquals("xNEF:200", methods.get(1).getTotalSettingsString());
		Assertions.assertEquals("oDUP:;xNEF:200", methods.toConfigString());
	}
}
