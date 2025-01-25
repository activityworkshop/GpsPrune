package tim.prune.function.comparesegments;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Distance;
import tim.prune.data.Unit;
import tim.prune.java8.StringUtils;


/** Class to hold the gui elements for the data export for spreadsheets */
public class DataPanel extends JPanel
{
	private final JTextArea _distanceMatchesText;
	private final JTextArea _timeMatchesText;

	public DataPanel()
	{
		final String defaultText = StringUtils.repeat("Example text to give it some width to start with\n", 10);
		_distanceMatchesText = new JTextArea(defaultText);
		_timeMatchesText = new JTextArea(defaultText);

		setLayout(new GridLayout(2, 1, 2, 6));
		add(makeHalfPanel("distancematches", _distanceMatchesText));
		add(makeHalfPanel("timematches", _timeMatchesText));
	}

	private static JPanel makeHalfPanel(String inLabelSuffix, JTextArea inTextArea)
	{
		final JPanel halfPanel = new JPanel();
		halfPanel.setLayout(new BorderLayout(4, 2));
		final JLabel topLabel = new JLabel(I18nManager.getText("dialog.comparesegments.data." + inLabelSuffix));
		halfPanel.add(topLabel, BorderLayout.NORTH);
		final JScrollPane scroller = new JScrollPane(inTextArea);
		scroller.setPreferredSize(new Dimension(400, 150));
		halfPanel.add(scroller, BorderLayout.CENTER);
		return halfPanel;
	}

	public void setDetails(List<IntersectionResult> inResults, Config inConfig)
	{
		final Unit distUnit = inConfig.getUnitSet().getDistanceUnit();
		final Unit speedUnit = inConfig.getUnitSet().getSpeedUnit();
		_distanceMatchesText.setText(generateDistanceData(inResults, distUnit, speedUnit));
		_timeMatchesText.setText(generateTimeData(inResults, distUnit));
	}

	/**
	 * @return a String to show in the text area
	 */
	private String generateDistanceData(List<IntersectionResult> inResults,
		Unit inDistUnit, Unit inSpeedUnit)
	{
		StringBuilder text = new StringBuilder();
		text.append(I18nManager.getText("fieldname.distance"))
			.append(';')
			.append(I18nManager.getText("fieldname.time"))
			.append(';')
			.append(I18nManager.getText("fieldname.distance"))
			.append(';')
			.append(I18nManager.getText("fieldname.time"))
			.append(';')
			.append(I18nManager.getText("dialog.comparesegments.data.secsahead"))
			.append(';')
			.append(I18nManager.getText("dialog.comparesegments.data.speeddiff"))
			.append(" (")
			.append(I18nManager.getText(inSpeedUnit.getShortnameKey()))
			.append(");\n");
		IntersectionResult firstResult = null;
		for (IntersectionResult result : inResults)
		{
			final double firstDistRadians;
			final double secondDistRadians;
			final long firstSeconds;
			final long secondSeconds;
			final double speedDiffRadiansPerSec;
			if (firstResult == null)
			{
				firstResult = result;
				firstDistRadians = secondDistRadians = 0.0;
				firstSeconds = secondSeconds = 0L;
				speedDiffRadiansPerSec = 0.0;
			}
			else
			{
				firstDistRadians = result.getFirstDistanceRadians(firstResult);
				secondDistRadians = result.getSecondDistanceRadians(firstResult);
				firstSeconds = result.getFirstDurationSeconds(firstResult);
				secondSeconds = result.getSecondDurationSeconds(firstResult);
				speedDiffRadiansPerSec = result.getDeltaSpeedRadiansPerSec();
			}
			final double speedDiffUnitsPerHour = Distance.convertRadiansToDistance(speedDiffRadiansPerSec, inDistUnit)
				* 60.0 * 60.0;
			text.append(Distance.convertRadiansToDistance(firstDistRadians, inDistUnit))
				.append(';')
				.append(firstSeconds)
				.append(';')
				.append(Distance.convertRadiansToDistance(secondDistRadians, inDistUnit))
				.append(';')
				.append(secondSeconds)
				.append(';')
				.append(firstSeconds - secondSeconds)
				.append(';')
				.append(speedDiffUnitsPerHour)
				.append(";\n");
		}
		return text.toString();
	}

	private String generateTimeData(List<IntersectionResult> inResults, Unit inDistUnit)
	{
		StringBuilder text = new StringBuilder();
		text.append(I18nManager.getText("fieldname.distance"))
			.append(';')
			.append(I18nManager.getText("fieldname.time"))
			.append(';')
			.append(I18nManager.getText("fieldname.distance"))
			.append(';')
			.append(I18nManager.getText("dialog.comparesegments.data.distahead"))
			.append(" (")
			.append(I18nManager.getText(inDistUnit.getShortnameKey()))
			.append(");\n");
		IntersectionResult firstResult = null;
		for (IntersectionResult result : inResults)
		{
			final double firstDistRadians;
			final Double secondDistRadians;
			final long firstSeconds;

			if (firstResult == null)
			{
				firstResult = result;
				firstDistRadians = secondDistRadians = 0.0;
				firstSeconds = 0L;
			}
			else
			{
				firstDistRadians = result.getFirstDistanceRadians(firstResult);
				firstSeconds = result.getFirstDurationSeconds(firstResult);
				secondDistRadians = TimeCalculations.findSecondDistanceAtSameTime(inResults, firstSeconds);
			}
			if (secondDistRadians != null)
			{
				text.append(Distance.convertRadiansToDistance(firstDistRadians, inDistUnit))
					.append(';')
					.append(firstSeconds)
					.append(';')
					.append(Distance.convertRadiansToDistance(secondDistRadians, inDistUnit))
					.append(';')
					.append(Distance.convertRadiansToDistance(secondDistRadians - firstDistRadians, inDistUnit))
					.append(";\n");
			}
		}
		return text.toString();
	}
}
