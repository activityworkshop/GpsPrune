package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.SourceInfo;
import tim.prune.data.TrackInfo;
import tim.prune.gui.DialogCloser;
import tim.prune.gui.ProgressDialog;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.xml.GpxCacherList;
import tim.prune.save.xml.XmlUtils;


/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter extends GenericFunction
{
	private final TrackInfo _trackInfo;
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _timestampsCheckbox = null;
	private JCheckBox _copySourceCheckbox = null;
	private JPanel _encodingsPanel = null;
	private JRadioButton _useSystemRadio = null, _forceUtf8Radio = null;
	private File _exportFile = null;
	private boolean _cancelled = false;
	private ProgressDialog _progress = null;
	/** Remember the previous sourceInfo object to tell whether it has changed */
	private SourceInfo _previousSourceInfo = null;


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
		setFileTitle();
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
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(2, 2));
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.name")));
		_nameField = new JTextField(10);
		descPanel.add(_nameField);
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.desc")));
		_descriptionField = new JTextField(10);
		descPanel.add(_descriptionField);
		mainPanel.add(descPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		// point type selection (track points, waypoints, photo points)
		_pointTypeSelector = new PointTypeSelector();
		mainPanel.add(_pointTypeSelector);
		// checkboxes for timestamps and copying
		JPanel checkPanel = new JPanel();
		_timestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.includetimestamps"));
		_timestampsCheckbox.setSelected(true);
		checkPanel.add(_timestampsCheckbox);
		_copySourceCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.copysource"));
		_copySourceCheckbox.setSelected(true);
		checkPanel.add(_copySourceCheckbox);
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
		_nameField.addKeyListener(new DialogCloser(_dialog));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> startExport();
		okButton.addActionListener(okListener);
		_descriptionField.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Set the suggestions for the name and title with which to export
	 */
	private void setFileTitle()
	{
		// Get the most recent file info
		SourceInfo currentSource = _app.getTrackInfo().getFileInfo().getFirstSource();
		if (currentSource != null && currentSource != _previousSourceInfo)
		{
			String lastTitle = currentSource.getFileTitle();
			if (lastTitle != null && !lastTitle.equals(""))
			{
				// Take the title of the last file loaded
				_nameField.setText(lastTitle);
			}
		}
		if (_nameField.getText().equals(""))
		{
			// no name given in the field already, so try to overwrite it
			String lastTitle = _app.getTrackInfo().getFileInfo().getFirstTitle();
			if (lastTitle != null && !lastTitle.equals(""))
			{
				_nameField.setText(lastTitle);
			}
		}
		// Remember this source info so we don't use it again
		_previousSourceInfo = currentSource;
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
		File saveFile = chooseGpxFile(_parentFrame, _nameField.getText());
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
	 * @return selected File, or null if selection cancelled
	 */
	public static File chooseGpxFile(JFrame inParentFrame, String inTrackName)
	{
		File saveFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
		fileChooser.setAcceptAllFileFilterUsed(false);
		// start from directory in config which should be set
		String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
		if (configDir != null) {
			fileChooser.setCurrentDirectory(new File(configDir));
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
		// Instantiate source file cachers in case we want to copy output
		GpxCacherList gpxCachers = null;
		if (_copySourceCheckbox.isSelected()) {
			gpxCachers = new GpxCacherList(_trackInfo.getFileInfo());
		}
		OutputStreamWriter writer = null;
		try
		{
			// normal writing to file - firstly specify UTF8 encoding if requested
			if (_forceUtf8Radio != null && _forceUtf8Radio.isSelected()) {
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile), "UTF-8");
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
			// write file
			final int numPoints = new GpxWriter(_progress, settings).exportData(writer, _trackInfo, _nameField.getText(),
				_descriptionField.getText(), gpxCachers);

			// close file
			writer.close();
			_progress.close();
			if (_cancelled) {
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.cancelled"));
				return;
			}
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
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
			// System.out.println("Exception: " + ioe.getClass().getName() + " - " + ioe.getMessage());
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
}
