package tim.prune.load;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import tim.prune.App;
import tim.prune.config.Config;
import tim.prune.data.Photo;
import tim.prune.load.xml.GzipFileLoader;
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
	private NmeaFileLoader _nmeaFileLoader = null;
	private XmlFileLoader _xmlFileLoader = null;
	private ZipFileLoader _zipFileLoader = null;
	private GzipFileLoader _gzipFileLoader = null;


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
		_nmeaFileLoader = new NmeaFileLoader(inApp);
		_xmlFileLoader = new XmlFileLoader(inApp);
		_zipFileLoader = new ZipFileLoader(inApp, _xmlFileLoader);
		_gzipFileLoader = new GzipFileLoader(inApp, _xmlFileLoader);
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
			_fileChooser.setFileFilter(_fileChooser.getAcceptAllFileFilter()); // For some reason seems necessary
			// start from directory in config if already set (by load jpegs)
			String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
			if (configDir == null) {configDir = Config.getConfigString(Config.KEY_PHOTO_DIR);}
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
			_fileChooser.setMultiSelectionEnabled(true); // Allow multiple selections
		}
		// Show the open dialog
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			File[] files = _fileChooser.getSelectedFiles();
			// Loop through files looking for files which exist and are readable
			ArrayList<File> dataFiles = new ArrayList<File>();
			if (files != null)
			{
				for (int i=0; i<files.length; i++)
				{
					File file = files[i];
					if (file.exists() && file.isFile() && file.canRead())
					{
						dataFiles.add(file);
					}
				}
			}
			if (dataFiles.size() > 0) {
				_app.loadDataFiles(dataFiles);
			}
			else
			{
				// couldn't find any files to load - show error message
				_app.showErrorMessage("error.load.dialogtitle", "error.load.noread");
			}
		}
	}

	/**
	 * Open the selected input file
	 * @param inFile file to open
	 */
	public void openFile(File inFile)
	{
		// Store directory in config for later
		File parent = inFile.getParentFile();
		if (parent != null) {
			Config.setConfigString(Config.KEY_TRACK_DIR, parent.getAbsolutePath());
		}
		// Check file type to see if it's xml or just normal text
		String fileExtension = inFile.getName().toLowerCase();
		if (fileExtension.length() > 4) {
			fileExtension = fileExtension.substring(fileExtension.length() - 4);
		}
		if (fileExtension.equals(".kml") || fileExtension.equals(".gpx")
			|| fileExtension.equals(".xml"))
		{
			// Use xml loader for kml, gpx and xml filenames
			_xmlFileLoader.openFile(inFile);
		}
		else if (fileExtension.equals(".kmz") || fileExtension.equals(".zip"))
		{
			// Use zip loader for zipped kml (or zipped gpx)
			_zipFileLoader.openFile(inFile);
		}
		else if (fileExtension.endsWith(".gz") || fileExtension.equals("gzip"))
		{
			// Use gzip loader for gzipped xml
			_gzipFileLoader.openFile(inFile);
		}
		else if (fileExtension.equals("nmea"))
		{
			_nmeaFileLoader.openFile(inFile);
		}
		else if (fileExtension.equals(".jpg") || fileExtension.equals("jpeg"))
		{
			Photo photo = JpegLoader.createPhoto(inFile);
			TreeSet<Photo> photoSet = new TreeSet<Photo>(new MediaSorter());
			photoSet.add(photo);
			_app.informPhotosLoaded(photoSet);
			_app.informNoDataLoaded(); // To trigger load of next file if any
		}
		else
		{
			// Use text loader for everything else
			_textFileLoader.openFile(inFile);
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
