package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.FileInfo;
import tim.prune.data.FileType;
import tim.prune.data.TrackInfo;
import tim.prune.gui.DialogCloser;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.ProgressDialog;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.xml.GpxWriter;
import tim.prune.save.xml.GpxWriter10;
import tim.prune.save.xml.GpxWriter11;
import tim.prune.save.xml.XmlUtils;


/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter extends GenericFunction
{
	private final TrackInfo _trackInfo;
	private JDialog _dialog = null;
	private JComboBox<String> _titleField = null;
	private JComboBox<String> _descriptionField = null;
	private JComboBox<String> _versionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _timestampsCheckbox = null;
	private JCheckBox _descsToCommentsCheckbox = null;
	private JPanel _encodingsPanel = null;
	private JRadioButton _useSystemRadio = null, _forceUtf8Radio = null;
	private File _exportFile = null;
	private boolean _cancelled = false;
	private ProgressDialog _progress = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GpxExporter(App inApp)
	{
		super(inApp);
		_trackInfo = inApp.getTrackInfo();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportgpx";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_pointTypeSelector.init(_app.getTrackInfo());
		_encodingsPanel.setVisible(!XmlUtils.isSystemUtf8());
		if (!XmlUtils.isSystemUtf8())
		{
			String systemEncoding = XmlUtils.getSystemEncoding();
			_useSystemRadio.setText(I18nManager.getText("dialog.exportgpx.encoding.system")
				+ " (" + (systemEncoding == null ? "unknown" : systemEncoding) + ")");
		}
		populateComboboxes();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// Make a panel for the name/desc text boxes
		final String longStringToDefineWidth = "Long enough for a reasonable description";
		JPanel descPanel = new JPanel();
		double[] weights = new double[] {0.2, 0.8};
		boolean[] aligns = new boolean[] {true, false};
		GuiGridLayout comboGrid = new GuiGridLayout(descPanel, weights, aligns);
		comboGrid.add(new JLabel(I18nManager.getText("dialog.exportgpx.name")));
		_titleField = new JComboBox<>();
		_titleField.setPrototypeDisplayValue(longStringToDefineWidth);
		_titleField.setEditable(true);
		comboGrid.add(_titleField);
		comboGrid.add(new JLabel(I18nManager.getText("dialog.exportgpx.desc")));
		_descriptionField = new JComboBox<>();
		_descriptionField.setPrototypeDisplayValue(longStringToDefineWidth);
		_descriptionField.setEditable(true);
		comboGrid.add(_descriptionField);
		// file version
		comboGrid.add(new JLabel(I18nManager.getText("dialog.exportxml.version")));
		_versionField = new JComboBox<>();
		_versionField.setPrototypeDisplayValue(longStringToDefineWidth);
		_versionField.setEditable(false);
		comboGrid.add(_versionField);
		mainPanel.add(descPanel);
		mainPanel.add(Box.createVerticalStrut(15));
		// point type selection (track points, waypoints, photo points)
		_pointTypeSelector = new PointTypeSelector();
		mainPanel.add(_pointTypeSelector);
		mainPanel.add(Box.createVerticalStrut(15));
		// checkboxes for timestamps and copying
		JPanel checkPanel = new JPanel();
		weights = new double[] {0.5, 0.5};
		aligns = new boolean[] {false, false};
		GuiGridLayout checkGrid = new GuiGridLayout(checkPanel, weights, aligns);
		_timestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.includetimestamps"));
		_timestampsCheckbox.setSelected(true);
		checkGrid.add(_timestampsCheckbox);
		_descsToCommentsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.descriptionstocomments"));
		checkGrid.add(_descsToCommentsCheckbox, 2, true);
		mainPanel.add(checkPanel);
		// panel for selecting character encoding
		_encodingsPanel = new JPanel();
		if (!XmlUtils.isSystemUtf8())
		{
			// only add this panel if system isn't utf8 (or can't be identified yet)
			_encodingsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			_encodingsPanel.setLayout(new BorderLayout());
			_encodingsPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.encoding")), BorderLayout.NORTH);
			JPanel radioPanel = new JPanel();
			radioPanel.setLayout(new FlowLayout());
			ButtonGroup radioGroup = new ButtonGroup();
			_useSystemRadio = new JRadioButton(I18nManager.getText("dialog.exportgpx.encoding.system"));
			_forceUtf8Radio = new JRadioButton(I18nManager.getText("dialog.exportgpx.encoding.utf8"));
			radioGroup.add(_useSystemRadio);
			radioGroup.add(_forceUtf8Radio);
			radioPanel.add(_useSystemRadio);
			radioPanel.add(_forceUtf8Radio);
			_useSystemRadio.setSelected(true);
			_encodingsPanel.add(radioPanel, BorderLayout.CENTER);
			mainPanel.add(_encodingsPanel);
		}
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// close dialog if escape pressed
		_titleField.addKeyListener(new DialogCloser(_dialog));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> startExport();
		okButton.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Set the suggestions for the title, description and version with which to export
	 */
	private void populateComboboxes()
	{
		final FileInfo fileInfo = _app.getTrackInfo().getFileInfo();
		populateCombobox(_titleField, fileInfo.getAllTitles());
		populateCombobox(_descriptionField, fileInfo.getAllDescriptions());
		_versionField.removeAllItems();
		// Check whether any extensions are used or not
		final boolean hasExtensions = fileInfo.hasExtensions(FileType.GPX);
		_versionField.addItem(I18nManager.getText("dialog.exportgpx.version10" + (hasExtensions ? "withextensions" : "")));
		_versionField.addItem(I18nManager.getText("dialog.exportgpx.version11" + (hasExtensions ? "withextensions" : "")));
		// Find best GPX version and pre-select item from combo
		boolean isGpx10 = shouldExportGpx10(fileInfo);
		_versionField.setSelectedIndex(isGpx10 ? 0 : 1);
	}

	/** @return true if Gpx version 1.0 should be pre-selected in the combobox */
	private boolean shouldExportGpx10(FileInfo inFileInfo)
	{
		ArrayList<String> gpxVersions = new ArrayList<>();
		gpxVersions.add("1.0");
		gpxVersions.add("1.1");
		VersionCombiner combiner = new VersionCombiner(gpxVersions);
		for (String version : inFileInfo.getVersions(FileType.GPX)) {
			combiner.addVersion(version);
		}
		return combiner.getBestVersion().equals("1.0");
	}

	/** Add the specified values to the combobox */
	private static void populateCombobox(JComboBox<String> inCombobox, List<String> inValues)
	{
		final String initialValue = getEnteredText(inCombobox);
		inCombobox.removeAllItems();
		for (String value : inValues) {
			inCombobox.addItem(value);
		}
		if (initialValue.isEmpty() && inCombobox.getItemCount() > 0) {
			inCombobox.setSelectedIndex(0);
		}
		else {
			inCombobox.setSelectedItem(initialValue);
		}
	}

	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, so check selections
		if (!_pointTypeSelector.getAnythingSelected())
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		String configDir = getConfig().getConfigString(Config.KEY_TRACK_DIR);
		File saveFile = chooseGpxFile(_parentFrame, getEnteredName(), configDir);
		if (saveFile != null)
		{
			// New file or overwrite confirmed, so initiate export in separate thread
			_exportFile = saveFile;
			_cancelled = false;
			_progress = new ProgressDialog(_parentFrame, getNameKey(), null, () -> _cancelled = true);
			_progress.show();
			new Thread(this::run).start();
			_dialog.dispose();
		}
	}


	/**
	 * Select a GPX file to save to
	 * @param inParentFrame parent frame for file chooser dialog
	 * @param inTrackName track name entered earlier
	 * @param inDirectory directory to save to, or null if unknown
	 * @return selected File, or null if selection cancelled
	 */
	public static File chooseGpxFile(JFrame inParentFrame, String inTrackName, String inDirectory)
	{
		File saveFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new GenericFileFilter("filetypefilter.gpx", new String[] {"gpx"}));
		fileChooser.setAcceptAllFileFilterUsed(false);
		// start from directory in config which should be set
		if (inDirectory != null) {
			fileChooser.setCurrentDirectory(new File(inDirectory));
		}
		// Make suggestion of filename based on already selected track name
		final String suggestedFilename = makeFilenameFromTrackName(inTrackName);
		if (suggestedFilename != null) {
			fileChooser.setSelectedFile(new File(suggestedFilename));
		}

		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (fileChooser.showSaveDialog(inParentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = fileChooser.getSelectedFile();
				// Check file extension
				if (!file.getName().toLowerCase().endsWith(".gpx"))
				{
					file = new File(file.getAbsolutePath() + ".gpx");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(inParentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// new file or overwrite confirmed
					saveFile = file;
				}
				else
				{
					// file exists and overwrite cancelled - select again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
		return saveFile;
	}

	/**
	 * @param inTrackName track name entered by user
	 * @return suggested filename (without spaces), or null
	 */
	private static String makeFilenameFromTrackName(String inTrackName)
	{
		String filename = (inTrackName == null ? "" : inTrackName.trim());
		if (filename.equals("")) {
			return null;
		}
		filename = filename.replaceAll(" ", "_");
		if (filename.length() > 20) {
			filename = filename.substring(0, 20);
		}
		if (!filename.toLowerCase().endsWith(".gpx")) {
			filename = filename + ".gpx";
		}
		return filename;
	}


	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		OutputStreamWriter writer = null;
		// TODO: Try with resources
		try
		{
			// normal writing to file - firstly specify UTF8 encoding if requested
			if (_forceUtf8Radio != null && _forceUtf8Radio.isSelected()) {
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile), StandardCharsets.UTF_8);
			}
			else {
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			}
			SettingsForExport settings = new SettingsForExport();
			settings.setExportTrackPoints(_pointTypeSelector.getTrackpointsSelected());
			settings.setExportWaypoints(_pointTypeSelector.getWaypointsSelected());
			settings.setExportPhotoPoints(_pointTypeSelector.getPhotopointsSelected());
			settings.setExportAudiopoints(_pointTypeSelector.getAudiopointsSelected());
			settings.setExportJustSelection(_pointTypeSelector.getJustSelection());
			settings.setExportTimestamps(_timestampsCheckbox.isSelected());
			settings.setCopyDescriptionsToComments(_descsToCommentsCheckbox.isSelected());
			// write file
			final String name = getEnteredName();
			final String description = getEnteredDescription();
			final boolean useGpx10 = _versionField.getSelectedIndex() == 0;
			GpxWriter gpxWriter = useGpx10 ? new GpxWriter10(_progress, settings)
				: new GpxWriter11(_progress, settings);
			final int numPoints = gpxWriter.exportData(writer, _trackInfo, name, description, _app.getTrackInfo().getFileInfo());

			// close file
			writer.close();
			_progress.close();
			if (_cancelled) {
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.cancelled"));
				return;
			}
			// Store directory in config for later
			getConfig().setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// Add to recent file list
			_app.addRecentFile(_exportFile, true);
			// Show confirmation
			UpdateMessageBroker.informSubscribers();
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + _exportFile.getAbsolutePath());
			// export successful
			_app.informDataSaved();
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

	private String getEnteredName() {
		return getEnteredText(_titleField);
	}

	private String getEnteredDescription() {
		return getEnteredText(_descriptionField);
	}

	private static String getEnteredText(JComboBox<String> inCombo)
	{
		Object item = inCombo.getSelectedItem();
		return item == null ? "" : item.toString();
	}
}
