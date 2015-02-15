package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;

public class SetLineWidth extends GenericFunction
{

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SetLineWidth(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.setlinewidth";
	}


	/**
	 * Run function
	 */
	public void begin()
	{
		int currLineWidth = Config.getConfigInt(Config.KEY_LINE_WIDTH);
		if (currLineWidth < 1 || currLineWidth > 4) {
			currLineWidth = 2;
		}
		Object lineWidthStr = JOptionPane.showInputDialog(_app.getFrame(),
			I18nManager.getText("dialog.setlinewidth.text"),
			I18nManager.getText(getNameKey()),
			JOptionPane.QUESTION_MESSAGE, null, null, "" + currLineWidth);
		if (lineWidthStr != null)
		{
			int lineWidth = 2;
			try {
				lineWidth = Integer.parseInt(lineWidthStr.toString());
				if (lineWidth >= 1 && lineWidth <= 4 && lineWidth != currLineWidth)
				{
					Config.setConfigInt(Config.KEY_LINE_WIDTH, lineWidth);
					UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
				}
			}
			catch (NumberFormatException nfe) {};
		}
	}
}
