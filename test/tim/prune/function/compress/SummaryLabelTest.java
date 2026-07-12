package tim.prune.function.compress;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

public class SummaryLabelTest
{
	@BeforeAll
	public static void setup() {
		// If I18n isn't inited, then the strings "dialog.compress.summarylabel" can't get resolved
		I18nManager.init(Locale.UK);
	}

	@Test
	public void testNothingToDelete()
	{
		Track track = new Track();
		SummaryLabel label = new SummaryLabel(track);
		label.clearValue();
		Assertions.assertEquals("Points to delete: 0", label.getText());

		label.setValue(0);
		Assertions.assertEquals("Points to delete: 0", label.getText());
	}

	@Test
	public void testOneOrTwoOfTwo()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(0.4, 0.5));
		track.appendPoint(new DataPoint(1.4, -1.5));
		Assertions.assertEquals(2, track.getNumPoints());

		SummaryLabel label = new SummaryLabel(track);
		label.setValue(1);
		Assertions.assertEquals("Points to delete: 1 (50%)", label.getText());
		label.setValue(2);
		Assertions.assertEquals("Points to delete: 2 (100%)", label.getText());
	}
}
