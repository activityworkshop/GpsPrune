package tim.prune.load;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Photo;
import tim.prune.data.PhotoStatus;
import tim.prune.data.Timestamp;
import tim.prune.drew.jpeg.ExifReader;
import tim.prune.drew.jpeg.JpegData;
import tim.prune.drew.jpeg.JpegException;
import tim.prune.drew.jpeg.Rational;

/**
 * Class to manage the loading of Jpegs and dealing with the GPS data from them
 */
public class JpegLoader implements Runnable
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private JFileChooser _fileChooser = null;
	private JCheckBox _subdirCheckbox = null;
	private JCheckBox _noExifCheckbox = null;
	private JDialog _progressDialog   = null;
	private JProgressBar _progressBar = null;
	private int[] _fileCounts = null;
	private boolean _cancelled = false;
	private ArrayList _photos = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of photo load
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public JpegLoader(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Select an input file and open the GUI frame
	 * to select load options
	 */
	public void openFile()
	{
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setMultiSelectionEnabled(true);
			_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			_fileChooser.setDialogTitle(I18nManager.getText("menu.file.addphotos"));
			_subdirCheckbox = new JCheckBox(I18nManager.getText("dialog.jpegload.subdirectories"));
			_subdirCheckbox.setSelected(true);
			_noExifCheckbox = new JCheckBox(I18nManager.getText("dialog.jpegload.loadjpegswithoutcoords"));
			_noExifCheckbox.setSelected(true);
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(_subdirCheckbox);
			panel.add(_noExifCheckbox);
			_fileChooser.setAccessory(panel);
		}
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			// Bring up dialog before starting
			showDialog();
			new Thread(this).start();
		}
	}


	/**
	 * Show the main dialog
	 */
	private void showDialog()
	{
		_progressDialog = new JDialog(_parentFrame, I18nManager.getText("dialog.jpegload.progress.title"));
		_progressDialog.setLocationRelativeTo(_parentFrame);
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		_progressBar.setString("");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.add(new JLabel(I18nManager.getText("dialog.jpegload.progress")));
		panel.add(_progressBar);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_cancelled = true;
			}
		});
		panel.add(cancelButton);
		_progressDialog.getContentPane().add(panel);
		_progressDialog.pack();
		_progressDialog.show();
	}


	/**
	 * Run method for performing tasks in separate thread
	 */
	public void run()
	{
		// Initialise arrays, errors, summaries
		_fileCounts = new int[4]; // files, jpegs, exifs, gps
		_photos = new ArrayList();
		File[] files = _fileChooser.getSelectedFiles();
		// Loop recursively over selected files/directories to count files
		int numFiles = countFileList(files, true, _subdirCheckbox.isSelected());
		// Set up the progress bar for this number of files
		_progressBar.setMaximum(numFiles);
		_progressBar.setValue(0);
		_cancelled = false;

		// Process the files recursively and build lists of photos
		processFileList(files, true, _subdirCheckbox.isSelected());
		_progressDialog.hide();
		if (_cancelled) {return;}

		//System.out.println("Finished - counts are: " + _fileCounts[0] + ", " + _fileCounts[1]
		//  + ", " + _fileCounts[2] + ", " + _fileCounts[3]);
		if (_fileCounts[0] == 0)
		{
			// No files found at all
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nofilesfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (_fileCounts[1] == 0)
		{
			// No jpegs found
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nojpegsfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (!_noExifCheckbox.isSelected() && _fileCounts[2] == 0)
		{
			// Need coordinates but no exif found
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.noexiffound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (!_noExifCheckbox.isSelected() && _fileCounts[3] == 0)
		{
			// Need coordinates but no gps information found
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nogpsfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			// Found some photos to load
			// TODO: Load jpeg information into dialog for confirmation?
			// Pass information back to app
			_app.informPhotosLoaded(_photos);
		}
	}


	/**
	 * Process a list of files and/or directories
	 * @param inFiles array of file/directories
	 * @param inFirstDir true if first directory
	 * @param inDescend true to descend to subdirectories
	 */
	private void processFileList(File[] inFiles, boolean inFirstDir, boolean inDescend)
	{
		if (inFiles != null)
		{
			// Loop over elements in array
			for (int i=0; i<inFiles.length; i++)
			{
				File file = inFiles[i];
				if (file.exists() && file.canRead())
				{
					// Check whether it's a file or a directory
					if (file.isFile())
					{
						processFile(file);
					}
					else if (file.isDirectory() && (inFirstDir || inDescend))
					{
						// Always process first directory,
						// only process subdirectories if checkbox selected
						File[] files = file.listFiles();
						processFileList(files, false, inDescend);
					}
				}
				else
				{
					// file doesn't exist or isn't readable - ignore error
				}
				// check for cancel button pressed
				if (_cancelled) break;
			}
		}
	}


	/**
	 * Process the given file, by attempting to extract its tags
	 * @param inFile file object to read
	 */
	private void processFile(File inFile)
	{
		// Update progress bar
		_fileCounts[0]++; // file found
		_progressBar.setValue(_fileCounts[0]);
		_progressBar.setString("" + _fileCounts[0] + " / " + _progressBar.getMaximum());
		_progressBar.repaint();

		// Check whether filename corresponds with accepted filenames
		if (!acceptPhotoFilename(inFile.getName())) {return;}

		// Create Photo object
		Photo photo = new Photo(inFile);
		// Try to get information out of exif
		try
		{
			JpegData jpegData = new ExifReader(inFile).extract();
			_fileCounts[1]++; // jpeg found (no exception thrown)
			if (jpegData.getExifDataPresent())
				{_fileCounts[2]++;} // exif found
			if (jpegData.isValid())
			{
				if (jpegData.getDatestamp() != null && jpegData.getTimestamp() != null)
				{
					photo.setTimestamp(createTimestamp(jpegData.getDatestamp(), jpegData.getTimestamp()));
				}
				// Make DataPoint and attach to Photo
				DataPoint point = createDataPoint(jpegData);
				point.setPhoto(photo);
				photo.setDataPoint(point);
				photo.setOriginalStatus(PhotoStatus.TAGGED);
				_fileCounts[3]++;
			}
		}
		catch (JpegException jpe) { // don't list errors, just count them
		}
		// Use file timestamp if exif timestamp isn't available
		if (photo.getTimestamp() == null)
		{
			photo.setTimestamp(new Timestamp(inFile.lastModified()));
			//System.out.println("No exif, using timestamp from file: " + inFile.lastModified() + " -> " + photo.getTimestamp().getText());
		}
		else
		{
			//System.out.println("timestamp from file = " + photo.getTimestamp().getText());
		}
		// Add the photo if it's got a point or if pointless photos should be added
		if (photo.getDataPoint() != null || _noExifCheckbox.isSelected())
		{
			_photos.add(photo);
		}
	}


	/**
	 * Recursively count the selected Files so we can draw a progress bar
	 * @param inFiles file list
	 * @param inFirstDir true if first directory
	 * @param inDescend true to descend to subdirectories
	 * @return count of the files selected
	 */
	private int countFileList(File[] inFiles, boolean inFirstDir, boolean inDescend)
	{
		int fileCount = 0;
		if (inFiles != null)
		{
			// Loop over elements in array
			for (int i=0; i<inFiles.length; i++)
			{
				File file = inFiles[i];
				if (file.exists() && file.canRead())
				{
					// Check whether it's a file or a directory
					if (file.isFile())
					{
						fileCount++;
					}
					else if (file.isDirectory() && (inFirstDir || inDescend))
					{
						fileCount += countFileList(file.listFiles(), false, inDescend);
					}
				}
			}
		}
		return fileCount;
	}


	/**
	 * Create a DataPoint object from the given jpeg data
	 * @param inData Jpeg data including coordinates
	 * @return DataPoint object for Track
	 */
	private static DataPoint createDataPoint(JpegData inData)
	{
		// Create model objects from jpeg data
		double latval = getCoordinateDoubleValue(inData.getLatitude(),
			inData.getLatitudeRef() == 'N' || inData.getLatitudeRef() == 'n');
		Latitude latitude = new Latitude(latval, Latitude.FORMAT_DEG_MIN_SEC);
		double lonval = getCoordinateDoubleValue(inData.getLongitude(),
			inData.getLongitudeRef() == 'E' || inData.getLongitudeRef() == 'e');
		Longitude longitude = new Longitude(lonval, Longitude.FORMAT_DEG_MIN_SEC);
		Altitude altitude = null;
		if (inData.getAltitude() != null)
		{
			altitude = new Altitude(inData.getAltitude().intValue(), Altitude.FORMAT_METRES);
		}
		return new DataPoint(latitude, longitude, altitude);
	}


	/**
	 * Convert an array of 3 Rational numbers into a double coordinate value
	 * @param inRationals array of three Rational objects
	 * @param isPositive true for positive hemisphere, for positive double value
	 * @return double value of coordinate, either positive or negative
	 */
	private static double getCoordinateDoubleValue(Rational[] inRationals, boolean isPositive)
	{
		if (inRationals == null || inRationals.length != 3) return 0.0;
		double value = inRationals[0].doubleValue()        // degrees
			+ inRationals[1].doubleValue() / 60.0          // minutes
			+ inRationals[2].doubleValue() / 60.0 / 60.0;  // seconds
		// make sure it's the correct sign
		value = Math.abs(value);
		if (!isPositive) value = -value;
		return value;
	}


	/**
	 * Use the given Rational values to create a timestamp
	 * @param inDate rationals describing date
	 * @param inTime rationals describing time
	 * @return Timestamp object corresponding to inputs
	 */
	private static Timestamp createTimestamp(Rational[] inDate, Rational[] inTime)
	{
		//System.out.println("Making timestamp for date (" + inDate[0].toString() + "," + inDate[1].toString() + "," + inDate[2].toString() + ") and time ("
		//	+ inTime[0].toString() + "," + inTime[1].toString() + "," + inTime[2].toString() + ")");
		return new Timestamp(inDate[0].intValue(), inDate[1].intValue(), inDate[2].intValue(),
			inTime[0].intValue(), inTime[1].intValue(), inTime[2].intValue());
	}


	/**
	 * Check whether to accept the given filename
	 * @param inName name of file
	 * @return true if accepted, false otherwise
	 */
	private static boolean acceptPhotoFilename(String inName)
	{
		if (inName != null && inName.length() > 4)
		{
			// Check for three-character file extensions jpg and jpe
			String lastFour = inName.substring(inName.length() - 4).toLowerCase();
			if (lastFour.equals(".jpg") || lastFour.equals(".jpe"))
			{
				return true;
			}
			// If not found, check for file extension jpeg
			if (inName.length() > 5)
			{
				String lastFive = inName.substring(inName.length() - 5).toLowerCase();
				if (lastFive.equals(".jpeg"))
				{
					return true;
				}
			}
		}
		// Not matched so don't accept
		return false;
	}
}
