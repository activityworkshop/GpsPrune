package tim.prune.function.media;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendMediaCmd;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.MediaObject;
import tim.prune.data.UnitSetLibrary;
import tim.prune.load.ItemToLoad.BlockStatus;
import tim.prune.load.MediaHelper;

/**
 * Class to load a single media item (photo / audio)
 * and link it to the referenced point
 */
public class LinkedMediaLoader
{
	private final App _app;
	private final HashSet<String> _blockedDomains = new HashSet<>();
	private final HashSet<String> _allowedDomains = new HashSet<>();

	public LinkedMediaLoader(App inApp) {
		_app = inApp;
	}

	/**
	 * Load a media using a provided url
	 * @param inUrl url
	 * @param inPoint point to which this media will be attached
	 * @param inStatus possible block status already applied
	 */
	public void loadFromUrl(URL inUrl, DataPoint inPoint, BlockStatus inStatus)
	{
		processBlockStatus(inUrl, inStatus);
		if (isDomainAllowed(inUrl)) {
			loadMedia(MediaHelper.createMediaObjectFromUrl(inUrl), inPoint);
		}
		_app.informDataLoadComplete();
	}

	private void processBlockStatus(URL inUrl, BlockStatus inStatus)
	{
		if (inStatus == BlockStatus.NOT_ASKED || inStatus == BlockStatus.ASKED) {
			return;
		}
		String domain = inUrl.getHost();
		if (inStatus == BlockStatus.ALLOW) {
			_allowedDomains.add(domain);
		}
		else if (inStatus == BlockStatus.BLOCK) {
			_blockedDomains.add(domain);
		}
	}

	/**
	 * After the media object has been constructed, load it and attach it
	 * @param inMedia media object, or null if load failed
	 * @param inPoint data point to which it will be attached
	 */
	private void loadMedia(MediaObject inMedia, DataPoint inPoint)
	{
		if (inMedia == null) {
			return;
		}
		// Check if the media object has a point now (from exif)
		DataPoint exifPoint = inMedia.getDataPoint();
		final MediaObject.Status originalMediaStatus =
			(exifPoint == null ? MediaObject.Status.NOT_CONNECTED : MediaObject.Status.TAGGED);
		inMedia.setOriginalStatus(originalMediaStatus);
		MediaObject.Status currMediaStatus = MediaObject.Status.TAGGED;
		if (exifPoint != null)
		{
			final double distinMetres = Distance.convertRadiansToDistance(
				DataPoint.calculateRadiansBetween(exifPoint, inPoint), UnitSetLibrary.UNITS_METRES);
			if (distinMetres > 10.0) {
				currMediaStatus = MediaObject.Status.CONNECTED; // still connected but changed
			}
		}
		inMedia.setCurrentStatus(currMediaStatus);
		// Now create the command
		CompoundCommand command = new CompoundCommand()
				.addCommand(new AppendMediaCmd(List.of(inMedia)))
				.addCommand(new ConnectMediaCmd(inPoint, inMedia));
		command.setDescription(I18nManager.getText("undo.loadmedia", shortenName(inMedia.getName())));
		command.setConfirmText(I18nManager.getText("confirm.loadedmedia"));
		_app.execute(command);
	}

	private String shortenName(String inName)
	{
		if (inName == null) {
			return "";
		}
		if (inName.length() < 30) {
			return inName;
		}
		return "..." + inName.substring(inName.length() - 30);
	}

	/**
	 * Check whether the domain of the given URL is allowed or blocked
	 * @param inUrl entire Url for display in the question message if necessary
	 * @return true if allowed, false if blocked
	 */
	private boolean isDomainAllowed(URL inUrl)
	{
		if (inUrl == null) {
			return false;
		}
		final String domain = inUrl.getHost();
		if (_allowedDomains.contains(domain)) {
			return true;
		}
		if (_blockedDomains.contains(domain)) {
			return false;
		}
		Object[] buttonTexts = {I18nManager.getText("button.allow"), I18nManager.getText("button.block")};
		final String question = splitUrl(inUrl.toString()) + "\n\n" + I18nManager.getText("dialog.loadlinkedmedia.allowdomain", domain);
		int answer = JOptionPane.showOptionDialog(_app.getFrame(), question,
				I18nManager.getText("dialog.loadlinkedmedia.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1]);
		if (answer == JOptionPane.YES_OPTION) {
			_allowedDomains.add(domain);
		}
		if (answer == JOptionPane.NO_OPTION) {
			_blockedDomains.add(domain);
		}
		return answer == JOptionPane.YES_OPTION;
	}

	public boolean isDomainKnown(String inDomain) {
		return _allowedDomains.contains(inDomain) || _blockedDomains.contains(inDomain);
	}

	/**
	 * @param inUrl url which is perhaps too long for one line
	 * @return string with line breaks to make more readable in dialog
	 */
	static String splitUrl(String inUrl)
	{
		final int splitLength = 50;
		StringBuilder builder = new StringBuilder();
		String urlString = inUrl.trim();
		int startPos = 0;
		while ((startPos + splitLength) < urlString.length()) {
			builder.append(urlString.substring(startPos, startPos+splitLength));
			builder.append('\n');
			startPos += splitLength;
		}
		builder.append(urlString.substring(startPos));
		return builder.toString();
	}

	/**
	 * Load a media object from inside an archive, like a zip or a kmz
	 * @param inArchiveFile container file from which data was loaded
	 * @param inPath path to media within archive
	 * @param inPoint data point to which it should be attached
	 */
	public void loadFromArchive(File inArchiveFile, String inPath, DataPoint inPoint)
	{
		loadMedia(MediaHelper.createMediaObjectRelative(inArchiveFile, inPath, inArchiveFile), inPoint);
		_app.informDataLoadComplete();
	}
}
