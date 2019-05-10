package tim.prune.load.xml;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.SourceInfo;
import tim.prune.load.MediaLinkInfo;

/**
 * Class to handle the loading of gzipped xml files
 */
public class GzipFileLoader
{
	/** App for callback of file loading */
	private App _app = null;
	/** Object to do the handling of the xml */
	XmlFileLoader _xmlLoader = null;

	/**
	 * Constructor
	 * @param inApp App object
	 * @param inXmlLoader object to do the xml handling
	 */
	public GzipFileLoader(App inApp, XmlFileLoader inXmlLoader)
	{
		_app = inApp;
		_xmlLoader = inXmlLoader;
	}

	/**
	 * Open the selected file and select appropriate xml loader
	 * @param inFile File to open
	 */
	public void openFile(File inFile)
	{
		GZIPInputStream istream = null;
		try
		{
			istream = new GZIPInputStream(new FileInputStream(inFile));
			_xmlLoader.reset();
			// Parse the stream using either Xerces or java classes
			_xmlLoader.parseXmlStream(istream);
			XmlHandler handler = _xmlLoader.getHandler();
			if (handler == null) {
				_app.showErrorMessage("error.load.dialogtitle", "error.load.noread");
			}
			else
			{
				// Send back to app
				SourceInfo sourceInfo = new SourceInfo(inFile,
					(handler instanceof GpxHandler?SourceInfo.FILE_TYPE.GPX:SourceInfo.FILE_TYPE.KML));
				_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(),
					null, sourceInfo, handler.getTrackNameList(),
					new MediaLinkInfo(inFile, handler.getLinkArray()));
			}
		}
		catch (Exception e)
		{
			// Error occurred, could be a non-xml file borking the parser
			_app.showErrorMessageNoLookup("error.load.dialogtitle",
				I18nManager.getText("error.load.othererror") + " " + e.getClass().getName());
			// It would be nice to verify the filename of the file inside the gz,
			// but the java classes don't give access to this information
		}
		finally {
			try {
				istream.close();
			}
			catch (Exception e2) {}
		}
	}
}
