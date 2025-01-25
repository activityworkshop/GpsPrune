package tim.prune.save;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionCombinerTest
{
	@Test
	public void testOnlyNullsAndEmpties()
	{
		VersionCombiner combiner = new VersionCombiner(List.of("1.0", "1.1"));
		Assertions.assertEquals("1.1", combiner.getBestVersion());
		combiner.addVersion(null);
		combiner.addVersion("");
		combiner.addVersion(null);
		Assertions.assertEquals("1.1", combiner.getBestVersion());
		combiner.addVersion("0.9"); // unsupported, ignored
		Assertions.assertEquals("1.1", combiner.getBestVersion());
	}

	@Test
	public void testOnlyLowerVersion()
	{
		VersionCombiner combiner = new VersionCombiner(List.of("1.0", "1.1"));
		Assertions.assertEquals("1.1", combiner.getBestVersion());
		combiner.addVersion("1.0");
		combiner.addVersion("");
		combiner.addVersion(null);
		combiner.addVersion("1.0");
		Assertions.assertEquals("1.0", combiner.getBestVersion());
	}

	@Test
	public void testMixOfVersions()
	{
		VersionCombiner combiner = new VersionCombiner(List.of("1.0", "1.1"));
		combiner.addVersion("1.2");
		Assertions.assertEquals("1.1", combiner.getBestVersion());
		combiner.addVersion("1.0");
		Assertions.assertEquals("1.0", combiner.getBestVersion());
		combiner.addVersion("1.1");
		Assertions.assertEquals("1.1", combiner.getBestVersion());
	}
}
