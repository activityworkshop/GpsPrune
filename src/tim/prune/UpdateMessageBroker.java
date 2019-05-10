package tim.prune;

/**
 * Class responsible for distributing update information
 * to all registered listeners
 */
public abstract class UpdateMessageBroker
{
	private static final int MAXIMUM_NUMBER_SUBSCRIBERS = 8;
	/** Array of all subscribers */
	private static DataSubscriber[] _subscribers = new DataSubscriber[MAXIMUM_NUMBER_SUBSCRIBERS];
	/** Index from which to start looking for an empty slot*/
	private static int _searchStartIndex = 0;
	/** Enable/disabled flag */
	private static boolean _enabled = true;


	/**
	 * Add a data subscriber to the list
	 * @param inSub DataSubscriber to add
	 */
	public static void addSubscriber(DataSubscriber inSub)
	{
		// Loop looking for first null entry
		for (int i=_searchStartIndex; i<MAXIMUM_NUMBER_SUBSCRIBERS; i++)
		{
			if (_subscribers[i] == null)
			{
				_subscribers[i] = inSub;
				_searchStartIndex = i+1;
				break;
			}
		}
	}

	/**
	 * Remove the given subscriber from the list
	 * @param inSub subscriber to remove
	 */
	public static void removeSubscriber(DataSubscriber inSub)
	{
		for (int i=0; i<MAXIMUM_NUMBER_SUBSCRIBERS; i++)
		{
			if (_subscribers[i] == inSub)
			{
				_subscribers[i] = null;
				// Could break out of the loop here but we want to make sure we remove all of them
			}
		}
		_searchStartIndex = 0; // for the next add, start from beginning to ensure all gaps are filled
	}

	/**
	 * Enable or disable the messaging (to allow temporary disabling for multiple operations)
	 * @param inEnabled false to disable, true to enable again
	 */
	public static void enableMessaging(boolean inEnabled)
	{
		_enabled = inEnabled;
	}

	/**
	 * Send a message to all subscribers that
	 * the data has been updated
	 */
	public static void informSubscribers()
	{
		informSubscribers(DataSubscriber.ALL);
	}


	/**
	 * Send message to all subscribers
	 * @param inChange Change that occurred
	 */
	public static void informSubscribers(byte inChange)
	{
		// TODO: Launch separate thread so that whatever caused the inform can finish
		if (!_enabled) return;
		for (int i=0; i<_subscribers.length; i++)
		{
			if (_subscribers[i] != null)
			{
				_subscribers[i].dataUpdated(inChange);
			}
		}
	}

	/**
	 * Send message to all subscribers
	 * @param inMessage message to display informing of action completed
	 */
	public static void informSubscribers(String inMessage)
	{
		if (!_enabled) return;
		for (int i=0; i<_subscribers.length; i++)
		{
			if (_subscribers[i] != null)
			{
				_subscribers[i].actionCompleted(inMessage);
			}
		}
	}
}
