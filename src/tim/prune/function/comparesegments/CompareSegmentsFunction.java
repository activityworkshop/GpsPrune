package tim.prune.function.comparesegments;

import tim.prune.App;
import tim.prune.ExternalTools;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.NumberUtils;
import tim.prune.data.Track;
import tim.prune.data.Unit;
import tim.prune.data.UnitSet;
import tim.prune.function.charts.ChartSeries;
import tim.prune.gui.BoxPanel;
import tim.prune.gui.WizardLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;

/**
 * Function to compare a pair of track segments to track performance
 */
public class CompareSegmentsFunction extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Wizard to control steps of function */
	private WizardLayout _wizard = null;
	/** Top label with dynamic contents */
	private JLabel _topLabel = null;
	/** Table */
	private JTable _segmentTable = null;
	/** Model containing segment information */
	private SegmentTableModel _segmentModel = null;
	/** buttons which can be enabled/disabled */
	private JButton _compareButton = null;
	private JButton _exportButton = null;
	private JButton _chartButton = null;

	/** Remember if a single segment was selected */
	private Integer _lastSelectedSegment = null;
	/** Progress bar on wait screen */
	private JProgressBar _progressBar = null;
	private boolean _cancelled = false;
	private final ArrayList<IntersectionResult> _results = new ArrayList<>();
	/** Objects to give results to */
	private SegmentsPanel _segmentsPanel = null;
	private MatchesPanel _matchesPanel = null;
	private DataPanel _dataPanel = null;


	/** Internal class to separate out the progress calculations */
	private class ProgressWidget
	{
		private int _lastIndex = 0;
		private int _toAdd = 0;
		private void showProgress(int inIndex, int inListSize)
		{
			if (inIndex < _lastIndex) {
				_toAdd = 100;
			}
			int result = (int) (100.0 * inIndex / inListSize);
			_progressBar.setValue(result + _toAdd);
			_lastIndex = inIndex;
		}
	}

	/** Custom cell renderer using padding on right side */
	private static class RightPaddedRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable inTable, Object inValue, boolean inSelected,
			boolean inHasFocus, int inRow, int inColumn)
		{
			JLabel label = (JLabel) super.getTableCellRendererComponent(inTable, inValue, inSelected, inHasFocus, inRow, inColumn);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));
			label.setText(NumberUtils.formatNumberLocal((Double) inValue, 3));
			return label;
		}
	}


	/**
	 * Constructor
	 */
	public CompareSegmentsFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.comparesegments";
	}

	@Override
	public void begin()
	{
		ArrayList<SegmentSummary> segments = makeSegmentList(_app.getTrackInfo().getTrack());
		if (segments.size() < 2)
		{
			_app.showErrorMessage(getNameKey(), "error.comparesegments.needtwosegments");
			return;
		}
		if (_dialog == null)
		{
			_segmentModel = new SegmentTableModel();
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			// add closing listener
			_dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					_cancelled = true;
				}
			});
			_dialog.pack();
		}
		TimeZone timezone = TimezoneHelper.getSelectedTimezone(getConfig());
		Unit distanceUnits = getConfig().getUnitSet().getDistanceUnit();
		_segmentModel.init(segments, timezone, distanceUnits);
		_segmentTable.clearSelection();
		_segmentTable.getColumnModel().getColumn(2).setCellRenderer(new RightPaddedRenderer());

		_compareButton.setEnabled(segments.size() == 2);
		setDialogTitle(segments.size() == 2);
		if (segments.size() == 2) {
			_segmentTable.addRowSelectionInterval(0, 1);
		}
		_wizard.showFirstCard();
		_cancelled = false;
		_dialog.setVisible(true);
	}

	/**
	 * Choose a title for the dialog depending on whether there are only two segments (in which case no selection is needed)
	 * or more than two (in which case the user needs to select just two)
	 */
	private void setDialogTitle(boolean inTwoSegments)
	{
		final String labelKey = inTwoSegments ? "dialog.comparesegments.introtwosegments" : "dialog.comparesegments.intro";
		_topLabel.setText(I18nManager.getText(labelKey));
	}

	/**
	 * @param inTrack track object
	 * @return list of segment details for the valid segments
	 */
	private ArrayList<SegmentSummary> makeSegmentList(Track inTrack)
	{
		ArrayList<SegmentSummary> result = new ArrayList<>();
		SegmentSummary currentSegment = null;
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point == null || point.isWaypoint()) {
				continue;
			}
			if (currentSegment == null) {
				currentSegment = new SegmentSummary(i, point);
			}
			else if (point.getSegmentStart())
			{
				if (currentSegment.isValid()) {
					result.add(currentSegment);
				}
				currentSegment = new SegmentSummary(i, point);
			}
			else {
				currentSegment.addPoint(point);
			}
		}
		if (currentSegment != null && currentSegment.isValid()) {
			result.add(currentSegment);
		}
		return result;
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel cardPanel = new JPanel();
		_wizard = new WizardLayout(cardPanel);

		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout(5, 5));
		// Label at top
		_topLabel = new JLabel(I18nManager.getText("dialog.comparesegments.intro"));
		_topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		selectionPanel.add(_topLabel, BorderLayout.NORTH);

		// Table to show segments found
		_segmentTable = new JTable(_segmentModel);
		_segmentTable.getSelectionModel().addListSelectionListener(e -> checkSelection());
		_segmentTable.setRowSelectionAllowed(true);
		_segmentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_segmentTable.setAutoCreateRowSorter(true);

		JScrollPane scrollPane = new JScrollPane(_segmentTable);
		scrollPane.setPreferredSize(new Dimension(400, 250));
		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		// close window if escape pressed
		KeyAdapter escListener = new KeyAdapter() {
			public void keyReleased(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		_segmentTable.addKeyListener(escListener);

		// button panel at bottom of selection panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_compareButton = new JButton(I18nManager.getText("button.compareselected"));
		_compareButton.addActionListener(e -> doCompare());
		buttonPanel.add(_compareButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		selectionPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Second card to just show a progress bar
		JPanel waitCard = new JPanel();
		waitCard.setLayout(new BorderLayout(5, 5));
		JPanel innerWaitPanel = new JPanel();
		innerWaitPanel.setLayout(new BoxLayout(innerWaitPanel, BoxLayout.Y_AXIS));
		innerWaitPanel.add(Box.createVerticalGlue());
		innerWaitPanel.add(new JLabel(I18nManager.getText("dialog.comparesegments.comparing")));
		_progressBar = new JProgressBar(0, 200);
		_progressBar.setIndeterminate(true);
		_progressBar.setStringPainted(true);
		innerWaitPanel.add(_progressBar);
		innerWaitPanel.add(Box.createVerticalGlue());
		// Add cancel button
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(cancelButton);
		waitCard.add(innerWaitPanel, BorderLayout.CENTER);
		waitCard.add(buttonPanel, BorderLayout.SOUTH);

		// Third card to show results
		BoxPanel resultsCard = BoxPanel.create();
		// Label at top
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.comparesegments.results"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		resultsCard.add(introLabel);
		resultsCard.add(Box.createVerticalStrut(4));

		_segmentsPanel = new SegmentsPanel();
		resultsCard.add(_segmentsPanel);

		_matchesPanel = new MatchesPanel();
		resultsCard.add(_matchesPanel);
		// Button panel at the bottom
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_exportButton = new JButton(I18nManager.getText("button.exportdata"));
		_exportButton.addActionListener(e -> _wizard.showNextCard());
		buttonPanel.add(_exportButton);

		_chartButton = new JButton(I18nManager.getText("button.chart"));
		_chartButton.addActionListener(e -> showChart(_results));
		buttonPanel.add(_chartButton);
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(closeButton);
		resultsCard.add(buttonPanel);

		// Fourth card for data export
		JPanel dataCard = new JPanel();
		dataCard.setLayout(new BorderLayout());
		_dataPanel = new DataPanel();
		dataCard.add(_dataPanel, BorderLayout.CENTER);
		// back button at the bottom
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton backButton = new JButton(I18nManager.getText("button.back"));
		backButton.addActionListener(e -> _wizard.showPreviousCard());
		buttonPanel.add(backButton);
		dataCard.add(buttonPanel, BorderLayout.SOUTH);

		_wizard.addCard(selectionPanel);
		_wizard.addCard(waitCard);
		_wizard.addCard(resultsCard);
		_wizard.addCard(dataCard);
		return cardPanel;
	}

	/** Check that two of the segments are selected */
	private void checkSelection()
	{
		int numSelected = _segmentTable.getSelectedRowCount();
		_compareButton.setEnabled(numSelected == 2);
		if (numSelected == 1
			&& _lastSelectedSegment != null
			&& _lastSelectedSegment != _segmentTable.getSelectedRow())
		{
			_segmentTable.addRowSelectionInterval(_lastSelectedSegment, _lastSelectedSegment);
		}
		_lastSelectedSegment = (numSelected == 1 ? _segmentTable.getSelectedRow() : null);
	}

	/** Method to start the comparison */
	private void doCompare()
	{
		if (_segmentTable.getSelectedRowCount() != 2) {
			return;
		}
		Integer segmentIdx1 = null, segmentIdx2 = null;
		for (int i=0; i< _segmentTable.getRowCount(); i++)
		{
			if (_segmentTable.isRowSelected(i))
			{
				int modelIndex = _segmentTable.convertRowIndexToModel(i);
				if (segmentIdx1 == null)
				{
					segmentIdx1 = modelIndex;
					segmentIdx2 = modelIndex;
				}
				else
				{
					segmentIdx1 = Math.min(segmentIdx1, modelIndex);
					segmentIdx2 = Math.max(segmentIdx2, modelIndex);
				}
			}
		}
		if (segmentIdx1 == null || segmentIdx2 == null || segmentIdx2.equals(segmentIdx1)) {
			throw new IllegalArgumentException("Require exactly two segments to be selected");
		}
		// Show wait screen ...
		_progressBar.setIndeterminate(false);
		_progressBar.setValue(0);
		_wizard.showNextCard();
		final int idx1 = segmentIdx1, idx2 = segmentIdx2;
		new Thread(() -> doCompareAsync(idx1, idx2)).start();
	}

	private void doCompareAsync(int inSegmentIdx1, int inSegmentIdx2)
	{
		// Collect the intersections together into one list
		final int pointIdx1 = _segmentModel.getSegmentStartIndex(inSegmentIdx1);
		final int pointIdx2 = _segmentModel.getSegmentStartIndex(inSegmentIdx2);
		_results.clear();
		findIntersections(_results, pointIdx1, pointIdx2);
		_wizard.showNextCard();
		boolean atLeastTwoResults = _results.size() >= 2;
		_exportButton.setEnabled(atLeastTwoResults);
		_chartButton.setEnabled(atLeastTwoResults);
		if (!atLeastTwoResults)
		{
			System.err.println("Couldn't find enough intersections for the comparison");
			_app.showErrorMessage(getNameKey(), "error.comparesegments.notenoughintersections");
		}
	}

	/**
	 * Find the intersections between the two specified segments
	 * @param inResults results list to fill
	 * @param inStartIndex1 start index of first segment
	 * @param inStartIndex2 start index of second segment
	 */
	private void findIntersections(ArrayList<IntersectionResult> inResults, int inStartIndex1, int inStartIndex2)
	{
		ProgressWidget progressWidget = new ProgressWidget();
		SegmentData data1 = new SegmentData(_app.getTrackInfo().getTrack(), inStartIndex1);
		SegmentData data2 = new SegmentData(_app.getTrackInfo().getTrack(), inStartIndex2);
		boolean isData1First = data1.isBefore(data2);
		SegmentData referenceData = isData1First ? data1 : data2;
		SegmentData secondData = isData1First ? data2 : data1;
		_segmentsPanel.setDetails(referenceData, secondData, getConfig());
		inResults.clear();
		findIntersections(referenceData, secondData, inResults, false, progressWidget);
		findIntersections(secondData, referenceData, inResults, true, progressWidget);
		Collections.sort(inResults);
		_matchesPanel.setDetails(inResults, getConfig());
		_dataPanel.setDetails(inResults, getConfig());
	}

	/**
	 * Match the segments in one direction
	 * @param inPointData segment from which to take the points
	 * @param inLineData segment from which to match lines
	 * @param inResults array to fill
	 * @param inReverseResult true if the segments are swapped
	 * @param inProgressWidget widget to inform about progress
	 */
	private void findIntersections(SegmentData inPointData, SegmentData inLineData,
		ArrayList<IntersectionResult> inResults, boolean inReverseResult,
		ProgressWidget inProgressWidget)
	{
		SegmentLooper looper = new SegmentLooper(inLineData.getLines());
		final int numPoints = inPointData.getPoints().getNumPoints();
		int index = 0;
		for (PointData pd : inPointData.getPoints().getPoints())
		{
			IntersectionResult result = looper.match(pd);
			if (result != null) {
				inResults.add(inReverseResult ? result.reverse() : result);
			}
			inProgressWidget.showProgress(index, numPoints);
			index++;
			if (_cancelled) {
				return;
			}
		}
	}

	private void showChart(ArrayList<IntersectionResult> inResults)
	{
		if (!ExternalTools.isToolInstalled(getConfig(), ExternalTools.TOOL_GNUPLOT))
		{
			_app.showErrorMessage(getNameKey(), "dialog.charts.gnuplotnotfound");
			return;
		}
		final String gnuplotPath = getConfig().getConfigString(Config.KEY_GNUPLOT_PATH);
		try
		{
			Process process = Runtime.getRuntime().exec(gnuplotPath + " -persist");
			try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream()))
			{
				writer.write("set multiplot layout 3,1\n");
				writer.write("set origin 0,0\n");
				writer.write("set size 1,0.25\n");
				writeAltitudeChart(writer, inResults);

				writer.write("set origin 0,0.25\n");
				writer.write("set size 1,0.375\n");
				writeTimeAheadChart(writer, inResults);

				writer.write("set origin 0,0.625\n");
				writer.write("set size 1,0.375\n");
				writeSpeedIncreaseChart(writer, inResults);

				writer.write("unset multiplot\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to execute Gnuplot: " + e.getMessage());
		}
	}

	private void writeAltitudeChart(OutputStreamWriter inWriter, ArrayList<IntersectionResult> inResults)
		throws IOException
	{
		ChartSeries distValues = getDistanceValues(inResults);
		ChartSeries altValues = getAltitudeValues(inResults);
		// Make a temporary data file for the output (one per subchart)
		File tempFile = File.createTempFile("gpsprunedata", null);
		tempFile.deleteOnExit();
		// write out values for x and y to temporary file
		try (FileWriter tempFileWriter = new FileWriter(tempFile))
		{
			tempFileWriter.write("# Temporary data file for GpsPrune charts\n\n");
			for (int i = 0; i < inResults.size(); i++)
			{
				if (distValues.hasData(i) && altValues.hasData(i)) {
					tempFileWriter.write("" + distValues.getData(i) + ", " + altValues.getData(i) + "\n");
				}
			}
			// axis labels
			UnitSet unitSet = getConfig().getUnitSet();
			final String distLabel = I18nManager.getText(unitSet.getDistanceUnit().getShortnameKey());
			inWriter.write("set xlabel '" + I18nManager.getText("fieldname.distance") + " (" + distLabel + ")'\n");
			final String altLabel  = I18nManager.getText(unitSet.getAltitudeUnit().getShortnameKey());
			inWriter.write("set ylabel '" + I18nManager.getText("fieldname.altitude") + " (" + altLabel + ")'\n");

			inWriter.write("set style fill solid 0.5 border -1\n");
			final double minAltitude = getMinAltitude(altValues);
			inWriter.write("plot '" + tempFile.getAbsolutePath() + "' title '' with filledcurve y1=" + minAltitude + " lt rgb \"#009000\"\n");
		}
	}

	private double getMinAltitude(ChartSeries inValues)
	{
		double minValue = 0.0;
		boolean foundValue = false;
		for (int i=0; i<inValues.getNumPoints(); i++)
		{
			if (inValues.hasData(i))
			{
				double value = inValues.getData(i);
				if (minValue > value || !foundValue) {
					minValue = value;
					foundValue = true;
				}
			}
		}
		// Round here to the multiple of 10 below the minimum
		return Math.floor(minValue / 10.0) * 10.0;
	}

	private ChartSeries getDistanceValues(ArrayList<IntersectionResult> inResults)
	{
		// Calculate distances and fill in in values array
		ChartSeries values = new ChartSeries(inResults.size());
		IntersectionResult firstResult = null;
		Unit distUnit = getConfig().getUnitSet().getDistanceUnit();
		int resultNum = 0;
		for (IntersectionResult result : inResults)
		{
			final double radians;
			if (firstResult == null)
			{
				radians = 0.0;
				firstResult = result;
			}
			else {
				radians = result.getFirstDistanceRadians(firstResult);
			}
			// distance values use currently configured units
			values.setData(resultNum, Distance.convertRadiansToDistance(radians, distUnit));
			resultNum++;
		}
		return values;
	}

	private ChartSeries getAltitudeValues(ArrayList<IntersectionResult> inResults)
	{
		ChartSeries values = new ChartSeries(inResults.size());
		Unit altitudeUnit = getConfig().getUnitSet().getAltitudeUnit();
		int resultNum = 0;
		for (IntersectionResult result : inResults)
		{
			final double altitude = result.getFirstPoint().getAltitude().getValue(altitudeUnit);
			values.setData(resultNum, altitude);
			resultNum++;
		}
		return values;
	}

	private void writeTimeAheadChart(OutputStreamWriter inWriter, ArrayList<IntersectionResult> inResults)
		throws IOException
	{
		ChartSeries distValues = getDistanceValues(inResults);
		ChartSeries timeValues = getTimeValues(inResults);
		// Make a temporary data file for the output (one per subchart)
		File tempFile = File.createTempFile("gpsprunedata", null);
		tempFile.deleteOnExit();
		// write out values for x and y to temporary file
		try (FileWriter tempFileWriter = new FileWriter(tempFile))
		{
			tempFileWriter.write("# Temporary data file for GpsPrune charts\n\n");
			for (int i = 0; i < inResults.size(); i++)
			{
				if (distValues.hasData(i) && timeValues.hasData(i)) {
					tempFileWriter.write("" + distValues.getData(i) + ", " + timeValues.getData(i) + "\n");
				}
			}
			inWriter.write("set grid\n");
			// axis labels
			inWriter.write("set xlabel ''\n");
			final String axisTitle = I18nManager.getText("dialog.comparesegments.data.secsahead");
			inWriter.write("set ylabel '" + axisTitle + "'\n");

			inWriter.write("set style fill solid 0.5 border -1\n");
			inWriter.write("plot '" + tempFile.getAbsolutePath() + "' title '' with points lt rgb \"#009000\"\n");
		}
	}

	private ChartSeries getTimeValues(ArrayList<IntersectionResult> inResults)
	{
		ChartSeries values = new ChartSeries(inResults.size());
		int resultNum = 0;
		IntersectionResult firstResult = null;
		for (IntersectionResult result : inResults)
		{
			final long secsAhead;
			if (firstResult == null) {
				firstResult = result;
				secsAhead = 0L;
			}
			else {
				secsAhead = result.getFirstDurationSeconds(firstResult) - result.getSecondDurationSeconds(firstResult);
			}
			values.setData(resultNum, secsAhead);
			resultNum++;
		}
		return values;
	}

	private void writeSpeedIncreaseChart(OutputStreamWriter inWriter, ArrayList<IntersectionResult> inResults)
		throws IOException
	{
		ChartSeries distValues = getDistanceValues(inResults);
		ChartSeries speedValues = getSpeedValues(inResults);
		// Make a temporary data file for the output (one per subchart)
		File tempFile = File.createTempFile("gpsprunedata", null);
		tempFile.deleteOnExit();
		// write out values for x and y to temporary file
		try (FileWriter tempFileWriter = new FileWriter(tempFile))
		{
			tempFileWriter.write("# Temporary data file for GpsPrune charts\n\n");
			for (int i = 0; i < inResults.size(); i++)
			{
				if (distValues.hasData(i) && speedValues.hasData(i)) {
					tempFileWriter.write("" + distValues.getData(i) + ", " + speedValues.getData(i) + "\n");
				}
			}
			inWriter.write("set grid\n");
			// axis labels
			inWriter.write("set xlabel ''\n");
			final Unit speedUnit = getConfig().getUnitSet().getSpeedUnit();
			String axisTitle = I18nManager.getText("dialog.comparesegments.data.speeddiff")
					+ " (" + I18nManager.getText(speedUnit.getShortnameKey()) + ")";
			inWriter.write("set ylabel '" + axisTitle + "'\n");

			inWriter.write("set style fill solid 0.5 border -1\n");
			inWriter.write("plot '" + tempFile.getAbsolutePath() + "' title '' with linespoints lt rgb \"#009000\"\n");
		}
	}

	private ChartSeries getSpeedValues(ArrayList<IntersectionResult> inResults)
	{
		final Unit distUnit = getConfig().getUnitSet().getDistanceUnit();
		ChartSeries values = new ChartSeries(inResults.size());
		int resultNum = 0;
		for (IntersectionResult result : inResults)
		{
			final double radsPerSec = result.getDeltaSpeedRadiansPerSec();
			final double speedDiffUnitsPerHour = Distance.convertRadiansToDistance(radsPerSec, distUnit)
				* 60.0 * 60.0;
			values.setData(resultNum, speedDiffUnitsPerHour);
			resultNum++;
		}
		return values;
	}
}
