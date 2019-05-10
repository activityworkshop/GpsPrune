package tim.prune.gui.colour;

import tim.prune.App;
import tim.prune.DataSubscriber;

/**
 * Caretaker of the current PointColourer, responsible for listening
 * to data changes and updating the colourer
 */
public class ColourerCaretaker implements DataSubscriber
{
	/** App object for getting the track */
	private App _app = null;
	/** PointColourer object for passing details to */
	private PointColourer _colourer = null;

	/**
	 * Constructor
	 * @param inApp app object to use
	 */
	public ColourerCaretaker(App inApp)
	{
		_app = inApp;
	}

	/**
	 * @param inColourer current colourer object
	 */
	public void setColourer(PointColourer inColourer)
	{
		_colourer = inColourer;
		dataUpdated(ALL);
	}

	/**
	 * @return point colourer, or null
	 */
	public PointColourer getColourer()
	{
		return _colourer;
	}

	/**
	 * Data has been updated
	 */
	public void dataUpdated(byte inUpdateType)
	{
		if ((inUpdateType &
			(DataSubscriber.DATA_ADDED_OR_REMOVED | DataSubscriber.DATA_EDITED | DataSubscriber.UNITS_CHANGED)) > 0
			&& _colourer != null)
		{
			_colourer.calculateColours(_app.getTrackInfo());
		}
	}

	/** Don't care about status */
	public void actionCompleted(String inMessage) {}
}
