package tim.prune.gui.map.tile;

import tim.prune.GpsPrune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Single worker instance processing definitions from the coordinator
 */
public class TileDownloader extends TileWorker
{
	public TileDownloader(Coordinator inParent) {
		super(inParent);
	}

	@Override
	protected TileBytes processTile(TileDef def)
	{
		URL tileUrl = def.getUrl();
		if (tileUrl == null) {
			return null;
		}
		InputStream in = null;
		TileBytes result = null;
		try
		{
			URLConnection conn = tileUrl.openConnection();
			conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
			in = conn.getInputStream();
			int len = conn.getContentLength();
			if (len > 0)
			{
				result = new TileBytes(len);
				int totalRead = 0;
				while (totalRead < len)
				{
					int numRead = in.read(result.data, totalRead, len-totalRead);
					totalRead += numRead;
				}

				// TODO: if this worked, maybe we just came back online?
			}
		}
		catch (IOException e)
		{
			System.err.println("IOE: " + e.getClass().getName() + " - " + e.getMessage());
			// TODO: if this didn't work, maybe we are now offline, or maybe we just need to block this URL to avoid retries?
		}
		finally {
			try {
				in.close();
			} catch (IOException | NullPointerException ignored) {}
		}
		return result;
	}
}
