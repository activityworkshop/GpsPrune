package tim.prune.gui;

import java.awt.Font;

import javax.swing.UIManager;

public class UiStyler
{
	/** @return the actual text scale factor used */
	public static int styleGui(String styleName, int textScaleFactor)
	{
		if (styleName != null && !styleName.isEmpty()) {
			setLookAndFeel(styleName);
		}

		final int clampedFactor = Math.max(100, Math.min(textScaleFactor, 300));
		if (clampedFactor > 100) {
			setTextScaleFactor(clampedFactor);
		}
		return clampedFactor;
	}

	private static void setLookAndFeel(String styleName)
	{
		try {
			UIManager.setLookAndFeel(styleName);
		}
		catch (Exception e) {
			System.err.println("Failed to set style '" + styleName + "': "
				+ e.getClass().getName() + ": " + e.getMessage());
		}
	}

	private static void setTextScaleFactor(int scaleFactor)
	{
		for (Object key : UIManager.getLookAndFeelDefaults().keySet())
		{
			if (key.toString().toLowerCase().endsWith("font")) {
				setFontSize(key, scaleFactor);
			}
		}
	}

	private static void setFontSize(Object key, int scaleFactor)
	{
		final Object currentValue = UIManager.getDefaults().get(key);
		if (currentValue instanceof Font)
		{
			final Font currentFont = (Font) currentValue;
			final int currentSize = currentFont.getSize();
			final float scaledSize = (float) (currentSize * 0.01 * scaleFactor);
			// System.out.println("Changing size of '" + key + "' from " + currentSize + " to " + scaledSize);
			UIManager.getDefaults().put(key, currentFont.deriveFont(scaledSize));

			final Font lafFont = (Font) UIManager.getLookAndFeelDefaults().get(key);
			UIManager.getLookAndFeelDefaults().put(key, lafFont.deriveFont(scaledSize));
		}
	}
}
