package tim.prune.function.estimate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EstimationParametersTest
{
	@Test
	void testTwoDp()
	{
		assertEquals("0.00", EstimationParameters.twoDp(0.0));
		assertEquals("2.00", EstimationParameters.twoDp(2.0));
		assertEquals("1.50", EstimationParameters.twoDp(1.50));
		assertEquals("1.50", EstimationParameters.twoDp(1.499));
		assertEquals("-1.50", EstimationParameters.twoDp(-1.499));
		assertEquals("131.04", EstimationParameters.twoDp(131.041));
	}

	@Test
	void testParseInvalidConfig()
	{
		assertNull(EstimationParameters.fromConfigString(null));
		assertNull(EstimationParameters.fromConfigString(""));
		assertNull(EstimationParameters.fromConfigString("abcde"));
		assertNull(EstimationParameters.fromConfigString("1.05;"));
		assertNull(EstimationParameters.fromConfigString(".;.;.;.;."));
		assertNull(EstimationParameters.fromConfigString("1.0;2.0;0.1;0.1;."));
	}

	@Test
	void testParseValidConfig()
	{
		EstimationParameters params = EstimationParameters.fromConfigString("1;2;3;4;5");
		assertEquals("1.00;2.00;3.00;4.00;5.00", params.toConfigString());
		params = EstimationParameters.fromConfigString("-1;-0.1;3.123;40;500");
		assertEquals("-1.00;-0.10;3.12;40.00;500.00", params.toConfigString());
	}
}
