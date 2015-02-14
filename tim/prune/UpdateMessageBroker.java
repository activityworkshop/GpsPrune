package tim.prune;

/**
 * Class responsible for distributing update information
 * to all registered listeners
 */
public abstract class UpdateMessageBroker
{
	private static final int MAXIMUM_NUMBER_SUBSCRIBERS = 6;
	private static DataSubscriber[] _subscribers = new DataSubscriber[MAXIMUM_NUMBER_SUBSCRIBERS];
	private static int _subscriberNum = 0;


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
		for (int i=0; i<_subscribers.length; i++)
		{
			if (_subscribers[i] != null)
			{
				_subscribers[i].actionCompleted(inMessage);
			}
		}
	}
}
