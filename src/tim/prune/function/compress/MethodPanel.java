package tim.prune.function.compress;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Distance;
import tim.prune.data.NumberUtils;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.methods.*;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.WholeNumberField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** Gui component controlling a single method within the list */
public class MethodPanel extends JPanel
{
	private int _index;
	private final MethodPanelContainer _parent;
	private final Config _config;
	private final JCheckBox _activeCheckbox;
	private final JComboBox<String> _methodCombo;
	private final WholeNumberField _nearbySpanField = new WholeNumberField(5);
	private final WholeNumberField _douglasSpanField = new WholeNumberField(5);
	private final DecimalNumberField _wackyFactorField = new DecimalNumberField();
	private final DecimalNumberField _singletonFactorField = new DecimalNumberField();
	private final DecimalNumberField _speedLowLimitField = new DecimalNumberField();
	private final DecimalNumberField _speedHighLimitField = new DecimalNumberField();
	private final DecimalNumberField _distanceLimitField = new DecimalNumberField();
	private final WholeNumberField _timeLimitField = new WholeNumberField(5);
	private final JPanel _cardPanel = new JPanel();
	private final CardLayout _cards = new CardLayout();
	private final JLabel _numToDeleteLabel;
	private JButton _actionButton = null;
	private CompressionMethod _compressionMethod = null;


	/** Inner class for exchanging values */
	private static class ValueExchanger
	{
		private final boolean _active;
		private final int _comboIndex, _intFactor;
		private final double _doubleFactor;
		private final CompressionMethodType _type;

		private ValueExchanger() {
			this(true, 0, CompressionMethodType.NONE, 0);
		}

		private ValueExchanger(boolean isActive, int inIndex, CompressionMethodType inType, int inFactor)
		{
			_active = isActive;
			_comboIndex = inIndex;
			_type = inType;
			_intFactor = inFactor;
			_doubleFactor = 0.0;
		}

		private ValueExchanger(boolean isActive, int inIndex, CompressionMethodType inType, double inFactor)
		{
			_active = isActive;
			_comboIndex = inIndex;
			_type = inType;
			_intFactor = 0;
			_doubleFactor = inFactor;
		}
	}

	/** Constructor */
	public MethodPanel(int inIndex, MethodPanelContainer inParent, ParameterValues inValues, Config inConfig)
	{
		_index = inIndex;
		_parent = inParent;
		_config = inConfig;
		_activeCheckbox = new JCheckBox();
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(2, 0, 2, 0),
				BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		));
		add(_activeCheckbox, BorderLayout.WEST);
		_actionButton = new JButton("...");
		_actionButton.addActionListener(e -> showPopup());
		add(makeButtonHolder(_actionButton), BorderLayout.EAST);
		_methodCombo = new JComboBox<>(getMethodNames());
		_numToDeleteLabel = new JLabel("text to go here");
		_cardPanel.setLayout(_cards);
		populateCards(inValues);
		setupListeners();
		add(combineIntoPanel(_methodCombo, _cardPanel, _numToDeleteLabel), BorderLayout.CENTER);
		showNumDeleted(0);
	}

	private void showPopup()
	{
		JPopupMenu popup = new JPopupMenu();
		JMenuItem moveUpItem = new JMenuItem("Move up");
		moveUpItem.addActionListener(e -> _parent.movePanelUp(_index));
		popup.add(moveUpItem);
		JMenuItem moveDownItem = new JMenuItem("Move down");
		moveDownItem.addActionListener(e -> _parent.movePanelDown(_index));
		popup.add(moveDownItem);
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(e -> _parent.deletePanel(_index));
		popup.add(deleteItem);
		popup.show(_actionButton, 20, 15);
	}

	private void populateCards(ParameterValues inValues)
	{
		// Card 0 - No method selected
		_cardPanel.add(new JPanel(), CompressionMethodType.NONE.toString());
		// Card 1 - Duplicates, no parameter needed
		_cardPanel.add(new JPanel(), CompressionMethodType.DUPLICATES.toString());
		// Card 2 - Nearby with factor
		final JPanel nearSpanPanel = new JPanel();
		nearSpanPanel.setLayout(new FlowLayout());
		nearSpanPanel.add(makeParamLabel("closepoints"));
		nearSpanPanel.add(_nearbySpanField);
		_nearbySpanField.setValue(getIntValue(inValues, CompressionMethodType.NEARBY_WITH_FACTOR));
		_cardPanel.add(nearSpanPanel, CompressionMethodType.NEARBY_WITH_FACTOR.toString());
		// Card 3 - Wacky points
		final JPanel wackyPanel = new JPanel();
		wackyPanel.setLayout(new FlowLayout());
		wackyPanel.add(makeParamLabel("wackypoints"));
		wackyPanel.add(_wackyFactorField);
		_wackyFactorField.setValue(getDoubleValue(inValues, CompressionMethodType.WACKY_POINTS));
		_cardPanel.add(wackyPanel, CompressionMethodType.WACKY_POINTS.toString());
		// Card 4 - Singleton
		final JPanel singletonPanel = new JPanel();
		singletonPanel.setLayout(new FlowLayout());
		singletonPanel.add(makeParamLabel("singletons"));
		singletonPanel.add(_singletonFactorField);
		_singletonFactorField.setValue(getDoubleValue(inValues, CompressionMethodType.SINGLETONS));
		_cardPanel.add(singletonPanel, CompressionMethodType.SINGLETONS.toString());
		// Card 5 - Douglas-Peucker
		final JPanel douglasPanel = new JPanel();
		douglasPanel.setLayout(new FlowLayout());
		douglasPanel.add(makeParamLabel("douglaspeucker"));
		douglasPanel.add(_douglasSpanField);
		_douglasSpanField.setValue(getIntValue(inValues, CompressionMethodType.DOUGLAS_PEUCKER));
		_cardPanel.add(douglasPanel, CompressionMethodType.DOUGLAS_PEUCKER.toString());
		// Card 6 - Speed limit too slow
		final JPanel tooSlowPanel = new JPanel();
		tooSlowPanel.setLayout(new FlowLayout());
		final String speedUnits = I18nManager.getText(getSpeedLimitUnits().getShortnameKey());
		tooSlowPanel.add(makeParamUnitsLabel("speedlimit", speedUnits));
		tooSlowPanel.add(_speedLowLimitField);
		double metricSpeed = getDoubleValue(inValues, CompressionMethodType.TOO_SLOW);
		_speedLowLimitField.setValue(convertSpeedToLocal(metricSpeed));
		_cardPanel.add(tooSlowPanel, CompressionMethodType.TOO_SLOW.toString());
		// Card 7 - Speed limit too fast
		final JPanel tooFastPanel = new JPanel();
		tooFastPanel.setLayout(new FlowLayout());
		tooFastPanel.add(makeParamUnitsLabel("speedlimit", speedUnits));
		tooFastPanel.add(_speedHighLimitField);
		metricSpeed = getDoubleValue(inValues, CompressionMethodType.TOO_FAST);
		_speedHighLimitField.setValue(convertSpeedToLocal(metricSpeed));
		_cardPanel.add(tooFastPanel, CompressionMethodType.TOO_FAST.toString());
		// Card 8 - Time limit
		final JPanel timePanel = new JPanel();
		timePanel.setLayout(new FlowLayout());
		timePanel.add(makeParamLabel("timedifference"));
		timePanel.add(_timeLimitField);
		_timeLimitField.setValue(getIntValue(inValues, CompressionMethodType.TIME_DIFFERENCE));
		_cardPanel.add(timePanel, CompressionMethodType.TIME_DIFFERENCE.toString());
		// Card 9 - Distance limit
		final JPanel distancePanel = new JPanel();
		distancePanel.setLayout(new FlowLayout());
		final String distUnits = I18nManager.getText(getDistanceLimitUnits().getShortnameKey());
		distancePanel.add(makeParamUnitsLabel("distancelimit", distUnits));
		distancePanel.add(_distanceLimitField);
		final double metreLimit = getDoubleValue(inValues, CompressionMethodType.NEARBY_WITH_DISTANCE);
		_distanceLimitField.setValue(convertDistanceToLocal(metreLimit));
		_cardPanel.add(distancePanel, CompressionMethodType.NEARBY_WITH_DISTANCE.toString());
		// Card 10 - Ski lifts
		_cardPanel.add(new JPanel(), CompressionMethodType.SKI_LIFTS.toString());
	}

	private static JLabel makeParamLabel(String inMethodName) {
		return new JLabel(I18nManager.getText("dialog.compress." + inMethodName + ".paramdesc") + ":");
	}

	private static JLabel makeParamUnitsLabel(String inMethodName, String inUnits) {
		return new JLabel(I18nManager.getText("dialog.compress." + inMethodName + ".paramdesc") + " (" + inUnits + "):");
	}

	private Unit getDistanceLimitUnits() {
		return isSystemMetric() ? UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET;
	}

	private Unit getSpeedLimitUnits() {
		return isSystemMetric() ? UnitSetLibrary.SPEED_UNITS_KMPERHOUR : UnitSetLibrary.SPEED_UNITS_MILESPERHOUR;
	}

	private int getIntValue(ParameterValues inValues, CompressionMethodType inType)
	{
		int value = inValues == null ? 0 : NumberUtils.getIntOrZero(inValues.getValue(inType));
		return value == 0 ? (int) getDoubleValue(inValues, inType) : value;
	}

	private double getDoubleValue(ParameterValues inValues, CompressionMethodType inType) {
		return inValues == null ? 0.0 : NumberUtils.getDoubleOrZero(inValues.getValue(inType));
	}

	private void setupListeners()
	{
		_methodCombo.addActionListener(e -> showCard());
		_activeCheckbox.addItemListener(e -> showLabel());
		final JTextField[] editFields = new JTextField[] {
			_nearbySpanField, _douglasSpanField, _wackyFactorField, _singletonFactorField,
			_speedLowLimitField, _speedHighLimitField, _distanceLimitField, _timeLimitField
		};
		final KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {triggerRefresh();}
		};
		for (JTextField editField : editFields) {
			editField.addKeyListener(keyListener);
		}
	}

	private static JPanel combineIntoPanel(JComboBox<String> inComboBox, JPanel inCardPanel, JLabel inSummaryLabel)
	{
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(inComboBox, BorderLayout.WEST);
		panel.add(inCardPanel, BorderLayout.CENTER);
		panel.add(inSummaryLabel, BorderLayout.SOUTH);
		return panel;
	}

	private static String[] getMethodNames()
	{
		String[] keys = {"none", "duplicates", "closepoints", "wacky", "singletons",
			"douglaspeucker", "distancelimit", "timelimit", "tooslow", "toofast", "skilifts"};
		String[] texts = new String[keys.length];
		for (int i=0; i<keys.length; i++) {
			texts[i] = I18nManager.getText("dialog.compress.method." + keys[i]);
		}
		return texts;
	}

	private static JPanel makeButtonHolder(JButton inButton)
	{
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalGlue());
		panel.add(inButton);
		panel.add(Box.createVerticalGlue());
		return panel;
	}

	/** Needs to be updated if the elements in the combobox change! */
	private CompressionMethodType getSelectedMethod()
	{
		CompressionMethodType[] types = new CompressionMethodType[] {
			CompressionMethodType.NONE, CompressionMethodType.DUPLICATES,
			CompressionMethodType.NEARBY_WITH_FACTOR, CompressionMethodType.WACKY_POINTS,
			CompressionMethodType.SINGLETONS, CompressionMethodType.DOUGLAS_PEUCKER,
			CompressionMethodType.NEARBY_WITH_DISTANCE, CompressionMethodType.TIME_DIFFERENCE,
			CompressionMethodType.TOO_SLOW, CompressionMethodType.TOO_FAST,
			CompressionMethodType.SKI_LIFTS};
		return types[_methodCombo.getSelectedIndex()];
	}

	private ValueExchanger getValues()
	{
		CompressionMethodType selectedMethod = getSelectedMethod();
		final boolean isActive = _activeCheckbox.isSelected();
		switch (selectedMethod)
		{
			case DUPLICATES: return new ValueExchanger(isActive, 1, selectedMethod, 0);
			case NEARBY_WITH_FACTOR: return new ValueExchanger(isActive, 2, selectedMethod, _nearbySpanField.getValue());
			case WACKY_POINTS: return new ValueExchanger(isActive, 3, selectedMethod, _wackyFactorField.getValue());
			case SINGLETONS: return new ValueExchanger(isActive, 4, selectedMethod, _singletonFactorField.getValue());
			case DOUGLAS_PEUCKER: return new ValueExchanger(isActive, 5, selectedMethod, _douglasSpanField.getValue());
			case NEARBY_WITH_DISTANCE: return new ValueExchanger(isActive, 6, selectedMethod, _distanceLimitField.getValue());
			case TIME_DIFFERENCE: return new ValueExchanger(isActive, 7, selectedMethod, _timeLimitField.getValue());
			case TOO_SLOW: return new ValueExchanger(isActive, 8, selectedMethod, _speedLowLimitField.getValue());
			case TOO_FAST: return new ValueExchanger(isActive, 9, selectedMethod, _speedHighLimitField.getValue());
			case SKI_LIFTS: return new ValueExchanger(isActive, 10, selectedMethod, 0);
			case NONE:
			default:
				return new ValueExchanger();
		}
	}

	private void applyValues(ValueExchanger inValues)
	{
		switch (inValues._type)
		{
			case NEARBY_WITH_FACTOR:
				_nearbySpanField.setValue(inValues._intFactor);
				break;
			case DOUGLAS_PEUCKER:
				_douglasSpanField.setValue(inValues._intFactor);
				break;
			case WACKY_POINTS:
				_wackyFactorField.setValue(inValues._doubleFactor);
				break;
			case SINGLETONS:
				_singletonFactorField.setValue(inValues._doubleFactor);
				break;
			case NEARBY_WITH_DISTANCE:
				_distanceLimitField.setValue(inValues._doubleFactor);
				break;
			case TIME_DIFFERENCE:
				_timeLimitField.setValue(inValues._intFactor);
				break;
			case TOO_SLOW:
				_speedLowLimitField.setValue(inValues._doubleFactor);
				break;
			case TOO_FAST:
				_speedHighLimitField.setValue(inValues._doubleFactor);
				break;
			case DUPLICATES:
			case SKI_LIFTS:
			case NONE:
			default:
				break;
		}
		_activeCheckbox.setSelected(inValues._active);
		_methodCombo.setSelectedIndex(inValues._comboIndex);
		showCard();
	}

	public void setMethod(CompressionMethod inMethod)
	{
		int comboIndex = 0;
		switch (inMethod.getType())
		{
			case DUPLICATES:
				comboIndex = 1;
				break;
			case NEARBY_WITH_FACTOR:
				comboIndex = 2;
				_nearbySpanField.setValue(NumberUtils.getIntOrZero(inMethod.getParam()));
				break;
			case WACKY_POINTS:
				comboIndex = 3;
				_wackyFactorField.setValue(NumberUtils.getDoubleOrZero(inMethod.getParam()));
				break;
			case SINGLETONS:
				comboIndex = 4;
				_singletonFactorField.setValue(NumberUtils.getDoubleOrZero(inMethod.getParam()));
				break;
			case DOUGLAS_PEUCKER:
				comboIndex = 5;
				_douglasSpanField.setValue(NumberUtils.getIntOrZero(inMethod.getParam()));
				break;
			case NEARBY_WITH_DISTANCE:
			{
				comboIndex = 6;
				final double localDistance = convertDistanceToLocal(NumberUtils.getDoubleOrZero(inMethod.getParam()));
				_distanceLimitField.setValue(localDistance);
				break;
			}
			case TIME_DIFFERENCE:
				comboIndex = 7;
				_timeLimitField.setValue(NumberUtils.getIntOrZero(inMethod.getParam()));
				break;
			case TOO_SLOW:
			{
				comboIndex = 8;
				final double localSpeed = convertSpeedToLocal(NumberUtils.getDoubleOrZero(inMethod.getParam()));
				_speedLowLimitField.setValue(localSpeed);
				break;
			}
			case TOO_FAST:
			{
				comboIndex = 9;
				final double localSpeed = convertSpeedToLocal(NumberUtils.getDoubleOrZero(inMethod.getParam()));
				_speedHighLimitField.setValue(localSpeed);
				break;
			}
			case SKI_LIFTS:
				comboIndex = 10;
				break;
			default:
				break;
		}
		_activeCheckbox.setSelected(inMethod.isActive());
		_methodCombo.setSelectedIndex(comboIndex);
		_numToDeleteLabel.setVisible(false);
		showCard();
	}

	/** @return the given number of metres either in metres or in feet depending on the config */
	private double convertDistanceToLocal(double inMetricValue)
	{
		final double numMetres = Math.abs(inMetricValue);
		if (isSystemMetric()) {
			return numMetres;
		}
		return Distance.convertBetweenUnits(numMetres, UnitSetLibrary.UNITS_METRES, UnitSetLibrary.UNITS_FEET);
	}

	/** @return the given speed either in km/h or in miles/h depending on the config */
	private double convertSpeedToLocal(double inMetricValue)
	{
		final double kmPerHour = Math.abs(inMetricValue);
		if (isSystemMetric()) {
			return kmPerHour;
		}
		// We're not in metric system, so need to convert from km-per-hour to miles-per-hour
		return Distance.convertBetweenUnits(kmPerHour, UnitSetLibrary.UNITS_KILOMETRES, UnitSetLibrary.UNITS_MILES);
	}

	private void showCard()
	{
		CompressionMethodType method = this.getSelectedMethod();
		_cards.show(_cardPanel, method.toString());
		triggerRefresh();
	}

	private void showLabel()
	{
		_numToDeleteLabel.setVisible(_activeCheckbox.isSelected());
		triggerRefresh();
	}

	void refresh() {
		showLabel();
	}

	private void triggerRefresh()
	{
		_compressionMethod = null;
		if (_parent != null) {
			_parent.recalculateAll();
		}
	}

	boolean hasMethodSelected() {
		return _methodCombo.getSelectedIndex() > 0;
	}

	public static void swapPanels(MethodPanel inPanel1, MethodPanel inPanel2)
	{
		if (inPanel1 == null || inPanel2 == null || inPanel1 == inPanel2) {
			return;
		}
		ValueExchanger values1 = inPanel1.getValues();
		ValueExchanger values2 = inPanel2.getValues();
		inPanel1.applyValues(values2);
		inPanel2.applyValues(values1);
	}

	CompressionMethod getCompressionMethod()
	{
		if (_compressionMethod == null) {
			_compressionMethod = createCompressionMethodFromGui();
		}
		if (_compressionMethod != null) {
			_compressionMethod.setActive(_activeCheckbox.isSelected());
		}
		return _compressionMethod;
	}

	private CompressionMethod createCompressionMethodFromGui()
	{
		switch (getSelectedMethod())
		{
			case DUPLICATES:
				return new DuplicatesMethod();
			case NEARBY_WITH_FACTOR:
				return new NearbyFactorMethod(_nearbySpanField.getValue());
			case DOUGLAS_PEUCKER:
				return new DouglasPeuckerMethod(_douglasSpanField.getValue());
			case WACKY_POINTS:
				return new WackyPointsMethod(_wackyFactorField.getValue());
			case SINGLETONS:
				return new SingletonsMethod(_singletonFactorField.getValue());
			case NEARBY_WITH_DISTANCE:
				return new NearbyDistMethod(getDistanceInMetres());
			case TIME_DIFFERENCE:
				return new TooSoonMethod(_timeLimitField.getValue());
			case TOO_SLOW:
				return new TooSlowMethod(getSpeedLimitMetric(_speedLowLimitField.getValue()));
			case TOO_FAST:
				return new TooFastMethod(getSpeedLimitMetric(_speedHighLimitField.getValue()));
			case SKI_LIFTS:
				return new SkiLiftsMethod();
			case NONE:
			default:
				return null;
		}
	}

	/** @return the value entered in the distance limit but converted into metres */
	private double getDistanceInMetres()
	{
		final double localDistance = Math.abs(_distanceLimitField.getValue());
		if (isSystemMetric()) {
			return localDistance;
		}
		// We're not in metric system, so we need to convert from feet to metres
		return Distance.convertBetweenUnits(localDistance, UnitSetLibrary.UNITS_FEET, UnitSetLibrary.UNITS_METRES);
	}

	private double getSpeedLimitMetric(double inLocalValue)
	{
		if (isSystemMetric()) {
			return inLocalValue;
		}
		// We're not in metric system, so we need to convert from miles-per-hour to kilometres-per-hour
		return Distance.convertBetweenUnits(inLocalValue, UnitSetLibrary.UNITS_MILES, UnitSetLibrary.UNITS_KILOMETRES);
	}

	public void showNumDeleted(int numDeletedByThisMethod) {
		_numToDeleteLabel.setText(I18nManager.getText("dialog.compress.panel.deletes") + ": " + numDeletedByThisMethod);
	}

	private boolean isSystemMetric() {
		return _config == null || _config.getUnitSet().isMetric();
	}
}
