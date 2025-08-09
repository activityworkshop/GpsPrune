package tim.prune.function.compress.methods;

import tim.prune.data.MarkingData;
import tim.prune.data.SpeedCalculator;


public class CompressionSpeedCalculator extends SpeedCalculator
{
	private final MarkingData _markings;

	CompressionSpeedCalculator(MarkingData inMarkings) {
		_markings = inMarkings;
	}

	protected boolean shouldIgnorePoint(int inIndex) {
		return _markings != null && _markings.isPointMarkedForDeletion(inIndex);
	}
}
