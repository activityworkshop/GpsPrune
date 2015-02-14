package tim.prune.load;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.I18nManager;
import tim.prune.load.xml.XmlFileLoader;


/**
 * Generic FileLoader class to select a file
 * and pass handling on to appropriate loader
 */
public class FileLoader
{
	private JFileChooser _fileChooser = null;
	private JFrame _parentFrame;
	private TextFileLoader _textFileLoader = null;
	private XmlFileLoader _xmlFileLoader = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of track load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public FileLoader(App inApp, JFrame inParentFrame)
	{
		_parentFrame = inParentFrame;
		_textFileLoader = new TextFileLoader(inApp, inParentFrame);
		_xmlFileLoader = new XmlFileLoader(inApp, inParentFrame);
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
				else
				{
					// Use text loader for everything else
					_textFileLoader.openFile(file);
				}
			}
			else
			{
				// couldn't read file - show error message
				JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.load.noread"),
					I18nManager.getText("error.load.dialogtitle"), JOptionPane.ERROR_MESSAGE);
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
