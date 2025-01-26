package tim.prune.load.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.SourceInfo;
import tim.prune.load.FileToBeLoaded;
import tim.prune.load.FileTypeLoader;
import tim.prune.load.MediaLinkInfo;

/**
 * Class for handling loading of Xml files, and passing the
 * loaded data back to the App object
 */
public class XmlFileLoader extends DefaultHandler implements Runnable
{
	private final App _app;
	private FileToBeLoaded _fileLock = null;
	private boolean _autoAppend = false;
	private XmlHandler _handler = null;
	private String _unknownType = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of track load
	 */
	public XmlFileLoader(App inApp) {
		_app = inApp;
	}

	/**
	 * Reset the handler to ensure data cleared
	 */
	public void reset()
	{
		_handler = null;
		_unknownType = null;
	}

	/**
	 * Open the selected file
	 * @param inFileLock File to open
	 * @param inAutoAppend true to auto-append
	 */
	public void openFile(FileToBeLoaded inFileLock, boolean inAutoAppend)
	{
		_fileLock = inFileLock;
		_fileLock.takeOwnership();	// we keep ownership for separate thread
		_autoAppend = inAutoAppend;
		reset();
		// start new thread in case xml parsing is time-consuming
		new Thread(this).start();
	}


	/**
	 * Run method, to parse the file
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		FileInputStream inStream = null;
		boolean success = false;
		try
		{
			inStream = new FileInputStream(_fileLock.getFile());
			success = parseXmlStream(inStream);
		}
		catch (FileNotFoundException fnfe) {}

		// Clean up the stream, don't need it any more
		if (inStream != null) {
			try {inStream.close();} catch (IOException e2) {}
		}

		if (success)
		{
			// Check whether handler was properly instantiated
			if (_handler == null)
			{
				// Wasn't either kml or gpx
				_app.showErrorMessageNoLookup("error.load.dialogtitle",
					I18nManager.getText("error.load.unknownxml") + " " + _unknownType);
			}
			else
			{
				SourceInfo sourceInfo = new SourceInfo(_fileLock.getFile(), _handler.getFileType(),
					_handler.getFileVersion());
				sourceInfo.setFileTitle(_handler.getFileTitle());
				sourceInfo.setFileDescription(_handler.getFileDescription());
				sourceInfo.setExtensionInfo(_handler.getExtensionInfo());

				// Pass information back to app
				new FileTypeLoader(_app).loadData(_handler, sourceInfo, _autoAppend,
					new MediaLinkInfo(_handler.getLinkArray()));
			}
		}
		_fileLock.release();
	}


	/**
	 * Try both Xerces and the built-in java classes to parse the given xml stream
	 * @param inStream input stream from file / zip / gzip
	 * @return true on success, false if both xerces and built-in parser failed
	 */
	public boolean parseXmlStream(InputStream inStream)
	{
		boolean success = false;
		// Firstly, try to use xerces to parse the xml (will throw an exception if not available)
		try
		{
			SAXParser saxParser = SAXParserFactory.newInstance("org.apache.xerces.parsers.SAXParser", null).newSAXParser();
			saxParser.parse(inStream, this);
			success = true; // worked
		}
		catch (Throwable e) {} // don't care too much if it didn't work, there's a backup

		// If that didn't work, try the built-in classes (which work for xml1.0 but handling for 1.1 contains bugs)
		if (!success)
		{
			try
			{
				// Construct a SAXParser and use this as a default handler
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				saxParser.parse(inStream, this);
				success = true;
			}
			catch (Exception e)
			{
				// Show error dialog
				_app.showErrorMessageNoLookup("error.load.dialogtitle",
					I18nManager.getText("error.load.othererror") + " " + e.getMessage());
			}
		}
		return success;
	}

	/**
	 * Receive a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException
	{
		// Check for "kml" or "gpx" tags
		if (_handler == null)
		{
			if (qName.equals("kml")) {
				_handler = new KmlHandler();
			}
			else if (qName.equals("gpx")) {
				_handler = new GpxHandler();
			}
			else if (_unknownType == null && !qName.equals("")) {
				_unknownType = qName;
			}
		}
		if (_handler != null) {
			_handler.startElement(uri, localName, qName, attributes);
		}
		super.startElement(uri, localName, qName, attributes);
	}


	/**
	 * Receive characters, either between or inside tags
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		if (_handler != null)
		{
			// Handler instantiated so pass tags on to it
			_handler.characters(ch, start, length);
		}
		super.characters(ch, start, length);
	}


	/**
	 * Receive end of element
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
		throws SAXException
	{
		if (_handler != null)
		{
			// Handler instantiated so pass tags on to it
			_handler.endElement(uri, localName, qName);
		}
		super.endElement(uri, localName, qName);
	}

	/**
	 * @return The Xml handler used for the parsing
	 */
	public XmlHandler getHandler() {
		return _handler;
	}
}
