package tim.prune;

/**
 * Class responsible for distributing update information
 * to all registered listeners
 */
public abstract class UpdateMessageBroker
{
	private static final int MAXIMUM_NUMBER_SUBSCRIBERS = 7;
	/** Array of all subscribers */
	private static DataSubscriber[] _subscribers = new DataSubscriber[MAXIMUM_NUMBER_SUBSCRIBERS];
	/** Counter of the number of subscribers added so far */
	private static int _subscriberNum = 0;
	/** Enable/disabled flag */
	private static boolean _enabled = true;


	/**
	 * Add a data subscriber to the list
	 * @param inSub DataSubscriber to add
	 */
	public static void addSubscriber(DataSubscriber inSub)
	{
		_subscribers[_subscriberNum] = inSub;
		_subscriberNum++;
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
