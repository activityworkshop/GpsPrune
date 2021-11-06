package tim.prune.load.xml;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tim.prune.App;
import tim.prune.data.SourceInfo;
import tim.prune.load.MediaLinkInfo;

/**
 * Class to handle the loading of zipped xml files
 */
public class ZipFileLoader
{
	/** App for callback of file loading */
	private App _app = null;
	/** Object to do the handling of the xml */
	private XmlFileLoader _xmlLoader = null;

	/**
	 * Constructor
	 * @param inApp App object
	 * @param inXmlLoader object to do the xml handling
	 */
	public ZipFileLoader(App inApp, XmlFileLoader inXmlLoader)
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
		try
		{
			ZipFile file = new ZipFile(inFile);
			Enumeration<?> entries = file.entries();
			boolean xmlFound = false;
			while (entries.hasMoreElements() && !xmlFound)
			{
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.toString();
				if (entryName != null && entryName.length() > 4)
				{
					String suffix = entryName.substring(entryName.length()-4).toLowerCase();
					if (suffix.equals(".kml") || suffix.equals(".gpx") || suffix.equals(".xml"))
					{
						_xmlLoader.reset();
						// Parse the stream using either Xerces or java classes
						_xmlLoader.parseXmlStream(file.getInputStream(entry));
						XmlHandler handler = _xmlLoader.getHandler();
						if (handler == null) {
							_app.showErrorMessage("error.load.dialogtitle", "error.load.othererror");
						}
						else
						{
							// Send back to app
							SourceInfo sourceInfo = new SourceInfo(inFile,
								(handler instanceof GpxHandler?SourceInfo.FILE_TYPE.GPX:SourceInfo.FILE_TYPE.KML));
							_app.informDataLoaded(handler.getFieldArray(), handler.getDataArray(),
								null, sourceInfo, handler.getTrackNameList(),
								new MediaLinkInfo(inFile, handler.getLinkArray()));
							xmlFound = true;
						}
					}
				}
			}
			file.close();
			// Check whether there was an xml file inside
			if (!xmlFound) {
				_app.showErrorMessage("error.load.dialogtitle", "error.load.noxmlinzip");
			}
		}
		catch (Exception e) {
			_app.showErrorMessageNoLookup("error.load.dialogtitle", e.getClass().getName() + "\n - " + e.getMessage());
		}
	}
}
