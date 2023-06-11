package tim.prune.function.sew;

/**
 * Exception used to indicate that no sew was possible
 */
class NothingDoneException extends Exception
{
	final int numSegments;

	public NothingDoneException(int inNumSegments) {
		numSegments = inNumSegments;
	}
}
