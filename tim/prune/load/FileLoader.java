package tim.prune.load;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.ZipFileLoader;


/**
 * Generic FileLoader class to select a file
 * and pass handling on to appropriate loader
 */
public class FileLoader
{
	private App _app;
	private JFileChooser _fileChooser = null;
	private JFrame _parentFrame;
	private TextFileLoader _textFileLoader = null;
	private XmlFileLoader _xmlFileLoader = null;
	private ZipFileLoader _zipFileLoader = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of track load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public FileLoader(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
		_textFileLoader = new TextFileLoader(inApp, inParentFrame);
		_xmlFileLoader = new XmlFileLoader(inApp);
		_zipFileLoader = new ZipFileLoader(inApp, _xmlFileLoader);
	}


	/**
	 * Select an input file and open the GUI frame
	 * to select load options
	 */
	public void openFile()
	{
		// Construct file chooser if necessary
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.addChoosableFileFilter(new GenericFileFilter("filetype.txt", new String[] {"txt", "text"}));
			_fileChooser.addChoosableFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
			_fileChooser.addChoosableFileFilter(new GenericFileFilter("filetype.kml", new String[] {"kml"}));
			_fileChooser.addChoosableFileFilter(new GenericFileFilter("filetype.kmz", new String[] {"kmz"}));
			_fileChooser.setAcceptAllFileFilterUsed(true);
			// start from directory in config if already set (by load jpegs)
			File configDir = Config.getWorkingDirectory();
			if (configDir != null) {_fileChooser.setCurrentDirectory(configDir);}
		}
		// Show the open dialog
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			File file = _fileChooser.getSelectedFile();
			// Check file exists and is readable
			if (file != null && file.exists() && file.canRead())
			{
				// Store directory in config for later
				Config.setWorkingDirectory(file.getParentFile());
				// Check file type to see if it's xml or just normal text
				String fileExtension = file.getName().toLowerCase();
				if (fileExtension.length() > 4)
					{fileExtension = fileExtension.substring(fileExtension.length() - 4);}
				if (fileExtension.equals(".kml") || fileExtension.equals(".gpx")
					|| fileExtension.equals(".xml"))
				{
					// Use xml loader for kml, gpx and xml filenames
					_xmlFileLoader.openFile(file);
				}
				else if (fileExtension.equals(".kmz") || fileExtension.equals(".zip"))
				{
					// Use zip loader for zipped kml (or zipped gpx)
					_zipFileLoader.openFile(file);
				}
				else
				{
					// Use text loader for everything else
					_textFileLoader.openFile(file);
				}
			}
			else
			{
				// couldn't read file - show error message
				_app.showErrorMessage("error.load.dialogtitle", "error.load.noread");
			}
		}
	}

	/**
	 * @return the last delimiter character used for a text file load
	 */
	public char getLastUsedDelimiter()
	{
		return _textFileLoader.getLastUsedDelimiter();
	}
}
