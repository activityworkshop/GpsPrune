package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.ConnectMultipleMediaCmd;
import tim.prune.cmd.EditSingleFieldCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Track;
import tim.prune.function.edit.PointEdit;

/**
 * Class to provide the function to delete the values of a single field
 * for all points in the current range
 */
public class DeleteFieldValues extends GenericFunction
{
	private JDialog _dialog = null;
	private JList<String> _fieldList = null;
	private FieldListModel _listModel = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DeleteFieldValues(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.deletefieldvalues";
	}

	/**
	 * Begin the function
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
		// refresh the dialog
		initDialog();
		// Check whether any fields left
		if (_listModel.getSize() < 1) {
			_app.showErrorMessage(getNameKey(), "dialog.deletefieldvalues.nofields");
		}
		else {
			_dialog.setVisible(true);
		}
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.deletefieldvalues.intro")), BorderLayout.NORTH);
		// List in centre
		_fieldList = new JList<String>(new String[] {"First field", "Second field"});
		// These entries will be replaced by the initDialog method
		_fieldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_fieldList.addListSelectionListener(e -> _okButton.setEnabled(_fieldList.getSelectedIndex() >= 0));
		dialogPanel.add(new JScrollPane(_fieldList), BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> finish());
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Initialise the dialog with the field list
	 */
	private void initDialog()
	{
		_listModel = new FieldListModel();
		int selStart = _app.getTrackInfo().getSelection().getStart();
		int selEnd = _app.getTrackInfo().getSelection().getEnd();
		// Loop over fields in track
		final Track track = _app.getTrackInfo().getTrack();
		FieldList fieldsInTrack = track.getFieldList();
		for (int i=0; i<fieldsInTrack.getNumFields(); i++)
		{
			Field field = fieldsInTrack.getField(i);
			if (field != Field.LATITUDE && field != Field.LONGITUDE
				&& track.hasData(field, selStart, selEnd))
			{
				_listModel.addField(field);
			}
		}
		// These aren't really fields but can also be deleted:
		if (track.hasData(Field.PHOTO, selStart, selEnd)) {
			_listModel.addField(Field.PHOTO);
		}
		if (track.hasData(Field.AUDIO, selStart, selEnd)) {
			_listModel.addField(Field.AUDIO);
		}
		_fieldList.setModel(_listModel);
		_fieldList.clearSelection();
		_okButton.setEnabled(false);
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		Field field = _listModel.getField(_fieldList.getSelectedIndex());
		if (field == null) {
			return;
		}
		final int selStart = _app.getTrackInfo().getSelection().getStart();
		final int selEnd = _app.getTrackInfo().getSelection().getEnd();
		_dialog.dispose();
		_app.getTrackInfo().getSelection().markInvalid();
		Command command = makeCommand(field, selStart, selEnd);
		command.setDescription(I18nManager.getText("undo.deletefieldvalues", field.getName()));
		command.setConfirmText(I18nManager.getText("confirm.deletefieldvalues"));
		_app.execute(command);
	}

	/**
	 * @return a suitable command to delete the field values for the given field
	 */
	private Command makeCommand(Field field, int selStart, int selEnd)
	{
		if (field == Field.PHOTO || field == Field.AUDIO)
		{
			// It's a media field so use a ConnectMultipleMediaCmd instead
			MediaLinkType linkType = field == Field.PHOTO ? MediaLinkType.LINK_PHOTOS : MediaLinkType.LINK_AUDIOS;
			ArrayList<PointAndMedia> points = new ArrayList<>();
			Track track = _app.getTrackInfo().getTrack();
			for (int i=selStart; i<= selEnd; i++) {
				points.add(new PointAndMedia(track.getPoint(i), null));
			}
			return new ConnectMultipleMediaCmd(linkType, points);
		}
		// It's a regular field so use EditSingleFieldCmd with values of null
		ArrayList<PointEdit> edits = new ArrayList<>();
		for (int i=selStart; i<= selEnd; i++) {
			edits.add(new PointEdit(i, null));
		}
		return new EditSingleFieldCmd(field, edits);
	}
}
