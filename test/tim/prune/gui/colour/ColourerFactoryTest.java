package tim.prune.gui.colour;

import java.awt.Color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.gui.colour.ColourerFactory.ColourerId;

public class ColourerFactoryTest
{
	@Test
	public void testColourerNull()
	{
		Assertions.assertNull(ColourerFactory.createColourer(null));
		Assertions.assertNull(ColourerFactory.createColourer(""));
		Assertions.assertNull(ColourerFactory.createColourer("none"));
		Assertions.assertNull(ColourerFactory.createColourer(";;;;"));
		Assertions.assertNull(ColourerFactory.createColourer("n;n;n;n;"));
		Assertions.assertNull(ColourerFactory.createColourer("x;n;n;n;"));
	}

	@Test
	public void testColourerByFileNoColours()
	{
		// Fails because number of colours can't be parsed
		Assertions.assertNull(ColourerFactory.createColourer("f;n;n;n;"));
		// Colourer by file, but with 0 colours and no start/end
		PointColourer colourer = ColourerFactory.createColourer("f;n;n;0;");
		Assertions.assertTrue(colourer instanceof FileColourer);
		Assertions.assertTrue(colourer.isDiscrete());
		Assertions.assertEquals(0, colourer.getMaxColours());
		((DiscretePointColourer) colourer).generateDiscreteColours(3);
		Assertions.assertEquals(Color.BLUE, ((DiscretePointColourer) colourer).getDiscreteColour(0));
		Assertions.assertEquals(Color.BLUE, ((DiscretePointColourer) colourer).getDiscreteColour(1));
		Assertions.assertEquals(Color.BLUE, ((DiscretePointColourer) colourer).getDiscreteColour(2));

		Assertions.assertEquals(ColourerId.BY_FILE, ColourerFactory.getId(colourer));
		Assertions.assertEquals("f;0000ff;0000ff;0", ColourerFactory.pointColourerToString(colourer));
	}

	@Test
	public void testColourerByFileBlackToWhite()
	{
		// Colourer by file, with 3 colours from black to white
		PointColourer colourer = ColourerFactory.createColourer("f;000000;ffffff;3;w");
		Assertions.assertTrue(colourer instanceof FileColourer);
		Assertions.assertTrue(colourer.isDiscrete());
		Assertions.assertEquals(3, colourer.getMaxColours());
		Assertions.assertTrue(colourer.isWideHueScaling());
		DiscretePointColourer dc = (DiscretePointColourer) colourer;
		dc.generateDiscreteColours(3);
		Assertions.assertEquals(Color.BLACK, dc.getDiscreteColour(0));
		Assertions.assertEquals(Color.GRAY, dc.getDiscreteColour(1));
		Assertions.assertEquals(Color.WHITE, dc.getDiscreteColour(2));
	}

	@Test
	public void testColourerByAltitudeYellowToGreen()
	{
		// Continuous colourer by altitude
		PointColourer colourer = ColourerFactory.createColourer("a;ffff00;00ff00;3;w");
		Assertions.assertTrue(colourer instanceof AltitudeColourer);
		Assertions.assertFalse(colourer.isDiscrete());
		Assertions.assertEquals(-1, colourer.getMaxColours());
		Assertions.assertTrue(colourer.isWideHueScaling());
		Assertions.assertEquals(Color.YELLOW, colourer.getStartColour());
		Assertions.assertEquals(Color.GREEN, colourer.getEndColour());
		Assertions.assertEquals(ColourerId.BY_ALTITUDE, ColourerFactory.getId(colourer));
		Assertions.assertEquals("a;ffff00;00ff00;0;w", ColourerFactory.pointColourerToString(colourer));
	}
}
