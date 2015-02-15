package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import tim.prune.I18nManager;
import tim.prune.data.Track;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.save.BaseImageConfigDialog;
import tim.prune.save.BaseImageConsumer;
import tim.prune.save.MapGrouter;
import tim.prune.threedee.ImageDefinition;

/**
 * Panel used to show the current base image details
 * and an edit button to change the definition
 */
public class BaseImageDefinitionPanel extends JPanel implements BaseImageConsumer
{
	/** Parent object (if any) */
	private BaseImageConsumer _parent = null;
	/** Label to describe the current settings */
	private JLabel _baseImageLabel = null;
	/** Button for changing the definition */
	private JButton _editButton = null;
	/** Dialog called by the "Edit" button to change the settings */
	private BaseImageConfigDialog _baseImageConfig = null;


	/**
	 * Constructor
	 * @param inParent parent object to inform about changes
	 * @param inParentDialog parent dialog
	 * @param inTrack track object
	 */
	public BaseImageDefinitionPanel(BaseImageConsumer inParent, JDialog inParentDialog,
		Track inTrack)
	{
		_parent = inParent;
		_baseImageConfig = new BaseImageConfigDialog(this, inParentDialog, inTrack);

		// Etched border
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		setLayout(new BorderLayout());

		// GUI components
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout(10, 4));
		subPanel.add(new JLabel(I18nManager.getText("dialog.exportpov.baseimage") + ": "), BorderLayout.WEST);
		_baseImageLabel = new JLabel("Typical sourcename");
		subPanel.add(_baseImageLabel, BorderLayout.CENTER);
		_editButton = new JButton(I18nManager.getText("button.edit"));
		_editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				changeBaseImage();
			}
		});
		subPanel.add(_editButton, BorderLayout.EAST);
		add(subPanel, BorderLayout.NORTH);
	}

	/**
	 * @param inDefinition image definition from interactive step
	 */
	public void initImageParameters(ImageDefinition inDefinition)
	{
		_baseImageConfig.setImageDefinition(inDefinition);
	}

	/**
	 * Change the base image by calling the BaseImageConfigDialog
	 */
	private void changeBaseImage()
	{
		// Check if there is a cache to use
		if (BaseImageConfigDialog.isImagePossible())
		{
			// Show new dialog to choose image details
			_baseImageConfig.begin();
		}
		// TODO: What if it isn't possible?  Should the caller show the error message?
		//else {
		//	_app.showErrorMessage(getNameKey(), "dialog.exportimage.noimagepossible");
		//}
	}

	/**
	 * Callback from base image config dialog
	 */
	public void baseImageChanged()
	{
		updateBaseImageDetails();
		if (_parent != null) {
			_parent.baseImageChanged();
		}
	}

	/**
	 * Update the description label according to the selected base image details
	 */
	public void updateBaseImageDetails()
	{
		String desc = null;
		ImageDefinition imageDef = _baseImageConfig.getImageDefinition();
		// Check if selected zoom level is suitable or not, if not then set image to no
		if (imageDef.getUseImage() && !_baseImageConfig.isSelectedZoomValid()) {
			imageDef.setUseImage(false, imageDef.getSourceIndex(), 5);
		}
		// Make a description for the label
		if (imageDef.getUseImage())
		{
			MapSource source = MapSourceLibrary.getSource(imageDef.getSourceIndex());
			if (source != null)
			{
				desc = source.getName() + " (" + imageDef.getZoom() + ")";
			}
		}
		if (desc == null) {
			desc = I18nManager.getText("dialog.about.no");
		}
		_baseImageLabel.setText(desc);
		_editButton.setEnabled(BaseImageConfigDialog.isImagePossible());
	}

	/**
	 * @return the grouter object for reuse of the prepared images
	 */
	public MapGrouter getGrouter()
	{
		return _baseImageConfig.getGrouter();
	}

	/**
	 * @return image definition
	 */
	public ImageDefinition getImageDefinition() {
		return _baseImageConfig.getImageDefinition();
	}

	/**
	 * @return true if any tiles were found
	 */
	public boolean getFoundData() {
		return _baseImageConfig.getFoundData();
	}
}
