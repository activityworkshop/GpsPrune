package tim.prune.load.xml;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;

/**
 * Class for handling loading of Xml files, and passing the
 * loaded data back to the App object
 */
public class XmlFileLoader extends DefaultHandler implements Runnable
{
	private File _file = null;
	private App _app = null;
	private JFrame _parentFrame = null;
	private XmlHandler _handler = null;
	private String _unknownType = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of track load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public XmlFileLoader(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Open the selected file and show the GUI dialog to select load options
	 * @param inFile File to open
	 */
	public void openFile(File inFile)
	{
		_file = inFile;
		_handler = null;
		_unknownType = null;
		// start new thread in case xml parsing is time-consuming
		new Thread(this).start();
	}


	/**
	 * Run method, to parse the file
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		try
		{
			// Construct a SAXParser and use this as a default handler
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(_file, this);

			// Check whether handler was properly instantiated
			if (_handler == null)
			{
				// Wasn't either kml or gpx
				JOptionPane.showMessageDialog(_parentFrame,
					I18nManager.getText("error.load.unknownxml") + " " + _unknownType,
					I18nManager.getText("error.load.dialogtitle"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				// Pass information back to app
				_app.informDataLoaded(_handler.getFieldArray(), _handler.getDataArray(),
					Altitude.FORMAT_METRES, _file.getName());
			}
		}
		catch (Exception e)
		{
			// Show error dialog
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("error.load.othererror") + " " + e.getMessage(),
				I18nManager.getText("error.load.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
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
			if (qName.equals("kml")) {_handler = new KmlHandler();}
			else if (qName.equals("gpx")) {_handler = new GpxHandler();}
			else if (_unknownType == null && qName != null && !qName.equals(""))
			{
				_unknownType = qName;
			}
		}
		else
		{
			// Handler instantiated so pass tags on to it
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
}
