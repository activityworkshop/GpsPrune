package tim.prune.gui.map;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.config.Config;
import tim.prune.gui.IconManager;

public class WaypointIconsTest
{
	@Test
	public void testDefault()
	{
		// Config says to use default icon
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, false);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_DEFAULT);
		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, null);
		Assertions.assertNull(iconDef);
	}

	@Test
	public void testNonExistentFileWithDefault()
	{
		// Config says to use a custom icon but it's blank or doesn't exist
		// fallback is default
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, true);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_DEFAULT);
		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, null);
		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, null);
		Assertions.assertNull(iconDef);

		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, "");
		iconDef = WaypointIcons.getDefinition(config, null);
		Assertions.assertNull(iconDef);

		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, "doesnotexist.png");
		iconDef = WaypointIcons.getDefinition(config, null);
		Assertions.assertNull(iconDef);
	}

	@Test
	public void testNonExistentFileWithFlag()
	{
		// Config says to use a custom icon but it's blank or doesn't exist
		// fallback is a built-in icon
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, true);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_FLAG);
		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, "doesnotexist.png");
		IconManager iconManager = new IconManager(true); // double sized icons
		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, iconManager);
		Assertions.assertNotNull(iconDef);
		Assertions.assertNotNull(iconDef.getImageIcon());
		Assertions.assertEquals(29, iconDef.getXOffset());
	}

	@Test
	public void testBuiltIn()
	{
		// Config says to use a built-in icon
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, false);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_CIRCLE);
		config.setConfigInt(Config.KEY_WAYPOINT_ICON_SIZE, 2);
		IconManager iconManager = new IconManager(false);
		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, iconManager);
		Assertions.assertNotNull(iconDef);
		Assertions.assertNotNull(iconDef.getImageIcon());
		Assertions.assertEquals(15, iconDef.getXOffset());
	}

	@Test
	public void testCustom() throws URISyntaxException
	{
		// Config says to use a custom icon from this test folder
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, true);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_DEFAULT);
		String pathToFile = new File(getClass().getResource("square.png").toURI()).getAbsolutePath();
		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, pathToFile);
		IconManager iconManager = new IconManager(false);

		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, iconManager);
		Assertions.assertNotNull(iconDef);
		Assertions.assertNotNull(iconDef.getImageIcon());
		Assertions.assertEquals(16, iconDef.getXOffset());
	}

	@Test
	public void testCustomSizeCheck() throws URISyntaxException
	{
		// Square image is an acceptable size so it is taken
		testCustomSize("square.png", true);
		// These images are either too small or too large, so they're ignored
		// and the default icon (in this case null) is used instead
		testCustomSize("too_small.png", false);
		testCustomSize("too_large.png", false);
	}

	private void testCustomSize(String inFilename, boolean inExpectOk) throws URISyntaxException
	{
		// Config says to use a custom icon from this test folder
		Config config = new Config();
		config.setConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM, true);
		config.setConfigInt(Config.KEY_WAYPOINT_ICONS, WpIconLibrary.WAYPT_DEFAULT);
		String pathToFile = new File(getClass().getResource(inFilename).toURI()).getAbsolutePath();
		config.setConfigString(Config.KEY_WAYPOINT_ICON_PATH, pathToFile);
		IconManager iconManager = new IconManager(false);

		WpIconDefinition iconDef = WaypointIcons.getDefinition(config, iconManager);
		if (inExpectOk)
		{
			Assertions.assertNotNull(iconDef);
			Assertions.assertNotNull(iconDef.getImageIcon());
			Assertions.assertEquals(16, iconDef.getXOffset());
		}
		else {
			Assertions.assertNull(iconDef);
		}
	}
}
