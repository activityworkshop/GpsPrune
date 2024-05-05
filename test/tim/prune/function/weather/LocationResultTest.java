package tim.prune.function.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LocationResultTest
{
	@Test
	public void testEmptyJson()
	{
		LocationResult result = LocationResult.fromString(null);
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("", result.getErrorMessage());

		result = LocationResult.fromString("[{}]");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("", result.getErrorMessage());

		result = LocationResult.fromString("[{\"lat\":1.234,\"lon\":45.678}]");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("", result.getErrorMessage());
	}

	@Test
	public void testJsonWithError()
	{
		LocationResult result = LocationResult.fromString("{\"cod\":\"400\",\"message\":\"wrong latitude\"}");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("wrong latitude", result.getErrorMessage());

		result = LocationResult.fromString("{\"message\":\"this one's got \\\"quotes\\\"!\"}");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("this one's got \\\"quotes\\\"!", result.getErrorMessage());
	}

	@Test
	public void testJsonWithJustName()
	{
		LocationResult result = LocationResult.fromString("{\"cod\":\"400\",\"message\":\"wrong latitude\"}");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("wrong latitude", result.getErrorMessage());

		result = LocationResult.fromString("{\"message\":\"this one's got \\\"quotes\\\"!\"}");
		Assertions.assertTrue(result.isError());
		Assertions.assertEquals("this one's got \\\"quotes\\\"!", result.getErrorMessage());
	}
}
