package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Field;

import javax.swing.JOptionPane;

public abstract class TimeSensitiveFunction extends GenericFunction
{
	private static boolean _mangleTimestampsConfirmed = false;

	public TimeSensitiveFunction(App inApp) {
		super(inApp);
	}

	/**
	 * If necessary, ask the user to confirm the operation
	 * @return true if it's ok to proceed
	 */
	protected boolean confirmTimestampMangling()
	{
		if (_mangleTimestampsConfirmed) {
			return true;
		}
		// check whether Timestamp field exists, and if so confirm reversal
		int selStart = _app.getTrackInfo().getSelection().getStart();
		int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (!_app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP, selStart, selEnd)) {
			return true;
		}
		if (JOptionPane.showConfirmDialog(_app.getFrame(),
				I18nManager.getText("dialog.confirmmangletimes.text"),
				I18nManager.getText("dialog.confirmmangletimes.title"),
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			_mangleTimestampsConfirmed = true;
			return true;
		}
		return false;
	}
}
