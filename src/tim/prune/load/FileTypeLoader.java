package tim.prune.load;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.DeleteAllPointsCmd;
import tim.prune.cmd.RemoveCorrelatedMediaCmd;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.PointCreateOptions;
import tim.prune.data.SourceInfo;
import tim.prune.data.Checker.DoubleStatus;
import tim.prune.data.SourceInfo.FILE_TYPE;
import tim.prune.load.xml.XmlHandler;

/**
 * Superclass of all the type-specific file loaders
 */
public class FileTypeLoader
{
	/** App for callback of file loading */
	private final App _app;

	public FileTypeLoader(App inApp) {
		_app = inApp;
	}

	protected App getApp() {
		return _app;
	}

	/**
	 * Get option for append, replace or cancel
	 * @param inAutoAppend flag for autoappend without asking
	 * @return YES=append, NO=replace, CANCEL=cancel
	 */
	protected int getAppendOption(boolean inAutoAppend)
	{
		if (inAutoAppend || _app.getTrackInfo().getTrack().getNumPoints() == 0) {
			return JOptionPane.YES_OPTION;
		}
		return JOptionPane.showConfirmDialog(_app.getFrame(),
			I18nManager.getText("dialog.openappend.text"),
			I18nManager.getText("dialog.openappend.title"),
			JOptionPane.YES_NO_CANCEL_OPTION);
	}

	/**
	 * Subclasses call this method to create the command and execute it
	 * @param inPointList list of points created from data
	 * @param inSourceInfo information about the data source
	 * @param inAppend true to append, false to replace
	 */
	protected void loadData(List<DataPoint> inPointList, SourceInfo inSourceInfo, boolean inAppend)
	{
		// Set the source info on each of the created points
		int index = 0;
		for (DataPoint point : inPointList) {
			point.setSourceInfo(inSourceInfo);
			point.setOriginalIndex(index++);
		}
		if (inSourceInfo != null) {
			inSourceInfo.setNumPoints(inPointList.size());
		}
		final CompoundCommand command = new CompoundCommand(DataSubscriber.FILE_LOADED);
		if (inAppend) {
			command.addCommand(new AppendRangeCmd(inPointList));
		}
		else
		{
			command.addCommand(new RemoveCorrelatedMediaCmd())
				.addCommand(new DeleteAllPointsCmd())
				.addCommand(new AppendRangeCmd(inPointList));
		}
		final String undoDesc = (inSourceInfo == null ? I18nManager.getTextWithNumber("undo.loadpoints", inPointList.size())
			: I18nManager.getText("undo.loadfile", getFilename(inSourceInfo)));
		command.setDescription(undoDesc);
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsadded", inPointList.size()));
		if (_app.execute(command))
		{
			// Check for doubled track
			Checker.DoubleStatus doubleStatus = Checker.isDoubledTrack(inPointList);
			if (doubleStatus != DoubleStatus.NOTHING)
			{
				String keySuffix = (doubleStatus == DoubleStatus.DOUBLED_WAYPOINTS_TRACKPOINTS ? "contentsdoubled.wayandtrack" : "contentsdoubled");
				JOptionPane.showMessageDialog(_app.getFrame(), I18nManager.getText("dialog.open." + keySuffix),
					I18nManager.getText("function.open"), JOptionPane.WARNING_MESSAGE);
			}
			// Also manage recent files
			if (inSourceInfo != null)
			{
				boolean isRegularLoad = (inSourceInfo.getFileType() != FILE_TYPE.GPSBABEL);
				_app.addRecentFile(inSourceInfo.getFile(), isRegularLoad);
			}
		}
	}

	/**
	 * @return filename from the source info
	 */
	private String getFilename(SourceInfo inSourceInfo)
	{
		if (inSourceInfo == null || inSourceInfo.getFile() == null) {
			return "";
		}
		String name = inSourceInfo.getFile().getName();
		if (name.length() > 20) {
			return name.substring(0, 20) + "...";
		}
		return name;
	}

	/**
	 * Create a list of points from the loaded data
	 * @param inFields array of fields
	 * @param inData data strings, in order
	 * @param inOptions selected options like units
	 * @return list of created points
	 */
	protected List<DataPoint> createPoints(Field[] inFields, Object[][] inData,
		PointCreateOptions inOptions)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		boolean firstPoint = true;
		FieldList fields = new FieldList(inFields);
		for (Object[] objects : inData)
		{
			DataPoint point = new DataPoint((String[]) objects, fields, inOptions);
			if (point.isValid())
			{
				if (firstPoint) {
					point.setSegmentStart(true);
				}
				points.add(point);
				firstPoint = false;
			}
		}
		return points;
	}

	/**
	 * Load the data from the xml handler
	 * @param inHandler xml handler which read the data from GPSBabel
	 * @param inSourceInfo info about file (or not)
	 * @param inAutoAppend true to auto-append
	 * @param inMediaLinks media links, if any
	 */
	public void loadData(XmlHandler inHandler, SourceInfo inSourceInfo,
		boolean inAutoAppend, MediaLinkInfo inMediaLinks)
	{
		int appendOption = getAppendOption(inAutoAppend);
		if (appendOption == JOptionPane.CANCEL_OPTION) {
			return;
		}
		// give data to App
		List<DataPoint> points = createPoints(inHandler.getFieldArray(),
			inHandler.getDataArray(), null);
		loadData(points, inSourceInfo, appendOption == JOptionPane.YES_OPTION);
		if (inMediaLinks != null && inMediaLinks.getLinkArray() != null
				&& inMediaLinks.getLinkArray().length == points.size())
		{
			// Build list of linked media to be loaded afterwards
			ArrayList<ItemToLoad> items = new ArrayList<>();
			for (int i=0; i<points.size(); i++) {
				String link = inMediaLinks.getLinkArray()[i];
				if (link != null) {
					DataPoint point = points.get(i);
					if (looksLikeUrl(link)) {
						try {
							items.add(ItemToLoad.mediaUrl(new URL(link), point));
						} catch (MalformedURLException ignored) {}
					}
					else {
						items.add(ItemToLoad.archivedFile(inSourceInfo.getFile(), link, point));
					}
				}
			}
			_app.loadLinkedMedia(items);
		}
	}

	private static boolean looksLikeUrl(String inLink)
	{
		if (inLink == null) {
			return false;
		}
		String lower = inLink.toLowerCase();
		return lower.startsWith("http://") || lower.startsWith("https://");
	}
}
