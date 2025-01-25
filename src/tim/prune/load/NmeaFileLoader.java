package tim.prune.load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.FileType;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.SourceInfo;
import tim.prune.data.UnitSetLibrary;

/**
 * Class to handle the loading of Nmea files
 */
public class NmeaFileLoader extends FileTypeLoader
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public NmeaFileLoader(App inApp) {
		super(inApp);
	}

	/**
	 * Open the selected file
	 * @param inFileLock File to open
	 * @param inAutoAppend true to automatically append
	 */
	public void openFile(FileToBeLoaded inFileLock, boolean inAutoAppend)
	{
		BufferedReader reader = null;
		ArrayList<NmeaMessage> messages = new ArrayList<NmeaMessage>();
		String lastDate = null;
		try
		{
			reader = new BufferedReader(new FileReader(inFileLock.getFile()));
			String currLine = reader.readLine();
			boolean newSegment = true;
			while (currLine != null)
			{
				// Try to make an NmeaMessage object for each line of file
				if (currLine.trim().length() > 0)
				{
					NmeaMessage message = processGGA(currLine);
					if (message != null)
					{
						if (message.hasFix())
						{
							message.setSegment(newSegment);
							message.setDate(lastDate);
							// add message to list
							messages.add(message);
						}
						// Start a new segment if fix lost
						newSegment = !message.hasFix();
					}
					else
					{
						String date = getDateFromRMC(currLine);
						if (date != null)
						{
							if (lastDate == null && !messages.isEmpty()) {
								// Backfill first few messages received before the first date
								for (NmeaMessage msg : messages) {
									msg.setDate(date);
								}
							}
							lastDate = date;
						}
					}
				}
				// Read next line, if any
				currLine = reader.readLine();
			}
		}
		catch (IOException ioe) {
			getApp().showErrorMessage("error.load.dialogtitle", "error.load.noread");
		}
		finally
		{
			// close file ignoring errors
			try {
				if (reader != null) reader.close();
			}
			catch (Exception e) {}
		}
		if (messages.size() > 0)
		{
			int appendOption = getAppendOption(inAutoAppend);
			if (appendOption == JOptionPane.CANCEL_OPTION) {
				return;
			}
			loadData(makePointList(messages), new SourceInfo(inFileLock.getFile(), FileType.NMEA),
				appendOption == JOptionPane.YES_OPTION);
		}
	}

	/**
	 * Process the given GGA sentence and return the message
	 * @param inLine line to process
	 * @return message object
	 */
	private static NmeaMessage processGGA(String inLine)
	{
		// Only consider lines which are long enough and begin with the GPS position sentence
		if (inLine == null || inLine.length() < 20 || !inLine.startsWith("$GPGGA")) {
			return null;
		}
		// Assume comma delimiter, split into array
		String[] splitLine = inLine.split(",");
		if (splitLine != null && splitLine.length >= 10)
		{
			return new NmeaMessage(splitLine[2] + splitLine[3], // latitude
				splitLine[4] + splitLine[5], // longitude
				splitLine[9], // altitude
				splitLine[1], // timestamp
				splitLine[6]); // fix
		}
		// Couldn't parse it, return null
		return null;
	}

	/**
	 * Process the given MRC sentence and return the date
	 * @param inLine line to process
	 * @return date, if any
	 */
	private static String getDateFromRMC(String inLine)
	{
		// Only consider lines which are long enough and begin with the RMC sentence
		if (inLine == null || inLine.length() < 20 || !inLine.startsWith("$GPRMC")) {
			return null;
		}
		// Assume comma delimiter, split into array
		String[] splitLine = inLine.split(",");
		if (splitLine != null && splitLine.length >= 10)
		{
			return splitLine[9]; // date in position 9
		}
		// Couldn't parse it, return null
		return null;
	}

	/**
	 * Make an object array from the data list
	 * @param inList list of messages
	 * @return object array for loading
	 */
	private List<DataPoint> makePointList(ArrayList<NmeaMessage> inList)
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		for (NmeaMessage nmea : inList)
		{
			DataPoint point = new DataPoint(Latitude.make(nmea.getLatitude()),
				Longitude.make(nmea.getLongitude()),
				new Altitude(nmea.getAltitude(), UnitSetLibrary.UNITS_METRES));
			point.setSegmentStart(nmea.getSegmentFlag());
			points.add(point);
		}
		return points;
	}
}
