package tim.prune.function.cache;

import tim.prune.load.GenericFileFilter;

/**
 * File filter for map tiles
 */
public class TileFilter extends GenericFileFilter
{
	/** Constructor */
	public TileFilter()
	{
		super("filetypefilter.jpeg", new String[] {"jpg", "png", "gif", "temp"});
	}
}
