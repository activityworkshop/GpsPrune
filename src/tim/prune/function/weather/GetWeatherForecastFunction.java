package tim.prune.function.weather;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Function to display a weather forecast for the current location
 * using the services of openweathermap.org
 */
public class GetWeatherForecastFunction extends GenericFunction
{
	/** Dialog object */
	private JDialog _dialog = null;
	/** Label for location */
	private JLabel _locationLabel = null;
	/** Label for the forecast update time */
	private JLabel _updateTimeLabel = null;
	/** Label for the sunrise and sunset times */
	private JLabel _sunriseLabel = null;
	/** Radio button for selecting current weather */
	private JRadioButton _currentForecastRadio = null;
	/** Radio button for selecting daily forecasts */
	private JRadioButton _dailyForecastRadio = null;
	/** Dropdown for selecting celsius / fahrenheit */
	private JComboBox<String> _tempUnitsDropdown = null;
	/** Table to hold the forecasts */
	private JTable _forecastsTable = null;
	/** Table model */
	private final WeatherTableModel _tableModel = new WeatherTableModel();
	/** Set of previously obtained results, to avoid repeating calls */
	private final ResultSet _resultSet = new ResultSet();
	/** Location id obtained from current forecast */
	private String _locationId = null;
	/** Access to xml streams */
	private StreamProvider _streamProvider;
	/** Flag to show that forecast is currently running, don't start another */
	private boolean _isRunning = false;


	/** Constructor */
	public GetWeatherForecastFunction(App inApp)
	{
		super(inApp);
		_streamProvider = new OnlineStreamProvider();
	}

	/** @return name key */
	public String getNameKey() {
		return "function.getweatherforecast";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Initialise dialog, show empty list
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Clear results
		_locationId = null;
		_tableModel.clear();
		_locationLabel.setText(I18nManager.getText("confirm.running"));
		_updateTimeLabel.setText("");
		_sunriseLabel.setText("");
		_currentForecastRadio.setSelected(true);

		// Start new thread to load list asynchronously
		new Thread(this::run).start();

		_dialog.setVisible(true);
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 4));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		_locationLabel = new JLabel(I18nManager.getText("confirm.running"));
		_locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(_locationLabel);
		_updateTimeLabel = new JLabel(" ");
		_updateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(_updateTimeLabel);
		_sunriseLabel = new JLabel(" ");
		_sunriseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(_sunriseLabel);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		radioPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		ButtonGroup forecastTypeGroup = new ButtonGroup();
		_currentForecastRadio = new JRadioButton(I18nManager.getText("dialog.weather.currentforecast"));
		_dailyForecastRadio = new JRadioButton(I18nManager.getText("dialog.weather.dailyforecast"));
		JRadioButton threeHourlyRadio = new JRadioButton(I18nManager.getText("dialog.weather.3hourlyforecast"));
		forecastTypeGroup.add(_currentForecastRadio);
		forecastTypeGroup.add(_dailyForecastRadio);
		forecastTypeGroup.add(threeHourlyRadio);
		radioPanel.add(_currentForecastRadio);
		radioPanel.add(_dailyForecastRadio);
		radioPanel.add(threeHourlyRadio);
		_currentForecastRadio.setSelected(true);
		ActionListener radioListener = e -> {
			if (!_isRunning) {
				new Thread(this::run).start();
			}
		};
		_currentForecastRadio.addActionListener(radioListener);
		_dailyForecastRadio.addActionListener(radioListener);
		threeHourlyRadio.addActionListener(radioListener);
		radioPanel.add(Box.createHorizontalGlue());
		radioPanel.add(Box.createHorizontalStrut(40));

		// Dropdown for temperature units
		radioPanel.add(new JLabel(I18nManager.getText("dialog.weather.temperatureunits") + ": "));
		_tempUnitsDropdown = new JComboBox<>(new String[] {
			I18nManager.getText("units.degreescelsius"), I18nManager.getText("units.degreesfahrenheit")
		});
		_tempUnitsDropdown.setMaximumSize(_tempUnitsDropdown.getPreferredSize());
		_tempUnitsDropdown.addActionListener(radioListener);
		radioPanel.add(_tempUnitsDropdown);
		radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(radioPanel);
		dialogPanel.add(topPanel, BorderLayout.NORTH);

		final IconRenderer iconRenderer = new IconRenderer(getIconManager());
		_forecastsTable = new JTable(_tableModel)
		{
			public TableCellRenderer getCellRenderer(int row, int column) {
				if ((row == WeatherTableModel.ROW_ICON)) {
					return iconRenderer;
				}
				return super.getCellRenderer(row, column);
			}
		};
		_forecastsTable.setRowSelectionAllowed(false);
		_forecastsTable.setRowHeight(2, 55); // make just that row high enough to see icons
		_forecastsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		_forecastsTable.getTableHeader().setReorderingAllowed(false);
		_forecastsTable.setShowHorizontalLines(false);

		JScrollPane scroller = new JScrollPane(_forecastsTable);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroller.setPreferredSize(new Dimension(500, 210));
		scroller.getViewport().setBackground(Color.white);

		dialogPanel.add(scroller, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton launchButton = new JButton(I18nManager.getText("button.showwebpage"));
		launchButton.addActionListener(e -> {
			BrowserLauncher.launchBrowser("http://openweathermap.org/city/" + (_locationId == null ? "" : _locationId));
		});
		buttonPanel.add(launchButton);
		// close
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(closeButton);
		// Add a holder panel with a static label to credit openweathermap
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.add(new JLabel(I18nManager.getText("dialog.weather.creditnotice")));
		southPanel.add(buttonPanel);
		dialogPanel.add(southPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Get the weather forecast in a separate thread
	 */
	public void run()
	{
		if (_isRunning) {return;} // don't run twice
		_isRunning = true;

		// Are we getting the current details, or getting a forecast?
		final boolean isCurrent = _locationId == null || _currentForecastRadio.isSelected();
		final boolean isDailyForecast = _dailyForecastRadio.isSelected() && !isCurrent;
		final boolean isHourlyForecast = !isCurrent && !isDailyForecast;
		final boolean isUsingCelsius  = _tempUnitsDropdown.getSelectedIndex() == 0;

		// Have we got these results already?  Look in store
		WeatherResults results = _resultSet.getWeather(_locationId, isCurrent, isDailyForecast, isHourlyForecast, isUsingCelsius);
		if (results == null)
		{
			if (isCurrent)
			{
				// Get the current details using either lat/long or locationId
				results = getCurrentWeather(isUsingCelsius);
				// If the current radio isn't selected, select it
				if (!_currentForecastRadio.isSelected()) {
					_currentForecastRadio.setSelected(true);
				}
			}
			else
			{
				// Get the specified forecast using the retrieved locationId
				results = getWeatherForecast(isDailyForecast, isUsingCelsius);
			}
			// If it's a valid answer, store it for later
			if (results != null)
			{
				_resultSet.setWeather(results, _locationId, isCurrent, isDailyForecast, isHourlyForecast, isUsingCelsius);
			}
		}

		// update table contents and labels
		if (results != null)
		{
			final WeatherResults finalResults = results;
			SwingUtilities.invokeLater(() -> {
				_tableModel.setResults(finalResults);
				adjustTable();
			});
			_locationLabel.setText(I18nManager.getText("dialog.weather.location") + ": " + results.getLocationName());
			final String ut = results.getUpdateTime();
			_updateTimeLabel.setText(I18nManager.getText("dialog.weather.update") + ": " + (ut == null ? "" : ut));
			if (results.getSunriseTime() != null && results.getSunsetTime() != null)
			{
				_sunriseLabel.setText(I18nManager.getText("dialog.weather.sunrise") + ": " + results.getSunriseTime()
					+ ", " + I18nManager.getText("dialog.weather.sunset") + ": " + results.getSunsetTime());
			}
			else {
				_sunriseLabel.setText("");
			}
		}

		// finished running
		_isRunning = false;
	}


	/**
	 * Adjust the column widths and row heights to fit the displayed data
	 */
	private void adjustTable()
	{
		if (!_tableModel.isEmpty())
		{
			// adjust column widths for all columns
			for (int i=0; i<_forecastsTable.getColumnCount(); i++)
			{
				double maxWidth = 0.0;
				for (int j=0; j<_forecastsTable.getRowCount(); j++)
				{
					final String value = _tableModel.getValueAt(j, i).toString();
					maxWidth = Math.max(maxWidth, _forecastsTable.getCellRenderer(0, 0).getTableCellRendererComponent(
						_forecastsTable, value, false, false, 0, 0).getPreferredSize().getWidth());
				}
				_forecastsTable.getColumnModel().getColumn(i).setMinWidth((int) maxWidth + 2);
			}
			// Set minimum row heights
			final int labelHeight = (int) (_forecastsTable.getCellRenderer(0, 0).getTableCellRendererComponent(
				_forecastsTable, "M", false, false, 0, 0).getMinimumSize().getHeight() * 1.2f + 4);
			for (int i=0; i<_forecastsTable.getRowCount(); i++)
			{
				if (i == WeatherTableModel.ROW_ICON) {
					_forecastsTable.setRowHeight(i, 55);
				}
				else {
					_forecastsTable.setRowHeight(i, labelHeight);
				}
			}
		}
	}

	/**
	 * Get the current weather using the lat/long and return the results
	 * @param inUseCelsius true for celsius, false for fahrenheit
	 * @return weather results
	 */
	WeatherResults getCurrentWeather(boolean inUseCelsius)
	{
		final Track track = _app.getTrackInfo().getTrack();
		if (track.getNumPoints() < 1) {return null;}
		// Get coordinates to lookup
		final double lat, lon;
		// See if a point is selected, if so use that
		DataPoint currPoint = _app.getTrackInfo().getCurrentPoint();
		if (currPoint != null)
		{
			// Use selected point
			lat = currPoint.getLatitude().getDouble();
			lon = currPoint.getLongitude().getDouble();
		}
		else
		{
			lat = track.getLatRange().getMidValue();
			lon = track.getLonRange().getMidValue();
		}

		// Create a handler to parse the returned XML
		OWMCurrentHandler xmlHandler = new OWMCurrentHandler();
		try (InputStream inStream = _streamProvider.getCurrentWeatherStream(lat, lon, _locationId, inUseCelsius))
		{
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inStream, xmlHandler);
		}
		catch (IOException | SAXException | ParserConfigurationException e)
		{
			// Show error message but don't close dialog
			_app.showErrorMessageNoLookup(getNameKey(), e.getClass().getName() + " - " + e.getMessage());
			_isRunning = false;
			return null;
		}

		// Save the location id
		if (xmlHandler.getLocationId() != null) {
			_locationId = xmlHandler.getLocationId();
		}
		// Get the results from the handler and return
		WeatherResults results = new WeatherResults();
		results.setForecast(xmlHandler.getCurrentWeather());
		results.setLocationName(xmlHandler.getLocationName());
		results.setUpdateTime(xmlHandler.getUpdateTime());
		results.setSunriseSunsetTimes(xmlHandler.getSunriseTime(), xmlHandler.getSunsetTime());
		results.setTempsCelsius(inUseCelsius);
		return results;
	}


	/**
	 * Get the weather forecast for the current location id and populate in _results
	 * @param inDaily true for daily, false for 3-hourly
	 * @param inCelsius true for celsius, false for fahrenheit
	 * @return weather results
	 */
	WeatherResults getWeatherForecast(boolean inDaily, boolean inCelsius)
	{
		if (_locationId == null) {
			return null;
		}

		// Create a handler to parse the returned XML
		OWMForecastHandler xmlHandler = new OWMForecastHandler();
		try (InputStream inStream = _streamProvider.getForecastStream(_locationId, inDaily, inCelsius))
		{
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inStream, xmlHandler);
		}
		catch (Exception e)
		{
			// Show error message but don't close dialog
			_app.showErrorMessageNoLookup(getNameKey(), e.getClass().getName() + " - " + e.getMessage());
			_isRunning = false;
			return null;
		}

		// Get results from handler, put in model
		WeatherResults results = new WeatherResults();
		results.setForecasts(xmlHandler.getForecasts());
		results.setLocationName(xmlHandler.getLocationName());
		results.setUpdateTime(xmlHandler.getUpdateTime());
		results.setTempsCelsius(inCelsius);
		return results;
	}

	/** Used for testing */
	void setStreamProvider(StreamProvider inProvider) {
		_streamProvider = inProvider;
	}
}
