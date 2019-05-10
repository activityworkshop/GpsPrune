package tim.prune.function.autoplay;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.IconManager;
import tim.prune.gui.WholeNumberField;

/**
 * Function to handle the autoplay of a track
 */
public class AutoplayFunction extends GenericFunction implements Runnable
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Entry field for number of seconds to autoplay for */
	private WholeNumberField _durationField = null;
	/** Checkbox for using point timestamps */
	private JCheckBox _useTimestampsCheckbox = null;
	/** Buttons for controlling autoplay */
	private JButton _rewindButton = null, _pauseButton = null, _playButton = null;
	/** Flag for recalculating all the times */
	private boolean _needToRecalculate = true;
	/** Point list */
	private PointList _pointList = null;
	/** Flag to see if we're still running or not */
	private boolean _running = false;
	/** Remember the time we started playing */
	private long _startTime = 0L;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public AutoplayFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.autoplay";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_dialog.setResizable(false);
			_dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					_running = false;
					super.windowClosing(e);
				}
			});
		}
		// Don't select any point
		_app.getTrackInfo().selectPoint(-1);
		// enable buttons
		enableButtons(false, true); // can't pause, can play
		// MAYBE: reset duration if it's too long
		// Disable point checkbox if there aren't any times
		final boolean hasTimes = _app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP);
		_useTimestampsCheckbox.setEnabled(hasTimes);
		if (!hasTimes)
		{
			_useTimestampsCheckbox.setSelected(false);
		}

		_needToRecalculate = true;
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		// Duration panel
		JPanel durationPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(durationPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.autoplay.duration") + " :"));
		_durationField = new WholeNumberField(3);
		_durationField.setValue(60); // default is one minute
		_durationField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onParamsChanged();
			}
		});
		grid.add(_durationField);
		durationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		dialogPanel.add(durationPanel);
		// Checkbox
		_useTimestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.autoplay.usetimestamps"));
		_useTimestampsCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		dialogPanel.add(_useTimestampsCheckbox);
		_useTimestampsCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onParamsChanged();
			}
		});
		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(_rewindButton = new JButton(IconManager.getImageIcon(IconManager.AUTOPLAY_REWIND)));
		_rewindButton.setToolTipText(I18nManager.getText("dialog.autoplay.rewind"));
		_rewindButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRewindPressed();
			}
		});
		buttonPanel.add(_pauseButton = new JButton(IconManager.getImageIcon(IconManager.AUTOPLAY_PAUSE)));
		_pauseButton.setToolTipText(I18nManager.getText("dialog.autoplay.pause"));
		_pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onPausePressed();
			}
		});
		buttonPanel.add(_playButton = new JButton(IconManager.getImageIcon(IconManager.AUTOPLAY_PLAY)));
		_playButton.setToolTipText(I18nManager.getText("dialog.autoplay.play"));
		_playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onPlayPressed();
			}
		});
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		dialogPanel.add(buttonPanel);
		return dialogPanel;
	}

	/**
	 * React to a change to either the duration or the checkbox
	 */
	private void onParamsChanged()
	{
		onRewindPressed();
		enableButtons(false, _durationField.getValue() > 0);
		_needToRecalculate = true;
	}

	/**
	 * React to rewind button pressed - stop and go back to the first point
	 */
	private void onRewindPressed()
	{
		//System.out.println("Rewind!  Stop thread if playing");
		_running = false;
		if (_pointList != null)
		{
			_pointList.set(0);
			_app.getTrackInfo().selectPoint(_pointList.getCurrentPointIndex());
		}
	}

	/**
	 * React to pause button pressed - stop scrolling but maintain position
	 */
	private void onPausePressed()
	{
		//System.out.println("Pause!  Stop thread if playing");
		_running = false;
		enableButtons(false, true);
	}

	/**
	 * React to play button being pressed - either start or resume
	 */
	private void onPlayPressed()
	{
		//System.out.println("Play!");
		if (_needToRecalculate) {
			recalculateTimes();
		}
		enableButtons(true, false);
		if (_pointList.isAtStart() || _pointList.isFinished())
		{
			_pointList.set(0);
			_startTime = System.currentTimeMillis();
		}
		else
		{
			// Get current millis from pointList, reset _startTime
			_startTime = System.currentTimeMillis() - _pointList.getCurrentMilliseconds();
		}
		new Thread(this).start();
	}

	/**
	 * Recalculate the times using the dialog settings
	 */
	private void recalculateTimes()
	{
		//System.out.println("Recalculate using params " + _durationField.getValue()
		//	+ " and " + (_useTimestampsCheckbox.isSelected() ? "times" : "indexes"));
		if (_useTimestampsCheckbox.isSelected()) {
			_pointList = generatePointListUsingTimes(_app.getTrackInfo().getTrack(), _durationField.getValue());
		}
		else {
			_pointList = generatePointListUsingIndexes(_app.getTrackInfo().getTrack().getNumPoints(), _durationField.getValue());
		}
		_needToRecalculate = false;
	}

	/**
	 * Enable and disable the pause and play buttons
	 * @param inCanPause true to enable pause button
	 * @param inCanPlay  true to enable play button
	 */
	private void enableButtons(boolean inCanPause, boolean inCanPlay)
	{
		_pauseButton.setEnabled(inCanPause);
		_playButton.setEnabled(inCanPlay);
	}

	/**
	 * Generate a points list based just on the point timestamps
	 * (points without timestamps will be ignored)
	 * @param inTrack track object
	 * @param inDuration number of seconds to play
	 * @return PointList object
	 */
	private static PointList generatePointListUsingTimes(Track inTrack, int inDuration)
	{
		// Make a Set of all the points with timestamps and sort them
		TreeSet<PointInfo> set = new TreeSet<PointInfo>();
		int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			PointInfo info = new PointInfo(inTrack.getPoint(i), i);
			if (info.getTimestamp() != null) {
				set.add(info);
			}
		}
		// For each point, keep track of the time since the previous time
		Timestamp previousTime = null;
		long trackMillis = 0L;
		// Copy info to point list
		numPoints = set.size();
		PointList list = new PointList(numPoints);
		Iterator<PointInfo> it = set.iterator();
		while (it.hasNext())
		{
			PointInfo info = it.next();
			if (previousTime != null)
			{
				if (info.getSegmentFlag()) {
					trackMillis += 1000; // just add a second if it's a new segment
				}
				else {
					trackMillis += (info.getTimestamp().getMillisecondsSince(previousTime));
				}
			}
			previousTime = info.getTimestamp();
			list.setPoint(trackMillis, info.getIndex());
		}
		// Now normalize the list to the requested length
		list.normalize(inDuration);
		return list;
	}


	/**
	 * Generate a points list based just on indexes, ignoring timestamps
	 * @param inNumPoints number of points in track
	 * @param inDuration number of seconds to play
	 * @return PointList object
	 */
	private static PointList generatePointListUsingIndexes(int inNumPoints, int inDuration)
	{
		// simple case, just take all the points in the track
		PointList list = new PointList(inNumPoints);
		// Add each of the points in turn
		for (int i=0; i<inNumPoints; i++)
		{
			list.setPoint(i, i);
		}
		list.normalize(inDuration);
		return list;
	}

	/**
	 * Run method, for scrolling in separate thread
	 */
	public void run()
	{
		_running = true;
		_app.getTrackInfo().selectPoint(_pointList.getCurrentPointIndex());
		while (_running && !_pointList.isFinished())
		{
			_pointList.set(System.currentTimeMillis() - _startTime);
			final int pointIndex = _pointList.getCurrentPointIndex();
			//System.out.println("Set point index to " + pointIndex);
			_app.getTrackInfo().selectPoint(pointIndex);
			long waitInterval = _pointList.getMillisUntilNextPoint(System.currentTimeMillis() - _startTime);
			if (waitInterval < 20) {waitInterval = 20;}
			try {Thread.sleep(waitInterval);}
			catch (InterruptedException ie) {}
		}
		_running = false;
		enableButtons(false, true);
	}
}
