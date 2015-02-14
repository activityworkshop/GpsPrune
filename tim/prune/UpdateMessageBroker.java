package tim.prune;

/**
 * Class responsible for distributing update information
 * to all registered listeners
 */
public class UpdateMessageBroker
{
	private DataSubscriber[] _subscribers;
	private int _subscriberNum = 0;
	private static final int MAXIMUM_NUMBER_SUBSCRIBERS = 4;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public UpdateMessageBroker()
	{
		_subscribers = new DataSubscriber[MAXIMUM_NUMBER_SUBSCRIBERS];
	}


	/**
	 * Add a data subscriber to the list
	 * @param inSub DataSubscriber to add
	 */
	public void addSubscriber(DataSubscriber inSub)
	{
		_subscribers[_subscriberNum] = inSub;
		_subscriberNum++;
	}


	/**
	 * Send a message to all subscribers that
	 * the data has been updated
	 */
	public void informSubscribers()
	{
		informSubscribers(DataSubscriber.ALL);
	}


	/**
	 * Send message to all subscribers
	 * @param inChange Change that occurred
	 */
	public void informSubscribers(byte inChange)
	{
		for (int i=0; i<_subscribers.length; i++)
		{
			if (_subscribers[i] != null)
			{
				_subscribers[i].dataUpdated(inChange);
			}
		}
	}
}
