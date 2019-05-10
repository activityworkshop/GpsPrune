package tim.prune;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.TransferHandler;

/**
 * Class to listen for drag/drop events and react to dropping files on to the application
 */
public class FileDropHandler extends TransferHandler
{
	/** App object for passing results back to */
	private App _app = null;

	/** Fixed flavour in case the java file list flavour isn't available */
	private static DataFlavor _uriListFlavour = null;

	/** Static block to initialise the list flavour */
	static
	{
		try {_uriListFlavour = new DataFlavor("text/uri-list;class=java.lang.String");
		} catch (ClassNotFoundException nfe) {}
	}


	/**
	 * Constructor
	 * @param inApp App object to pass results of drop back to
	 */
	public FileDropHandler(App inApp)
	{
		_app = inApp;
	}

	/**
	 * Check if the object being dragged can be accepted
	 * @param inSupport object to check
	 */
	public boolean canImport(TransferSupport inSupport)
	{
		boolean retval = inSupport.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
			|| inSupport.isDataFlavorSupported(_uriListFlavour);
		// Modify icon to show a copy, not a move (+ icon on cursor)
		if (retval) {
			inSupport.setDropAction(COPY);
		}
		return retval;
	}

	/**
	 * Accept the incoming data and pass it on to the App
	 * @param inSupport contents of drop
	 */
	public boolean importData(TransferSupport inSupport)
	{
		if (!canImport(inSupport)) {return false;} // not allowed

		boolean success = false;
		ArrayList<File> dataFiles = new ArrayList<File>();

		// Try a java file list flavour first
		try
		{
			@SuppressWarnings("unchecked")
			java.util.List<File> fileList = (java.util.List<File>)
			inSupport.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			success = true;

			for (File f : fileList)
			{
				if (f != null && f.exists())
				{
					if (f.isDirectory()) {
						addDirectoryToList(f, dataFiles);
					}
					else if (f.isFile()) {
						dataFiles.add(f);
					}
				}
			}
		} catch (Exception e) {}  // exception caught, probably missing a javafilelist flavour - just continue

		// If that didn't work, try a list of strings instead
		if (!success)
		{
			try
			{
				String pathList = inSupport.getTransferable().getTransferData(_uriListFlavour).toString();
				success = true;

				for (String s : pathList.split("[\n\r]+"))
				{
					if (s != null && !s.equals(""))
					{
						File f = new File(new URI(s));
						if (f.exists())
						{
							if (f.isDirectory()) {
								addDirectoryToList(f, dataFiles);
							}
							else if (f.isFile()) {
								dataFiles.add(f);
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println("exception: " + e.getClass().getName() + " - " + e.getMessage());
				return false;
			}
		}

		// Pass files back to app
		if (!dataFiles.isEmpty()) {
			_app.loadDataFiles(dataFiles);
		}
		return true;
	}


	/**
	 * Recursively-called method to add files from the given directory (and its subdirectories)
	 * to the given list of files
	 * @param inDir directory to add
	 * @param inList file list to append to
	 */
	private void addDirectoryToList(File inDir, ArrayList<File> inList)
	{
		if (inDir != null && inDir.exists() && inDir.canRead() && inDir.isDirectory() && inList != null)
		{
			for (String path : inDir.list())
			{
				if (path != null)
				{
					File f = new File(inDir, path);
					if (f.exists() && f.canRead())
					{
						if (f.isFile())
						{
							// Add the found file to the list (if it's not in there already)
							if (!inList.contains(f)) {
								inList.add(f);
							}
						}
						else if (f.isDirectory())
						{
							// Recursively add the files from subdirectories
							addDirectoryToList(f, inList);
						}
					}
				}
			}
		}
	}
}
