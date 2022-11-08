package tim.prune.load.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import tim.prune.App;
import tim.prune.data.Field;
import tim.prune.data.SourceInfo;


/**
 * Class to handle the loading of GeoJSON files
 */
public class JsonFileLoader
{
	/** App for callback of file loading */
	private App _app = null;
	/** Stack of blocks */
	private Stack<JsonBlock> _jsonBlocks = null;
	/** List of points extracted */
	private ArrayList<JsonPoint> _jsonPoints = null;
	private boolean _newSegment = true;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public JsonFileLoader(App inApp)
	{
		_app = inApp;
		_jsonBlocks = new Stack<JsonBlock>();
		_jsonPoints = new ArrayList<JsonPoint>();
	}

	/**
	 * Open the selected file
	 * @param inFile File to open
	 */
	public void openFile(File inFile)
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(inFile));
			String currLine = reader.readLine();
			while (currLine != null)
			{
				processTokensInLine(currLine);
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
		if (_jsonPoints.size() > 0)
		{
			Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
				Field.NEW_SEGMENT};
			_app.informDataLoaded(fields, makeDataArray(),
				null, new SourceInfo(inFile, SourceInfo.FILE_TYPE.JSON), null);
		}
		// TODO: Show message if nothing was found?
	}

	/** Split the given line from the json into tokens
	 *  and process them one by one */
	private void processTokensInLine(String inLine)
	{
		if (inLine == null) {return;}
		String line = inLine.trim();
		StringBuilder currToken = new StringBuilder();
		boolean insideQuotes = false;
		boolean previousSlash = false;
		for (char x : line.toCharArray())
		{
			if (insideQuotes || x=='"') {
				currToken.append(x);
			}
			else
			{
				if (" :,".indexOf(x) >= 0) {
					processToken(currToken.toString());
					currToken.setLength(0);
				}
				else if ("[{".indexOf(x) >= 0) {
					// start of a new block
					_jsonBlocks.add(new JsonBlock());
				}
				else if ("]}".indexOf(x) >= 0)
				{
					processToken(currToken.toString());
					currToken.setLength(0);
					// end of the current block
					processBlock(_jsonBlocks.pop());
				}
				else {
					currToken.append(x);
				}
			}
			if (x == '"' && !previousSlash) {insideQuotes = !insideQuotes;}
			previousSlash = (x == '\\') && !previousSlash;
		}
		processToken(currToken.toString());
	}

	private void processToken(String inToken)
	{
		if (inToken == null || inToken.equals("")) {return;}
		if (inToken.equals("\"coordinates\"")) {
			_newSegment = true;
		}
		_jsonBlocks.peek().addToken(inToken);
	}

	/** Process the end of the given block */
	private void processBlock(JsonBlock inBlock)
	{
		if (inBlock.areFieldsValid())
		{
			_jsonPoints.add(inBlock.createSinglePoint(_newSegment));
			_newSegment = false;
		}
		else if (inBlock.areSingleCoordsValid())
		{
			// block contains a single point - pass to parent list
			_jsonBlocks.peek().addSingleCoordsFrom(inBlock);
		}
		else if (inBlock.isCoordListValid())
		{
			// block contains a list of point coords
			_jsonBlocks.peek().addCoordListFrom(inBlock);
		}
		else if (inBlock.hasValidCoordList())
		{
			for (int i=0; i<inBlock.getNumPoints(); i++)
			{
				_jsonPoints.add(inBlock.createPointFromList(i));
			}
			_newSegment = true;
		}
	}

	/**
	 * Make an object array from the data list
	 * @return object array for loading
	 */
	private Object[][] makeDataArray()
	{
		Object[][] result = new Object[_jsonPoints.size()][];
		for (int i=0; i<_jsonPoints.size(); i++) {
			JsonPoint point = _jsonPoints.get(i);
			result[i] = new String[] {point._latitude, point._longitude, point._altitude, point._newSegment?"1":"0"};
		}
		return result;
	}
}
