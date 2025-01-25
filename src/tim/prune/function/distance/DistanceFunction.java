package tim.prune.function.distance;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Class to provide the distance measuring function between waypoints
 */
public class DistanceFunction extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Table for 'from' point selection */
	private JTable _pointTable = null;
	/** Model for 'from' table */
	private FromTableModel _fromModel = null;
	/** Table for showing distances, bearings */
	private JTable _distancesTable = null;
	/** Model for distance table */
	private DistanceTableModel _distModel = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public DistanceFunction(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.distances";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		ArrayList<DataPoint> pointList = getPointList(_app.getTrackInfo());
		// Check if point list has size at least 2
		if (pointList.size() < 2) {
			_app.showErrorMessage(getNameKey(), "dialog.distances.toofewpoints");
			return;
		}
		_fromModel.init(pointList);
		_distModel.init(pointList);
		final int pointIndex = getPointIndex(pointList, _app.getTrackInfo());
		_pointTable.getSelectionModel().setSelectionInterval(pointIndex, pointIndex);
		_distModel.recalculate(pointIndex, getConfig());
		centerAlignBearingColumn();
		_dialog.setVisible(true);
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(5, 5));
		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.distances.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialogPanel.add(topLabel, BorderLayout.NORTH);

		// First table for 'from point'
		_fromModel = new FromTableModel();
		_pointTable = new JTable(_fromModel);
		_pointTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				_distModel.recalculate(_pointTable.getSelectedRow(), getConfig());
			}
		});
		JScrollPane fromScrollPane = new JScrollPane(_pointTable);
		fromScrollPane.setPreferredSize(new Dimension(100, 250));

		// second table for distances
		_distModel = new DistanceTableModel();
		_distancesTable = new JTable(_distModel);
		_distancesTable.setAutoCreateRowSorter(true);
		JScrollPane toScrollPane = new JScrollPane(_distancesTable);
		toScrollPane.setPreferredSize(new Dimension(200, 250));

		// Combine using a horizontal split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fromScrollPane, toScrollPane);
		splitPane.setDividerLocation(0.3);
		splitPane.setResizeWeight(0.4);
		dialogPanel.add(splitPane, BorderLayout.CENTER);

		// close window if escape pressed
		KeyAdapter escListener = new KeyAdapter() {
			public void keyReleased(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_pointTable.addKeyListener(escListener);
		_distancesTable.addKeyListener(escListener);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(closeButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * We want the right-hand column aligned centrally.
	 * For some reason we need to do this after every table update, as it gets forgotten after model.recalculate
	 */
	private void centerAlignBearingColumn()
	{
		// center the third column, so it's not so close to the second column
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		_distancesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	}

	/**
	 * Get the point list for the tables
	 * @param inTrackInfo TrackInfo object
	 */
	private static ArrayList<DataPoint> getPointList(TrackInfo inTrackInfo)
	{
		// Get the list of waypoints (if any)
		ArrayList<DataPoint> pointList = new ArrayList<>();
		inTrackInfo.getTrack().getWaypoints(pointList);
		// Get the current point (if any)
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		if (currPoint != null && !currPoint.isWaypoint()) {
			// Add current point to start of list
			pointList.add(0, currPoint);
		}
		return pointList;
	}

	/**
	 * Find the point to select from the given point list
	 * @param pointList list of points
	 * @param inTrackInfo current track info to get selected point (if any)
	 * @return index of point to be selected
	 */
	private static int getPointIndex(ArrayList<DataPoint> pointList, TrackInfo inTrackInfo)
	{
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		if (currPoint != null && currPoint.isWaypoint())
		{
			// Currently selected point is a waypoint, so select this one for convenience
			for (int i=0; i<pointList.size(); i++)
			{
				if (pointList.get(i) == currPoint) {
					return i;
				}
			}
		}
		return 0;
	}
}
