package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import tim.prune.ExternalTools;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;

/**
 * Class to call Exiftool to save coordinate information in jpg files
 */
public class ExifSaver implements Runnable
{
	private Frame _parentFrame = null;
	private JDialog _dialog = null;
	private JButton _okButton = null;
	private JCheckBox _overwriteCheckbox = null;
	private JCheckBox _forceCheckbox = null;
	private JProgressBar _progressBar = null;
	private PhotoTableModel _photoTableModel = null;
	private boolean _saveCancelled = false;


	// To preserve timestamps of file use parameter -P
	// To overwrite file (careful!) use parameter -overwrite_original_in_place

	// To read all GPS tags,   use -GPS:All
	// To delete all GPS tags, use -GPS:All=

	// To set Altitude, use -GPSAltitude= and -GPSAltitudeRef=
	// To set Latitude, use -GPSLatitude= and -GPSLatitudeRef=

	// To delete all tags with overwrite: exiftool -P -overwrite_original_in_place -GPS:All= <filename>

	// To set altitude with overwrite: exiftool -P -overwrite_original_in_place -GPSAltitude=1234 -GPSAltitudeRef='Above Sea Level' <filename>
	// (setting altitude ref to 0 doesn't work)
	// To set latitude with overwrite: exiftool -P -overwrite_original_in_place -GPSLatitude='12 34 56.78' -GPSLatitudeRef=N <filename>
	// (latitude as space-separated deg min sec, reference as either N or S)
	// Same for longitude, reference E or W


	/**
	 * Constructor
	 * @param inParentFrame parent frame
	 */
	public ExifSaver(Frame inParentFrame)
	{
		_parentFrame = inParentFrame;
	}


	/**
	 * Save exif information to all photos in the list
	 * whose coordinate information has changed since loading
	 * @param inPhotoList list of photos to save
	 * @return true if saved
	 */
	public boolean saveExifInformation(PhotoList inPhotoList)
	{
		// Check if external exif tool can be called
		boolean exifToolInstalled = ExternalTools.isToolInstalled(ExternalTools.TOOL_EXIFTOOL);
		if (!exifToolInstalled)
		{
			// show warning
			int answer = JOptionPane.showConfirmDialog(_dialog, I18nManager.getText("dialog.saveexif.noexiftool"),
				I18nManager.getText("dialog.saveexif.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION)
			{
				return false;
			}
		}
		// Make model and add all photos to it
		_photoTableModel = new PhotoTableModel(inPhotoList.getNumPhotos());
		for (int i=0; i<inPhotoList.getNumPhotos(); i++)
		{
			Photo photo = inPhotoList.getPhoto(i);
			PhotoTableEntry entry = new PhotoTableEntry(photo);
			_photoTableModel.addPhotoInfo(entry);
		}
		// Check if there are any modified photos to save
		if (_photoTableModel.getNumSaveablePhotos() < 1)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.saveexif.nothingtosave"),
				I18nManager.getText("dialog.saveexif.title"), JOptionPane.WARNING_MESSAGE);
			return false;
		}
		// Construct dialog
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.saveexif.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		// set progress bar and show dialog
		_progressBar.setVisible(false);
		_dialog.setVisible(true);
		return true;
	}


	/**
	 * Put together the dialog components for adding to the gui
	 * @return panel containing all gui components
	 */
	private JPanel makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.saveexif.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(8, 6, 5, 6));
		panel.add(topLabel, BorderLayout.NORTH);
		// centre panel with most controls
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BorderLayout());
		// table panel with table and checkbox
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		JTable photoTable = new JTable(_photoTableModel);
		JScrollPane scrollPane = new JScrollPane(photoTable);
		scrollPane.setPreferredSize(new Dimension(300, 160));
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		// Pair of checkboxes
		JPanel checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
		_overwriteCheckbox = new JCheckBox(I18nManager.getText("dialog.saveexif.overwrite"));
		_overwriteCheckbox.setSelected(false);
		checkPanel.add(_overwriteCheckbox);
		_forceCheckbox = new JCheckBox(I18nManager.getText("dialog.saveexif.force"));
		_forceCheckbox.setSelected(false);
		checkPanel.add(_forceCheckbox);
		tablePanel.add(checkPanel, BorderLayout.SOUTH);
		centrePanel.add(tablePanel, BorderLayout.CENTER);
		// progress bar below main controls
		_progressBar = new JProgressBar(0, 100);
		centrePanel.add(_progressBar, BorderLayout.SOUTH);
		panel.add(centrePanel, BorderLayout.CENTER);
		// Right-hand panel with select all, none buttons
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		JButton selectAllButton = new JButton(I18nManager.getText("button.selectall"));
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				selectPhotos(true);
			}
		});
		rightPanel.add(selectAllButton);
		JButton selectNoneButton = new JButton(I18nManager.getText("button.selectnone"));
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				selectPhotos(false);
			}
		});
		rightPanel.add(selectNoneButton);
		panel.add(rightPanel, BorderLayout.EAST);
		// Lower panel with ok and cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// disable ok button
				_okButton.setEnabled(false);
				// start new thread to do save
				new Thread(ExifSaver.this).start();
			}
		});
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_saveCancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}


	/**
	 * Select all or select none
	 * @param inSelected true to select all photos, false to deselect all
	 */
	private void selectPhotos(boolean inSelected)
	{
		int numPhotos = _photoTableModel.getRowCount();
		for (int i=0; i<numPhotos; i++)
		{
			_photoTableModel.getPhotoTableEntry(i).setSaveFlag(inSelected);
		}
		_photoTableModel.fireTableDataChanged();
	}


	/**
	 * Run method for saving in separate thread
	 */
	public void run()
	{
		_saveCancelled = false;
		PhotoTableEntry entry = null;
		Photo photo = null;
		int numPhotos = _photoTableModel.getRowCount();
		_progressBar.setMaximum(numPhotos);
		_progressBar.setValue(0);
		_progressBar.setVisible(true);
		boolean overwriteFlag = _overwriteCheckbox.isSelected();
		int numSaved = 0, numFailed = 0, numForced = 0;
		// Loop over all photos in list
		for (int i=0; i<numPhotos; i++)
		{
			entry = _photoTableModel.getPhotoTableEntry(i);
			if (entry != null && entry.getSaveFlag() && !_saveCancelled)
			{
				// Only look at photos which are selected and whose status has changed since load
				photo = entry.getPhoto();
				if (photo != null && photo.isModified())
				{
					// Increment counter if save successful
					if (savePhoto(photo, overwriteFlag, false)) {
						numSaved++;
					}
					else {
						if (_forceCheckbox.isSelected() && savePhoto(photo, overwriteFlag, true))
						{
							numForced++;
						}
						else {
							numFailed++;
						}
					}
				}
			}
			// update progress bar
			_progressBar.setValue(i + 1);
		}
		_progressBar.setVisible(false);
		// Show confirmation
		UpdateMessageBroker.informSubscribers(I18nManager.getTextWithNumber("confirm.saveexif.ok", numSaved));
		if (numFailed > 0)
		{
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getTextWithNumber("error.saveexif.failed", numFailed),
				I18nManager.getText("dialog.saveexif.title"), JOptionPane.ERROR_MESSAGE);
		}
		if (numForced > 0)
		{
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getTextWithNumber("error.saveexif.forced", numForced),
				I18nManager.getText("dialog.saveexif.title"), JOptionPane.WARNING_MESSAGE);
		}
		// close dialog, all finished
		_dialog.dispose();
	}


	/**
	 * Save the details for the given photo
	 * @param inPhoto Photo object
	 * @param inOverwriteFlag true to overwrite file, false otherwise
	 * @param inForceFlag true to force write, ignoring minor errors
	 * @return true if details saved ok
	 */
	private boolean savePhoto(Photo inPhoto, boolean inOverwriteFlag, boolean inForceFlag)
	{
		// If photos don't have a file, then can't save them
		if (inPhoto.getFile() == null) {
			return false;
		}
		// Check whether photo file still exists
		if (!inPhoto.getFile().exists())
		{
			// photo file doesn't exist any more
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("error.saveexif.filenotfound") + " : " + inPhoto.getFile().getAbsolutePath(),
				I18nManager.getText("dialog.saveexif.title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// Warn if file read-only and selected to overwrite
		if (inOverwriteFlag && !inPhoto.getFile().canWrite())
		{
			// eek, can't overwrite file
			int answer = JOptionPane.showConfirmDialog(_parentFrame,
				I18nManager.getText("error.saveexif.cannotoverwrite1") + " " + inPhoto.getFile().getAbsolutePath()
					+ " " + I18nManager.getText("error.saveexif.cannotoverwrite2"),
				I18nManager.getText("dialog.saveexif.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (answer == JOptionPane.YES_OPTION)
			{
				// don't overwrite this image but write to copy
				inOverwriteFlag = false;
			}
			else
			{
				// don't do anything with this file
				return false;
			}
		}
		String[] command = null;
		if (inPhoto.getCurrentStatus() == Photo.Status.NOT_CONNECTED)
		{
			// Photo is no longer connected, so delete gps tags
			command = getDeleteGpsExifTagsCommand(inPhoto.getFile(), inOverwriteFlag);
		}
		else
		{
			// Photo is now connected, so write new gps tags
			command = getWriteGpsExifTagsCommand(inPhoto.getFile(), inPhoto.getDataPoint(), inOverwriteFlag, inForceFlag);
		}
		// Execute exif command
		boolean saved = false;
		try
		{
			Process process = Runtime.getRuntime().exec(command);
			// Wait for process to finish so not too many run in parallel
			try {
				process.waitFor();
			}
			catch (InterruptedException ie) {}
			saved = (process.exitValue() == 0);
		}
		catch (Exception e)
		{
			// show error message
			JOptionPane.showMessageDialog(_parentFrame, "Exception: '" + e.getClass().getName() + "' : "
				+ e.getMessage(), I18nManager.getText("dialog.saveexif.title"), JOptionPane.ERROR_MESSAGE);
		}
		return saved;
	}


	/**
	 * Create the command to delete the gps exif tags from the specified file
	 * @param inFile file from which to delete tags
	 * @param inOverwrite true to overwrite file, false to create copy
	 * @return external command to delete gps tags
	 */
	private static String[] getDeleteGpsExifTagsCommand(File inFile, boolean inOverwrite)
	{
		// Make a string array to construct the command and its parameters
		String[] result = new String[inOverwrite?5:4];
		result[0] = Config.getConfigString(Config.KEY_EXIFTOOL_PATH);
		result[1] = "-P";
		if (inOverwrite) {result[2] = " -overwrite_original_in_place";}
		// remove all gps tags
		int paramOffset = inOverwrite?3:2;
		result[paramOffset] = "-GPS:All=";
		result[paramOffset + 1] = inFile.getAbsolutePath();
		return result;
	}


	/**
	 * Create the comand to write the gps exif tags to the specified file
	 * @param inFile file to which to write the tags
	 * @param inPoint DataPoint object containing coordinate information
	 * @param inOverwrite true to overwrite file, false to create copy
	 * @param inForce true to force write, ignoring minor errors
	 * @return external command to write gps tags
	 */
	private static String[] getWriteGpsExifTagsCommand(File inFile, DataPoint inPoint,
		boolean inOverwrite, boolean inForce)
	{
		// Make a string array to construct the command and its parameters
		String[] result = new String[(inOverwrite?10:9) + (inForce?1:0)];
		result[0] = Config.getConfigString(Config.KEY_EXIFTOOL_PATH);
		result[1] = "-P";
		if (inOverwrite) {result[2] = "-overwrite_original_in_place";}
		int paramOffset = inOverwrite?3:2;
		if (inForce) {
			result[paramOffset] = "-m";
			paramOffset++;
		}
		// To set latitude : -GPSLatitude='12 34 56.78' -GPSLatitudeRef='N'
		// (latitude as space-separated deg min sec, reference as either N or S)
		result[paramOffset] = "-GPSLatitude='" + inPoint.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC_WITH_SPACES)
		 + "'";
		result[paramOffset + 1] = "-GPSLatitudeRef=" + inPoint.getLatitude().output(Coordinate.FORMAT_CARDINAL);
		// same for longitude with space-separated deg min sec, reference as either E or W
		result[paramOffset + 2] = "-GPSLongitude='" + inPoint.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC_WITH_SPACES)
		 + "'";
		result[paramOffset + 3] = "-GPSLongitudeRef=" + inPoint.getLongitude().output(Coordinate.FORMAT_CARDINAL);
		// add altitude if it has it
		result[paramOffset + 4] = "-GPSAltitude="
		 + (inPoint.hasAltitude()?inPoint.getAltitude().getMetricValue():0);
		result[paramOffset + 5] = "-GPSAltitudeRef='Above Sea Level'";
		// add the filename to modify
		result[paramOffset + 6] = inFile.getAbsolutePath();
		return result;
	}
}
