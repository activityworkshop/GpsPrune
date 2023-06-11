package tim.prune;

import java.util.ArrayList;

/**
 * Class responsible for distributing update information
 * to all registered listeners
 */
public abstract class UpdateMessageBroker
{
	private static final ArrayList<DataSubscriber> _subscribers = new ArrayList<>();


	/**
	 * Add a data subscriber to the list
	 * @param inSub DataSubscriber to add
	 */
	public static void addSubscriber(DataSubscriber inSub)
	{
		if (inSub != null) {
			_subscribers.add(inSub);
		}
	}

	/**
	 * Remove the given subscriber from the list
	 * @param inSub subscriber to remove
	 */
	public static void removeSubscriber(DataSubscriber inSub) {
		_subscribers.remove(inSub);
	}

	/**
	 * Send a message to all subscribers that
	 * the data has been updated
	 */
	public static void informSubscribers() {
		informSubscribers(DataSubscriber.ALL_DATA);
	}


	/**
	 * Send message to all subscribers
	 * @param inChange Change that occurred
	 */
	public static void informSubscribers(int inChange)
	{
		// TODO: Launch separate thread so that whatever caused the inform can finish
		for (DataSubscriber subscriber : _subscribers) {
			subscriber.dataUpdated(inChange);
		}
	}

	/**
	 * Send message to all subscribers
	 * @param inMessage message to display informing of action completed
	 */
	public static void informSubscribers(String inMessage)
	{
		for (DataSubscriber subscriber : _subscribers) {
			subscriber.actionCompleted(inMessage);
		}
	}
}
