package tim.prune.tips;

/**
 * Definition of a tip, including key and whether the tip
 * has already been shown or not.
 * This class is only visible within this package
 */
class TipDefinition
{
	/** Key of message to show when fired */
	private String _messageKey = null;
	/** Threshold of calls before tip is shown */
	private int _threshold = 0;
	/** Number of times this tip has been hit */
	private int _hitCount = 0;
	/** Flag whether tip is active or has already been shown */
	private boolean _active = true;

	/**
	 * Constructor
	 * @param inKey key for message to show
	 */
	TipDefinition(String inKey)
	{
		this(inKey, 0);
	}

	/**
	 * Constructor
	 * @param inKey message key
	 * @param inThreshold threshold
	 */
	TipDefinition(String inKey, int inThreshold)
	{
		_messageKey = inKey;
		_threshold  = inThreshold;
	}

	/**
	 * Hit this definition and check the threshold
	 * @return true if the message should be shown
	 */
	boolean shouldShowMessage()
	{
		if (_active)
		{
			boolean overThreshold = (_hitCount >= _threshold);
			if (!overThreshold) {
				_hitCount++;
			}
			else {
				_active = false; // only fire once
			}
			return overThreshold;
		}
		// not active
		return false;
	}

	/**
	 * @return message key
	 */
	String getMessageKey() {
		return _messageKey;
	}
}
