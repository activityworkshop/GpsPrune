package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.undo.UndoRearrangePhotos;

/**
 * Class to provide the function for rearranging photo points
 */
public class RearrangePhotosFunction extends GenericFunction
{
	/** Function dialog */
	private JDialog _dialog = null;
	/** Radio buttons for start/end */
	private JRadioButton[] _positionRadios = null;
	/** Radio buttons for sorting */
	private JRadioButton[] _sortRadios = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangePhotosFunction(App inApp)
	{
		super(inApp);
	}

	/** Begin the rearrange */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Reset dialog and show
		_dialog.setVisible(true);
	}

	/** Get the name key (not needed) */
	public String getNameKey() {
		return "function.rearrangephotos";
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.rearrangephotos.desc")), BorderLayout.NORTH);
		// Radios for position (start / end)
		_positionRadios = new JRadioButton[2];
		final String[] posNames = {"tostart", "toend"};
		ButtonGroup posGroup = new ButtonGroup();
		JPanel posPanel = new JPanel();
		posPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (int i=0; i<2; i++)
		{
			_positionRadios[i] = new JRadioButton(I18nManager.getText("dialog.rearrangephotos." + posNames[i]));
			posGroup.add(_positionRadios[i]);
			posPanel.add(_positionRadios[i]);
		}
		_positionRadios[0].setSelected(true);
		// Radios for sort (none / filename / time)
		_sortRadios = new JRadioButton[3];
		final String[] sortNames = {"nosort", "sortbyfilename", "sortbytime"};
		ButtonGroup sortGroup = new ButtonGroup();
		JPanel sortPanel = new JPanel();
		sortPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (int i=0; i<3; i++)
		{
			_sortRadios[i] = new JRadioButton(I18nManager.getText("dialog.rearrangephotos." + sortNames[i]));
			sortGroup.add(_sortRadios[i]);
			sortPanel.add(_sortRadios[i]);
		}
		_sortRadios[0].setSelected(true);
		// add to middle of dialog
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
		centrePanel.add(posPanel);
		centrePanel.add(sortPanel);
		dialogPanel.add(centrePanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
				_dialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Perform the rearrange
	 */
	private void finish()
	{
		Track track = _app.getTrackInfo().getTrack();
		UndoRearrangePhotos undo = new UndoRearrangePhotos(track);
		// Loop through track collecting non-photo points and photo points
		final int numPoints = track.getNumPoints();
		DataPoint[] nonPhotos = new DataPoint[numPoints];
		DataPoint[] photos = new DataPoint[numPoints];
		int numNonPhotos = 0;
		int numPhotos = 0;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.getPhoto() != null) {
				photos[numPhotos] = point;
				numPhotos++;
			}
			else {
				nonPhotos[numNonPhotos] = point;
				numNonPhotos++;
			}
		}
		// Sort photos if necessary
		if (!_sortRadios[0].isSelected() && numPhotos > 1) {
			sortPhotos(photos, _sortRadios[1].isSelected());
		}
		// Put the non-photo points and photo points together
		DataPoint[] neworder = new DataPoint[numPoints];
		if (_positionRadios[0].isSelected()) {
			// photos at front
			System.arraycopy(photos, 0, neworder, 0, numPhotos);
			System.arraycopy(nonPhotos, 0, neworder, numPhotos, numNonPhotos);
		}
		else {
			// photos at end
			System.arraycopy(nonPhotos, 0, neworder, 0, numNonPhotos);
			System.arraycopy(photos, 0, neworder, numNonPhotos, numPhotos);
		}
		// Give track the new point order
		if (track.replaceContents(neworder))
		{
			_app.getTrackInfo().getSelection().clearAll();
			_app.completeFunction(undo, I18nManager.getText("confirm.rearrangephotos"));
			// Note: subscribers are informed up to three times now
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Sort the given photo list either by filename or by time
	 * @param inPhotos array of DataPoint objects to sort
	 * @param inSortByFile true to sort by filename, false to sort by timestamp
	 * @return sorted array
	 */
	private static void sortPhotos(DataPoint[] inPhotos, boolean inSortByFile)
	{
		Comparator<DataPoint> comparator = null;
		if (inSortByFile)
		{
			// sort by filename
			comparator = new Comparator<DataPoint>() {
				public int compare(DataPoint inP1, DataPoint inP2) {
					if (inP2 == null) return -1; // all nulls at end
					if (inP1 == null) return 1;
					if (inP1.getPhoto().getFile() == null || inP2.getPhoto().getFile() == null)
						return inP1.getPhoto().getName().compareTo(inP2.getPhoto().getName());
					return inP1.getPhoto().getFile().compareTo(inP2.getPhoto().getFile());
				}
			};
		}
		else
		{
			// sort by photo timestamp
			comparator = new Comparator<DataPoint>() {
				public int compare(DataPoint inP1, DataPoint inP2) {
					if (inP2 == null) return -1; // all nulls at end
					if (inP1 == null) return 1;
					long secDiff = inP1.getPhoto().getTimestamp().getSecondsSince(inP2.getPhoto().getTimestamp());
					return (secDiff<0?-1:(secDiff==0?0:1));
				}
			};
		}
		Arrays.sort(inPhotos, comparator);
	}
}
