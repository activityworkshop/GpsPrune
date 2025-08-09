package tim.prune.save;

import java.awt.Image;

/** Interface for TileFetchers to give their results back to the Grouter */
public interface Grouter {
	/** Inform the grouter that an image has been loaded */
	void tileReady(Image inImage, int inXoffset, int inYoffset);
}
