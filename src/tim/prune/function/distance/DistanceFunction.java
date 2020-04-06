package tim.prune.function.distance;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	/** Model for distance table */
	private DistanceTableModel _distModel = null;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public DistanceFunction(App inApp)
	{
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
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
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
		_pointTable.getSelectionModel().setSelectionInterval(0, 0);
		_distModel.recalculate(0);
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

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 2));
		// First table for 'from point'
		_fromModel = new FromTableModel();
		_pointTable = new JTable(_fromModel);
		_pointTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			/** selection changed */
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					_distModel.recalculate(_pointTable.getSelectedRow());
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(_pointTable);
		scrollPane.setPreferredSize(new Dimension(100, 250));
		mainPanel.add(scrollPane);
		// second table for distances
		_distModel = new DistanceTableModel();
		JTable distTable = new JTable(_distModel);
		distTable.setAutoCreateRowSorter(true);
		scrollPane = new JScrollPane(distTable);
		scrollPane.setPreferredSize(new Dimension(200, 250));
		mainPanel.add(scrollPane);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// close window if escape pressed
		KeyAdapter escListener = new KeyAdapter() {
			public void keyReleased(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_pointTable.addKeyListener(escListener);
		distTable.addKeyListener(escListener);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(closeButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Get the point list for the tables
	 * @param inTrackInfo TrackInfo object
	 */
	private static ArrayList<DataPoint> getPointList(TrackInfo inTrackInfo)
	{
		// Get the list of waypoints (if any)
		ArrayList<DataPoint> pointList = new ArrayList<DataPoint>();
		inTrackInfo.getTrack().getWaypoints(pointList);
		// Get the current point (if any)
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		if (currPoint != null && !currPoint.isWaypoint()) {
			// Add current point to start of list
			pointList.add(0, currPoint);
		}
		return pointList;
	}
}
