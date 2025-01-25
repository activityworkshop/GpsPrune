package tim.prune.load.xml;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tim.prune.App;
import tim.prune.data.SourceInfo;
import tim.prune.load.FileToBeLoaded;
import tim.prune.load.FileTypeLoader;
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
	 * @param inFileLock File to open
	 * @param inAutoAppend true to auto-append
	 */
	public void openFile(FileToBeLoaded inFileLock, boolean inAutoAppend)
	{
		try
		{
			ZipFile file = new ZipFile(inFileLock.getFile());
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
							SourceInfo sourceInfo = new SourceInfo(inFileLock.getFile(), handler.getFileType(),
								handler.getFileVersion());
							sourceInfo.setFileTitle(handler.getFileTitle());
							sourceInfo.setFileDescription(handler.getFileDescription());
							sourceInfo.setExtensionInfo(handler.getExtensionInfo());
							new FileTypeLoader(_app).loadData(handler, sourceInfo, inAutoAppend,
								new MediaLinkInfo(inFileLock.getFile(), handler.getLinkArray()));
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
