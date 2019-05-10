package tim.prune.tips;

/**
 * Class to manage the showing of tips according
 * to the fixed TipDefinitions
 */
public abstract class TipManager
{
	public static final int Tip_UseAMapCache    = 0;
	public static final int Tip_LearnTimeParams = 1;
	public static final int Tip_DownloadSrtm    = 2;
	public static final int Tip_UseSrtmFor3d    = 3;
	public static final int Tip_ManuallyCorrelateOne = 4;
	private static final int Number_Tips = Tip_ManuallyCorrelateOne + 1;

	/** Array of tip definitions */
	private static TipDefinition[] TIPDEFS = new TipDefinition[Number_Tips];

	/** Static block to initialise tip definitions */
	static
	{
		TIPDEFS[Tip_UseAMapCache] = new TipDefinition("tip.useamapcache", 150);
		TIPDEFS[Tip_LearnTimeParams] = new TipDefinition("tip.learntimeparams");
		TIPDEFS[Tip_DownloadSrtm] = new TipDefinition("tip.downloadsrtm", 5);
		TIPDEFS[Tip_UseSrtmFor3d] = new TipDefinition("tip.usesrtmfor3d");
		TIPDEFS[Tip_ManuallyCorrelateOne] = new TipDefinition("tip.manuallycorrelateone");
	}

	/**
	 * Fire a trigger for the specified tip and get the message key if tip should be shown
	 * @param inTipNumber number of tip from constants
	 * @return message key if a message should be shown, or null otherwise
	 */
	public static String fireTipTrigger(int inTipNumber)
	{
		try {
			TipDefinition tip = TIPDEFS[inTipNumber];
			if (tip.shouldShowMessage()) {
				return tip.getMessageKey();
			}
		}
		catch (ArrayIndexOutOfBoundsException obe) {} // unrecognised tip given
		return null;
	}
}
