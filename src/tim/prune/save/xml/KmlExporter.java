package tim.prune.save.xml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.ColourUtils;
import tim.prune.config.Config;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FileInfo;
import tim.prune.data.FileType;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.DialogCloser;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;
import tim.prune.gui.colour.ColourChooser;
import tim.prune.gui.colour.ColourPatch;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.PointTypeSelector;
import tim.prune.save.VersionCombiner;

/**
 * Class to export the current track information
 * into a specified Kml or Kmz file
 */
public class KmlExporter extends GenericFunction
{
	private final TrackInfo _trackInfo;
	private final Track _track;
	private JDialog _dialog = null;
	private JComboBox<String> _titleField = null;
	private JComboBox<String> _versionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _altitudesCheckbox = null;
	private JCheckBox _kmzCheckbox = null;
	private JCheckBox _exportImagesCheckbox = null;
	private JLabel _imageSizeLabel = null;
	private WholeNumberField _imageSizeField = null;
	private ColourPatch _colourPatch = null;
	private KmlWriter _kmlWriter = null;
	private JLabel _progressLabel = null;
	private JProgressBar _progressBar = null;

	private JFileChooser _fileChooser = null;
	private File _exportFile = null;
	private JButton _okButton = null;
	private ColourChooser _colourChooser = null;

	// Filename of Kml file within zip archive
	private static final String KML_FILENAME_IN_KMZ = "doc.kml";
	// Default width and height of thumbnail images in Kmz
	private static final int DEFAULT_THUMBNAIL_WIDTH = 240;
	// Default track colour
	private static final Color DEFAULT_TRACK_COLOUR = new Color(204, 0, 0); // red


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public KmlExporter(App inApp)
	{
		super(inApp);
		_trackInfo = inApp.getTrackInfo();
		_track = _trackInfo.getTrack();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportkml";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window including whether to compress to kmz (and include pictures) or not
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_colourChooser = new ColourChooser(_dialog);
		}
		// Fill in image size from config
		_imageSizeField.setValue(getConfig().getConfigInt(Config.KEY_KMZ_IMAGE_SIZE));
		enableCheckboxes();
		populateTitles();
		_okButton.setEnabled(true);
		_progressLabel.setText("");
		_progressBar.setVisible(false);
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a central panel with the combos for title and version
		final String longStringToDefineWidth = "Long enough for a reasonable description";
		JPanel combosPanel = new JPanel();
		combosPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		double[] weights = new double[] {0.2, 0.8};
		boolean[] aligns = new boolean[] {true, false};
		GuiGridLayout comboGrid = new GuiGridLayout(combosPanel, weights, aligns);
		// Track title
		comboGrid.add(new JLabel(I18nManager.getText("dialog.exportkml.title")));
		_titleField = new JComboBox<>();
		_titleField.setPrototypeDisplayValue(longStringToDefineWidth);
		_titleField.setEditable(true);
		_titleField.addKeyListener(new DialogCloser(_dialog));
		comboGrid.add(_titleField);
		// file version
		comboGrid.add(new JLabel(I18nManager.getText("dialog.exportxml.version")));
		_versionField = new JComboBox<>();
		_versionField.addItem(I18nManager.getText("dialog.exportkml.version22"));
		_versionField.addItem(I18nManager.getText("dialog.exportkml.version23"));
		_versionField.setPrototypeDisplayValue(longStringToDefineWidth);
		_versionField.setEditable(false);
		comboGrid.add(_versionField);
		mainPanel.add(combosPanel);
		mainPanel.add(Box.createVerticalStrut(15));
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// point type selection
		_pointTypeSelector = new PointTypeSelector();
		_pointTypeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_pointTypeSelector);
		// Colour definition
		Color trackColour = ColourUtils.colourFromHex(getConfig().getConfigString(Config.KEY_KML_TRACK_COLOUR));
		if (trackColour == null) {
			trackColour = DEFAULT_TRACK_COLOUR;
		}
		_colourPatch = new ColourPatch(trackColour);
		_colourPatch.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				_colourChooser.showDialog(_colourPatch);
			}
		});
		JPanel colourPanel = new JPanel();
		colourPanel.add(new JLabel(I18nManager.getText("dialog.exportkml.trackcolour")));
		colourPanel.add(_colourPatch);
		mainPanel.add(colourPanel);
		// Checkbox for altitude export
		_altitudesCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.altitude"));
		_altitudesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_altitudesCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_altitudesCheckbox);

		// Checkboxes for kmz export and image export
		_kmzCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.kmz"));
		_kmzCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_kmzCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		// enable image checkbox if kmz activated
		_kmzCheckbox.addActionListener(e -> enableCheckboxes());
		mainPanel.add(_kmzCheckbox);
		_exportImagesCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.exportimages"));
		_exportImagesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_exportImagesCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		// enable image size fields if image checkbox changes
		_exportImagesCheckbox.addActionListener(e -> enableImageSizeFields());
		mainPanel.add(_exportImagesCheckbox);
		// Panel for the image size
		JPanel imageSizePanel = new JPanel();
		imageSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_imageSizeLabel = new JLabel(I18nManager.getText("dialog.exportkml.imagesize"));
		_imageSizeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		imageSizePanel.add(_imageSizeLabel);
		_imageSizeField = new WholeNumberField(4);
		imageSizePanel.add(_imageSizeField);
		mainPanel.add(imageSizePanel);

		mainPanel.add(Box.createVerticalStrut(10));
		_progressLabel = new JLabel("...");
		_progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_progressLabel);
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setVisible(false);
		_progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_progressBar);
		mainPanel.add(Box.createVerticalStrut(10));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> startExport();
		_okButton.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> {
			if (_kmlWriter != null) {
				_kmlWriter.cancelExport();
			}
			_dialog.dispose();
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Enable the checkboxes according to data
	 */
	private void enableCheckboxes()
	{
		_pointTypeSelector.init(_trackInfo);
		boolean hasAltitudes = _track.hasData(Field.ALTITUDE);
		if (!hasAltitudes) {_altitudesCheckbox.setSelected(false);}
		boolean hasPhotos = _trackInfo.getPhotoList().hasAny();
		_exportImagesCheckbox.setSelected(hasPhotos && _kmzCheckbox.isSelected());
		_exportImagesCheckbox.setEnabled(hasPhotos && _kmzCheckbox.isSelected());
		enableImageSizeFields();
	}

	/**
	 * Populate the titles in the combobox
	 */
	private void populateTitles()
	{
		final String initialTitle = getEnteredTitle();
		_titleField.removeAllItems();
		for (String value : _app.getTrackInfo().getFileInfo().getAllTitles()) {
			_titleField.addItem(value);
		}
		if (initialTitle.isEmpty() && _titleField.getItemCount() > 0) {
			_titleField.setSelectedIndex(0);
		}
		else {
			_titleField.setSelectedItem(initialTitle);
		}
		// Find best KML version and pre-select item from combo
		final FileInfo fileInfo = _app.getTrackInfo().getFileInfo();
		boolean isKml22 = shouldExportKml22(fileInfo);
		_versionField.setSelectedIndex(isKml22 ? 0 : 1);
	}

	/** @return true if Kml version 2.2 should be pre-selected in the combobox */
	private boolean shouldExportKml22(FileInfo inFileInfo)
	{
		VersionCombiner combiner = new VersionCombiner(List.of("2.2", "2.3"));
		for (String version : inFileInfo.getVersions(FileType.KML)) {
			combiner.addVersion(version);
		}
		return combiner.getBestVersion().equals("2.2");
	}

	/**
	 * Enable and disable the image size fields according to the checkboxes
	 */
	private void enableImageSizeFields()
	{
		boolean exportImages = _exportImagesCheckbox.isEnabled() && _exportImagesCheckbox.isSelected();
		_imageSizeField.setEnabled(exportImages);
		_imageSizeLabel.setEnabled(exportImages);
	}

	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, now validate selection checkboxes
		if (!_pointTypeSelector.getAnythingSelected()) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetypefilter.kmlkmz", new String[] {"kml", "kmz"}));
			// start from directory in config which should be set
			String configDir = getConfig().getConfigString(Config.KEY_TRACK_DIR);
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
		}
		final String requiredExtension, otherExtension;
		if (_kmzCheckbox.isSelected()) {
			requiredExtension = ".kmz"; otherExtension = ".kml";
		}
		else {
			requiredExtension = ".kml"; otherExtension = ".kmz";
		}
		_fileChooser.setAcceptAllFileFilterUsed(false);
		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = _fileChooser.getSelectedFile();
				if (file.getName().toLowerCase().endsWith(otherExtension))
				{
					String path = file.getAbsolutePath();
					file = new File(path.substring(0, path.length()-otherExtension.length()) + requiredExtension);
				}
				else if (!file.getName().toLowerCase().endsWith(requiredExtension))
				{
					file = new File(file.getAbsolutePath() + requiredExtension);
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// New file or overwrite confirmed, so initiate export in separate thread
					_exportFile = file;
					new Thread(this::run).start();
				}
				else
				{
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
	}


	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		// Disable ok button to stop second go
		_okButton.setEnabled(false);
		// Initialise progress indicators
		_progressLabel.setText(I18nManager.getText("confirm.running"));
		_progressBar.setVisible(true);
		_progressBar.setValue(0);
		boolean exportToKmz = _kmzCheckbox.isSelected();
		boolean exportImages = exportToKmz && _exportImagesCheckbox.isSelected();
		_progressBar.setMaximum(exportImages ? getNumPhotosToExport() : 1);

		TimeZone timezone = TimezoneHelper.getSelectedTimezone(getConfig());
		// Put all the selected options together into an object
		KmlExportOptions options = new KmlExportOptions()
			.setExportTrackPoints(_pointTypeSelector.getTrackpointsSelected())
			.setExportWaypoints(_pointTypeSelector.getWaypointsSelected())
			.setExportPhotos(_pointTypeSelector.getPhotopointsSelected())
			.setExportAudios(_pointTypeSelector.getAudiopointsSelected())
			.setExportJustSelection(_pointTypeSelector.getJustSelection())
			.setAbsoluteAltitudes(_altitudesCheckbox.isSelected())
			.setTitle(getEnteredTitle())
			.setTrackColour(_colourPatch.getBackground())
			.setTimezone(timezone);
		if (_versionField.getSelectedIndex() == 0) {
			_kmlWriter = new KmlWriter22(_trackInfo, options, v -> _progressBar.setValue(v));
		}
		else {
			_kmlWriter = new KmlWriter23(_trackInfo, options, v -> _progressBar.setValue(v));
		}
		OutputStreamWriter writer = null;
		ZipOutputStream zipOutputStream = null;
		try
		{
			// Select writer according to whether kmz requested or not
			if (!_kmzCheckbox.isSelected())
			{
				// normal writing to file
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			}
			else
			{
				// kmz requested - need zip output stream
				zipOutputStream = new ZipOutputStream(new FileOutputStream(_exportFile));
				// Export images into zip file too if requested
				if (exportImages)
				{
					// Get entered value for image size, store in config
					int thumbSize = _imageSizeField.getValue();
					if (thumbSize < DEFAULT_THUMBNAIL_WIDTH) {thumbSize = DEFAULT_THUMBNAIL_WIDTH;}
					getConfig().setConfigInt(Config.KEY_KMZ_IMAGE_SIZE, thumbSize);

					// Create thumbnails of each photo in turn and add to zip as images/image<n>.jpg
					// This is done first so that photo sizes are known for later
					_kmlWriter.exportThumbnails(zipOutputStream, thumbSize);
				}
				writer = new OutputStreamWriter(zipOutputStream);
				// Make an entry in the zip file for the kml file
				ZipEntry kmlEntry = new ZipEntry(KML_FILENAME_IN_KMZ);
				zipOutputStream.putNextEntry(kmlEntry);
			}
			// write file
			final int numPoints = _kmlWriter.exportData(writer, exportImages);
			// update config with selected track colour
			getConfig().setConfigString(Config.KEY_KML_TRACK_COLOUR, ColourUtils.makeHexCode(_colourPatch.getBackground()));
			// update progress bar
			_progressBar.setValue(1);

			// close zip entry if necessary
			if (zipOutputStream != null)
			{
				// Make sure all buffered data in writer is flushed
				writer.flush();
				// Close off this entry in the zip file
				zipOutputStream.closeEntry();
			}

			// close file
			writer.close();

			// Store directory in config for later
			getConfig().setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			_app.addRecentFile(_exportFile, true);
			// show confirmation
			UpdateMessageBroker.informSubscribers();
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + _exportFile.getAbsolutePath());
			// export successful so need to close dialog and return
			_dialog.dispose();
			return;
		}
		catch (IOException ioe)
		{
			try {
				if (writer != null) writer.close();
			}
			catch (IOException ioe2) {}
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("error.save.failed") + " : " + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		// if not returned already, export failed so need to recall the file selection
		startExport();
	}

	/** @return the string entered in the combo box for the track title */
	private String getEnteredTitle()
	{
		Object item = (_titleField == null ? null : _titleField.getSelectedItem());
		return (item == null ? "" : item.toString());
	}

	/**
	 * @return number of correlated photos in the track
	 */
	private int getNumPhotosToExport()
	{
		final int numPoints = _track.getNumPoints();
		int numPhotos = 0;
		// Loop over all points in track
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			if (point.getPhoto() != null) {
				numPhotos++;
			}
		}
		return numPhotos;
	}
}
