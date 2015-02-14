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
			_subdirCheckbox = new JCheckBox(I18nManager.getText("dialog.jpegload.subdirectories"));
			_subdirCheckbox.setSelected(true);
			_fileChooser.setAccessory(_subdirCheckbox);
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
		// Loop over selected files/directories
		File[] files = _fileChooser.getSelectedFiles();
		int numFiles = countFileList(files, true, _subdirCheckbox.isSelected());
		// if (false) System.out.println("Found " + numFiles + " files");
		_progressBar.setMaximum(numFiles);
		_progressBar.setValue(0);
		_cancelled = false;
		processFileList(files, true, _subdirCheckbox.isSelected());
		_progressDialog.hide();
		if (_cancelled) return;
		// System.out.println("Finished - counts are: " + _fileCounts[0] + ", " + _fileCounts[1] + ", " + _fileCounts[2] + ", " + _fileCounts[3]);
		if (_fileCounts[0] == 0)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nofilesfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (_fileCounts[1] == 0)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nojpegsfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (_fileCounts[2] == 0)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.noexiffound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else if (_fileCounts[3] == 0)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.jpegload.nogpsfound"),
				I18nManager.getText("error.jpegload.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			// Load information into dialog for confirmation
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
						processDirectory(file, inDescend);
					}
				}
				else
				{
					// file doesn't exist or isn't readable - record error
				}
				// check for cancel
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
		_fileCounts[0]++; // file found
		_progressBar.setValue(_fileCounts[0]);
		_progressBar.setString("" + _fileCounts[0] + " / " + _progressBar.getMaximum());
		_progressBar.repaint();
		try
		{
			JpegData jpegData = new ExifReader(inFile).extract();
			_fileCounts[1]++; // jpeg found (no exception thrown)
//			if (jpegData.getNumErrors() > 0)
//				System.out.println("Number of errors was: " + jpegData.getNumErrors() + ": " + jpegData.getErrors().get(0));
			if (jpegData.getExifDataPresent())
				_fileCounts[2]++; // exif found
			if (jpegData.isValid())
			{
//				if (false && jpegData.getTimestamp() != null)
//					System.out.println("Timestamp is " + jpegData.getTimestamp()[0].toString() + ":" + jpegData.getTimestamp()[1].toString() + ":" + jpegData.getTimestamp()[2].toString());
//				if (false && jpegData.getDatestamp() != null)
//					System.out.println("Datestamp is " + jpegData.getDatestamp()[0].toString() + ":" + jpegData.getDatestamp()[1].toString() + ":" + jpegData.getDatestamp()[2].toString());
				// Make DataPoint and Photo
				DataPoint point = createDataPoint(jpegData);
				Photo photo = new Photo(inFile);
				point.setPhoto(photo);
				photo.setDataPoint(point);
				_photos.add(photo);
//				System.out.println("Made photo: " + photo.getFile().getAbsolutePath() + " with the datapoint: "
//					+ point.getLatitude().output(Latitude.FORMAT_DEG_MIN_SEC) + ", "
//					+ point.getLongitude().output(Longitude.FORMAT_DEG_MIN_SEC) + ", "
//					+ point.getAltitude().getValue(Altitude.FORMAT_METRES));
				_fileCounts[3]++;
			}
		}
		catch (JpegException jpe) { // don't list errors, just count them
		}
	}


	/**
	 * Process the given directory, by looping over its contents
	 * and recursively through its subdirectories
	 * @param inDirectory directory to read
	 * @param inDescend true to descend subdirectories
	 */
	private void processDirectory(File inDirectory, boolean inDescend)
	{
		File[] files = inDirectory.listFiles();
		processFileList(files, false, inDescend);
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
		Latitude latitude = new Latitude(latval, Latitude.FORMAT_NONE);
		double lonval = getCoordinateDoubleValue(inData.getLongitude(),
			inData.getLongitudeRef() == 'E' || inData.getLongitudeRef() == 'e');
		Longitude longitude = new Longitude(lonval, Longitude.FORMAT_NONE);
		Altitude altitude = new Altitude(inData.getAltitude().intValue(), Altitude.FORMAT_METRES);
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
}
