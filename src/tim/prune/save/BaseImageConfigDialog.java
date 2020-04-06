package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Track;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.threedee.ImageDefinition;

/**
 * Dialog to let you choose the parameters for a base image
 * (source and zoom) including preview
 */
public class BaseImageConfigDialog implements Runnable
{
	/** Parent to notify */
	private BaseImageConsumer _parent = null;
	/** Parent dialog for position */
	private JDialog _parentDialog = null;
	/** Track to use for preview image */
	private Track _track = null;
	/** Dialog to show */
	private JDialog _dialog = null;
	/** Checkbox for using an image or not */
	private JCheckBox _useImageCheckbox = null;
	/** Panel to hold the other controls */
	private JPanel _mainPanel = null;
	/** Dropdown for map source */
	private JComboBox<String> _mapSourceDropdown = null;
	/** Dropdown for zoom levels */
	private JComboBox<String> _zoomDropdown = null;
	/** Button to trigger a download of the missing map tiles */
	private JButton _downloadTilesButton = null;
	/** Progress bar for downloading additional tiles */
	private JProgressBar _progressBar = null;
	/** Label for number of tiles found */
	private JLabel _tilesFoundLabel = null;
	/** Label for image size in pixels */
	private JLabel _imageSizeLabel = null;
	/** Image preview panel */
	private ImagePreviewPanel _previewPanel = null;
	/** Grouter, used to avoid regenerating images */
	private MapGrouter _grouter = new MapGrouter();
	/** OK button, needs to be enabled/disabled */
	private JButton _okButton = null;
	/** Flag for rebuilding dialog, don't bother refreshing and recalculating */
	private boolean _rebuilding = false;
	/** Cached values to allow cancellation of dialog */
	private ImageDefinition _imageDef = new ImageDefinition();


	/**
	 * Constructor
	 * @param inParent parent object to notify on completion of dialog
	 * @param inParentDialog parent dialog
	 * @param inTrack track object
	 */
	public BaseImageConfigDialog(BaseImageConsumer inParent, JDialog inParentDialog, Track inTrack)
	{
		_parent = inParent;
		_parentDialog = inParentDialog;
		_dialog = new JDialog(inParentDialog, I18nManager.getText("dialog.baseimage.title"), true);
		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		_track = inTrack;
	}

	/**
	 * @param inDefinition image definition object from previous dialog
	 */
	public void setImageDefinition(ImageDefinition inDefinition)
	{
		_imageDef = inDefinition;
		if (_imageDef == null) {
			_imageDef = new ImageDefinition();
		}
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		initDialog();
		_dialog.setLocationRelativeTo(_parentDialog);
		_dialog.setVisible(true);
	}

	/**
	 * Begin the function with a default of using an image
	 */
	public void beginWithImageYes()
	{
		initDialog();
		_useImageCheckbox.setSelected(true);
		refreshDialog();
		_dialog.setVisible(true);
	}

	/**
	 * Initialise the dialog from the cached values
	 */
	private void initDialog()
	{
		_rebuilding = true;
		_useImageCheckbox.setSelected(_imageDef.getUseImage());
		// Populate the dropdown of map sources from the library in case it has changed
		_mapSourceDropdown.removeAllItems();
		for (int i=0; i<MapSourceLibrary.getNumSources(); i++)
		{
			_mapSourceDropdown.addItem(MapSourceLibrary.getSource(i).getName());
		}
		int sourceIndex = _imageDef.getSourceIndex();
		if (sourceIndex < 0 || sourceIndex >= _mapSourceDropdown.getItemCount()) {
			sourceIndex = 0;
		}
		_mapSourceDropdown.setSelectedIndex(sourceIndex);

		// Zoom level
		int zoomLevel = _imageDef.getZoom();
		if (_imageDef.getUseImage())
		{
			for (int i=0; i<_zoomDropdown.getItemCount(); i++)
			{
				String item = _zoomDropdown.getItemAt(i);
				try {
					if (Integer.parseInt(item) == zoomLevel)
					{
						_zoomDropdown.setSelectedIndex(i);
						break;
					}
				}
				catch (NumberFormatException nfe) {}
			}
		}
		_rebuilding = false;
		refreshDialog();
	}

	/**
	 * Update the visibility of the controls, and update the zoom dropdown based on the selected map source
	 */
	private void refreshDialog()
	{
		_mainPanel.setVisible(_useImageCheckbox.isSelected());
		// Exit if we're in the middle of something
		if (_rebuilding) {return;}
		int currentZoom = 0;
		try {
			currentZoom = Integer.parseInt(_zoomDropdown.getSelectedItem().toString());
		}
		catch (Exception nfe) {}
		// First time in, the dropdown might be empty but we still might have a zoom in the definition
		if (_zoomDropdown.getItemCount() == 0) {
			currentZoom = _imageDef.getZoom();
		}
		// Get the extent of the track so we can work out how big the images are going to be for each zoom level
		final double xyExtent = Math.max(_track.getXRange().getRange(), _track.getYRange().getRange());
		int zoomToSelect = -1;

		_rebuilding = true;
		_zoomDropdown.removeAllItems();
		if (_useImageCheckbox.isSelected() && _mapSourceDropdown.getItemCount() > 0)
		{
			int currentSource = _mapSourceDropdown.getSelectedIndex();
			for (int i=5; i<18; i++)
			{
				// How many pixels does this give?
				final int zoomFactor = 1 << i;
				final int pixCount = (int) (xyExtent * zoomFactor * 256);
				if (pixCount > 100      // less than this isn't worth it
					&& pixCount < 4000  // don't want to run out of memory
					&& isZoomAvailable(i, MapSourceLibrary.getSource(currentSource)))
				{
					_zoomDropdown.addItem("" + i);
					if (i == currentZoom) {
						zoomToSelect = _zoomDropdown.getItemCount() - 1;
					}
				}
				// else System.out.println("Not using zoom " + i + " because pixCount=" + pixCount + " and xyExtent=" + xyExtent);
			}
		}
		_zoomDropdown.setSelectedIndex(zoomToSelect);
		_rebuilding = false;

		_okButton.setEnabled(!_useImageCheckbox.isSelected() ||
			(_zoomDropdown.getItemCount() > 0 && _zoomDropdown.getSelectedIndex() >= 0));
		updateImagePreview();
	}

	/**
	 * @return true if it should be possible to use an image, false if no disk cache or cache empty
	 */
	public static boolean isImagePossible()
	{
		String path = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (path != null && !path.equals(""))
		{
			File cacheDir = new File(path);
			if (cacheDir.exists() && cacheDir.isDirectory())
			{
				// Check if there are any directories in the cache
				for (File subdir : cacheDir.listFiles())
				{
					if (subdir.exists() && subdir.isDirectory()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * See if the requested zoom level is available
	 * @param inZoom zoom level
	 * @param inSource selected map source
	 * @return true if there is a zoom directory for each of the source's layers
	 */
	private static boolean isZoomAvailable(int inZoom, MapSource inSource)
	{
		if (inSource == null) {return false;}
		String path = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (path == null || path.equals("")) {
			return false;
		}
		File cacheDir = new File(path);
		if (!cacheDir.exists() || !cacheDir.isDirectory()) {
			return false;
		}
		// First layer
		File layer0 = new File(cacheDir, inSource.getSiteName(0) + inZoom);
		if (!layer0.exists() || !layer0.isDirectory() || !layer0.canRead()) {
			return false;
		}
		// Second layer, if any
		if (inSource.getNumLayers() > 1)
		{
			File layer1 = new File(cacheDir, inSource.getSiteName(1) + inZoom);
			if (!layer1.exists() || !layer1.isDirectory() || !layer1.canRead()) {
				return false;
			}
		}
		// must be ok
		return true;
	}


	/**
	 * @return image definition object
	 */
	public ImageDefinition getImageDefinition() {
		return _imageDef;
	}

	/**
	 * Make the dialog components to select the options
	 * @return Component holding gui elements
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		_useImageCheckbox = new JCheckBox(I18nManager.getText("dialog.baseimage.useimage"));
		_useImageCheckbox.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
		_useImageCheckbox.setHorizontalAlignment(JLabel.CENTER);
		_useImageCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshDialog();
			}
		});
		panel.add(_useImageCheckbox, BorderLayout.NORTH);

		// Outer panel with the grid and the map preview
		_mainPanel = new JPanel();
		_mainPanel.setLayout(new BorderLayout(1, 10));
		// Central stuff with labels and dropdowns
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new GridLayout(0, 2, 10, 4));
		// map source
		JLabel sourceLabel = new JLabel(I18nManager.getText("dialog.baseimage.mapsource") + ": ");
		sourceLabel.setHorizontalAlignment(JLabel.RIGHT);
		controlsPanel.add(sourceLabel);
		_mapSourceDropdown = new JComboBox<String>();
		_mapSourceDropdown.addItem("name of map source");
		// Add listener to dropdown to change zoom levels
		_mapSourceDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshDialog();
			}
		});
		controlsPanel.add(_mapSourceDropdown);
		// zoom level
		JLabel zoomLabel = new JLabel(I18nManager.getText("dialog.baseimage.zoom") + ": ");
		zoomLabel.setHorizontalAlignment(JLabel.RIGHT);
		controlsPanel.add(zoomLabel);
		_zoomDropdown = new JComboBox<String>();
		// Add action listener to enable ok button when zoom changed
		_zoomDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (_zoomDropdown.getSelectedIndex() >= 0) {
					_okButton.setEnabled(true);
					updateImagePreview();
				}
			}
		});
		controlsPanel.add(_zoomDropdown);
		_mainPanel.add(controlsPanel, BorderLayout.NORTH);

		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new BorderLayout(10, 1));
		// image preview
		_previewPanel = new ImagePreviewPanel();
		imagePanel.add(_previewPanel, BorderLayout.CENTER);

		// Label panel on right
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		JPanel downloadPanel = new JPanel();
		downloadPanel.setLayout(new BorderLayout(4, 4));
		_downloadTilesButton = new JButton(I18nManager.getText("button.load"));
		_downloadTilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				downloadRemainingTiles();
			}
		});
		_downloadTilesButton.setVisible(false);
		downloadPanel.add(_downloadTilesButton, BorderLayout.NORTH);
		_progressBar = new JProgressBar();
		_progressBar.setIndeterminate(true);
		_progressBar.setVisible(false);
		downloadPanel.add(_progressBar, BorderLayout.SOUTH);
		labelPanel.add(downloadPanel, BorderLayout.NORTH);
		JPanel labelGridPanel = new JPanel();
		labelGridPanel.setLayout(new GridLayout(0, 2, 10, 4));
		labelGridPanel.add(new JLabel(I18nManager.getText("dialog.baseimage.tiles") + ": "));
		_tilesFoundLabel = new JLabel("11 / 11");
		labelGridPanel.add(_tilesFoundLabel);
		labelGridPanel.add(new JLabel(I18nManager.getText("dialog.baseimage.size") + ": "));
		_imageSizeLabel = new JLabel("1430");
		labelGridPanel.add(_imageSizeLabel);
		labelGridPanel.add(new JLabel(" ")); // just for spacing
		labelPanel.add(labelGridPanel, BorderLayout.SOUTH);
		imagePanel.add(labelPanel, BorderLayout.EAST);

		_mainPanel.add(imagePanel, BorderLayout.CENTER);
		panel.add(_mainPanel, BorderLayout.CENTER);

		// OK, Cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// Check values, maybe don't want to exit
				if (!_useImageCheckbox.isSelected()
					|| (_mapSourceDropdown.getSelectedIndex() >= 0 && _zoomDropdown.getSelectedIndex() >= 0))
				{
					storeValues();
					_dialog.dispose();
				}
			}
		});
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Listener to close dialog if escape pressed
		KeyAdapter closer = new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_useImageCheckbox.addKeyListener(closer);
		_mapSourceDropdown.addKeyListener(closer);
		_zoomDropdown.addKeyListener(closer);
		_okButton.addKeyListener(closer);
		cancelButton.addKeyListener(closer);

		return panel;
	}

	/**
	 * Use the selected settings to make a preview image and (asynchronously) update the preview panel
	 */
	private void updateImagePreview()
	{
		// Clear labels
		_downloadTilesButton.setVisible(false);
		_tilesFoundLabel.setText("");
		_imageSizeLabel.setText("");
		if (_useImageCheckbox.isSelected() && _mapSourceDropdown.getSelectedIndex() >= 0
			&& _zoomDropdown.getItemCount() > 0 && _zoomDropdown.getSelectedIndex() >= 0)
		{
			_previewPanel.startLoading();
			// Launch a separate thread to create an image and pass it to the preview panel
			new Thread(this).start();
		}
		else {
			// clear preview
			_previewPanel.setImage(null);
		}
	}

	/**
	 * Store the selected details in the variables
	 */
	private void storeValues()
	{
		// Store values of controls in variables
		_imageDef.setUseImage(_useImageCheckbox.isSelected(),
			_mapSourceDropdown.getSelectedIndex(),
			getSelectedZoomLevel());
		// Inform parent that details have changed
		_parent.baseImageChanged();
	}

	/**
	 * Run method for separate thread.  Uses the current dialog parameters
	 * to trigger a call to the Grouter, and pass the image to the preview panel
	 */
	public void run()
	{
		// Remember the current dropdown indices, so we know whether they've changed or not
		final int mapIndex = _mapSourceDropdown.getSelectedIndex();
		final int zoomIndex = _zoomDropdown.getSelectedIndex();
		if (!_useImageCheckbox.isSelected() || mapIndex < 0 || zoomIndex < 0) {return;}

		// Get the map source from the index
		MapSource mapSource = MapSourceLibrary.getSource(mapIndex);

		// Use the Grouter to create an image (slow, blocks thread)
		GroutedImage groutedImage = _grouter.createMapImage(_track, mapSource, getSelectedZoomLevel());

		// If the dialog hasn't changed, pass the generated image to the preview panel
		if (_useImageCheckbox.isSelected()
			&& _mapSourceDropdown.getSelectedIndex() == mapIndex
			&& _zoomDropdown.getSelectedIndex() == zoomIndex
			&& groutedImage != null)
		{
			_previewPanel.setImage(groutedImage);
			final int numTilesRemaining = groutedImage.getNumTilesTotal() - groutedImage.getNumTilesUsed();
			final boolean offerDownload = numTilesRemaining > 0 && numTilesRemaining < 50 && Config.getConfigBoolean(Config.KEY_ONLINE_MODE);
			// Set values of labels
			_downloadTilesButton.setVisible(offerDownload);
			_downloadTilesButton.setEnabled(offerDownload);
			_tilesFoundLabel.setText(groutedImage.getNumTilesUsed() + " / " + groutedImage.getNumTilesTotal());
			if (groutedImage.getImageSize() > 0) {
				_imageSizeLabel.setText("" + groutedImage.getImageSize());
			}
			else {
				_imageSizeLabel.setText("");
			}
		}
		else
		{
			_previewPanel.setImage(null);
			// Clear labels
			_downloadTilesButton.setVisible(false);
			_tilesFoundLabel.setText("");
			_imageSizeLabel.setText("");
		}
	}

	/**
	 * @return zoom level selected in the dropdown
	 */
	private int getSelectedZoomLevel()
	{
		int zoomLevel = 0;
		try {
			zoomLevel = Integer.parseInt(_zoomDropdown.getSelectedItem().toString());
		}
		catch (NullPointerException npe) {}
		catch (Exception e) {
			System.err.println("Exception: " + e.getClass().getName() + " : " + e.getMessage());
		}
		return zoomLevel;
	}

	/**
	 * @return true if any map data has been found for the image
	 */
	public boolean getFoundData()
	{
		return _imageDef.getUseImage() && _imageDef.getZoom() > 0
			&& _previewPanel != null && _previewPanel.getTilesFound();
	}

	/**
	 * @return true if selected zoom is valid for the current track (based only on pixel size)
	 */
	public boolean isSelectedZoomValid()
	{
		final double xyExtent = Math.max(_track.getXRange().getRange(), _track.getYRange().getRange());
		// How many pixels does this give?
		final int zoomFactor = 1 << _imageDef.getZoom();
		final int pixCount = (int) (xyExtent * zoomFactor * 256);
		return (pixCount > 100     // less than this isn't worth it
			&& pixCount < 4000);   // don't want to run out of memory
	}

	/**
	 * @return the map grouter for retrieval of generated image
	 */
	public MapGrouter getGrouter()
	{
		return _grouter;
	}

	/**
	 * @return method triggered by "download" button, to asynchronously download the missing tiles
	 */
	private void downloadRemainingTiles()
	{
		_downloadTilesButton.setEnabled(false);
		new Thread(new Runnable() {
			public void run()
			{
				_progressBar.setVisible(true);
				// Use a grouter to get all tiles from the TileManager, including downloading
				MapGrouter grouter = new MapGrouter();
				final int mapIndex = _mapSourceDropdown.getSelectedIndex();
				if (!_useImageCheckbox.isSelected() || mapIndex < 0) {return;}
				MapSource mapSource = MapSourceLibrary.getSource(mapIndex);
				grouter.createMapImage(_track, mapSource, getSelectedZoomLevel(), true);
				_progressBar.setVisible(false);
				// And then refresh the dialog
				_grouter.clearMapImage();
				updateImagePreview();
			}
		}).start();
	}
}
