package tim.prune.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import tim.prune.App;
import tim.prune.data.Field;
import tim.prune.data.SourceInfo;

/**
 * Class to handle the loading of Nmea files
 */
public class NmeaFileLoader
{
	/** App for callback of file loading */
	private App _app = null;

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public NmeaFileLoader(App inApp)
	{
		_app = inApp;
	}

	/**
	 * Open the selected file
	 * @param inFile File to open
	 */
	public void openFile(File inFile)
	{
		BufferedReader reader = null;
		ArrayList<NmeaMessage> messages = new ArrayList<NmeaMessage>();
		String lastDate = null;
		try
		{
			reader = new BufferedReader(new FileReader(inFile));
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
					else {
						String date = getDateFromRMC(currLine);
						if (date != null)
						{
							if (lastDate == null && !messages.isEmpty()) {
								// Backfill first few messages received before the first date
								for (int m=0; m<messages.size(); m++) {
									messages.get(m).setDate(date);
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
			_app.showErrorMessage("error.load.dialogtitle", "error.load.noread");
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
			_app.informDataLoaded(getFieldArray(), makeDataArray(messages),
				null, new SourceInfo(inFile, SourceInfo.FILE_TYPE.NMEA), null);
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
	private static Object[][] makeDataArray(ArrayList<NmeaMessage> inList)
	{
		Object[][] result = new Object[inList.size()][];
		for (int i=0; i<inList.size(); i++) {
			result[i] = inList.get(i).getStrings();
		}
		return result;
	}

	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
			Field.TIMESTAMP, Field.NEW_SEGMENT};
		return fields;
	}
}
