package tim.prune.gui.map;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;

import tim.prune.config.Config;
import tim.prune.gui.IconManager;

public class WaypointIcons
{
	/** @return an icon definition to use for waypoints based on the current config */
	public static WpIconDefinition getDefinition(Config inConfig, IconManager inIconManager)
	{
		if (inConfig.getConfigBoolean(Config.KEY_WAYPOINT_ICON_CUSTOM))
		{
			final String iconPath = inConfig.getConfigString(Config.KEY_WAYPOINT_ICON_PATH);
			WpIconDefinition iconDef = getCustomIcon(iconPath);
			if (iconDef != null) {
				return iconDef;
			}
		}
		// No valid custom icon, so find out the built-in type
		final int wpType = inConfig.getConfigInt(Config.KEY_WAYPOINT_ICONS);
		if (wpType == WpIconLibrary.WAYPT_DEFAULT) {
			return null;
		}
		final int wpSize = inConfig.getConfigInt(Config.KEY_WAYPOINT_ICON_SIZE);
		return WpIconLibrary.getIconDefinition(wpType, wpSize, inIconManager);
	}

	/** @return an icon definition from the given file path, or null */
	private static WpIconDefinition getCustomIcon(String inIconPath)
	{
		if (inIconPath == null || inIconPath.isEmpty()) {
			return null;
		}
		File iconFile = new File(inIconPath);
		if (!iconFile.exists() || !iconFile.isFile() || !iconFile.canRead()) {
			return null;
		}
		try
		{
			ImageIcon icon = new ImageIcon(iconFile.toURI().toURL());
			WpIconDefinition iconDef = new WpIconDefinition(icon);
			// If icon is too small or too big, we ignore it and use a built-in one instead
			if (iconDef.getXOffset() > 2 && iconDef.getXOffset() <= 64
				&& iconDef.getYOffset() > 2 && iconDef.getYOffset() <= 64)
			{
				return iconDef;
			}
		} catch (MalformedURLException ignored) {}
		return null;
	}
}
