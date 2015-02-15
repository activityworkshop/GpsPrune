package tim.prune.function;

import tim.prune.App;
import tim.prune.FunctionLibrary;
import tim.prune.GenericFunction;

/**
 * Class to stop playing the current audio clip
 */
public class StopAudioFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public StopAudioFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.stopaudio";
	}

	/**
	 * Perform function
	 */
	public void begin()
	{
		PlayAudioFunction playFn = (PlayAudioFunction) FunctionLibrary.FUNCTION_PLAY_AUDIO;
		playFn.stopClip();
	}
}
