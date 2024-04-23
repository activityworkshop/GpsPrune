package tim.prune.load.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.SourceInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.load.FileToBeLoaded;
import tim.prune.load.FileTypeLoader;


/**
 * Class to handle the loading of GeoJSON files
 */
public class JsonFileLoader extends FileTypeLoader
{
	/** Stack of blocks */
	private final Stack<JsonBlock> _jsonBlocks;
	/** List of points extracted */
	private final ArrayList<JsonPoint> _jsonPoints;
	private boolean _newSegment = true;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public JsonFileLoader(App inApp)
	{
		super(inApp);
		_jsonBlocks = new Stack<JsonBlock>();
		_jsonPoints = new ArrayList<JsonPoint>();
	}

	/**
	 * Open the selected file
	 * @param inFileLock File to open
	 * @param inAutoAppend true to automatically append
	 */
	public void openFile(FileToBeLoaded inFileLock, boolean inAutoAppend)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(inFileLock.getFile())))
		{
			String currLine = reader.readLine();
			while (currLine != null)
			{
				processTokensInLine(currLine);
				// Read next line, if any
				currLine = reader.readLine();
			}
		}
		catch (IOException ioe) {
			getApp().showErrorMessage("error.load.dialogtitle", "error.load.noread");
		}

		if (!_jsonPoints.isEmpty())
		{
			int appendOption = getAppendOption(inAutoAppend);
			if (appendOption == JOptionPane.CANCEL_OPTION) {
				return;
			}
			loadData(makePointList(), new SourceInfo(inFileLock.getFile(), SourceInfo.FileType.JSON),
				appendOption == JOptionPane.YES_OPTION);
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
		if (inToken == null || inToken.isBlank()) {return;}
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
			for (int i=0; i<inBlock.getNumPoints(); i++) {
				_jsonPoints.add(inBlock.createPointFromList(i));
			}
			_newSegment = true;
		}
	}

	/**
	 * @return list of points to load
	 */
	private List<DataPoint> makePointList()
	{
		ArrayList<DataPoint> points = new ArrayList<>();
		for (JsonPoint jsonPoint : _jsonPoints)
		{
			DataPoint point = new DataPoint(Latitude.make(jsonPoint._latitude),
				Longitude.make(jsonPoint._longitude),
				new Altitude(jsonPoint._altitude, UnitSetLibrary.UNITS_METRES));
			if (point.isValid())
			{
				point.setSegmentStart(jsonPoint._newSegment);
				points.add(point);
			}
		}
		return points;
	}
}
