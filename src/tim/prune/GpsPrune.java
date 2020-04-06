package tim.prune;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import tim.prune.config.Config;
import tim.prune.config.ConfigException;
import tim.prune.gui.DetailsDisplay;
import tim.prune.gui.IconManager;
import tim.prune.gui.MenuManager;
import tim.prune.gui.SelectorDisplay;
import tim.prune.gui.SidebarController;
import tim.prune.gui.StatusBar;
import tim.prune.gui.Viewport;
import tim.prune.gui.map.MapCanvas;
import tim.prune.gui.profile.ProfileChart;

/**
 * GpsPrune is a tool to visualize, edit, convert and prune GPS data
 * Please see the included readme.txt or https://activityworkshop.net
 * This software is copyright activityworkshop.net 2006-2020 and made available through the Gnu GPL version 2.
 * For license details please see the included license.txt.
 * GpsPrune is the main entry point to the application, including initialisation and launch
 */
public class GpsPrune
{
	/** Version number of application, used in about screen and for version check */
	public static final String VERSION_NUMBER = "20";
	/** Build number, just used for about screen */
	public static final String BUILD_NUMBER = "378";
	/** Static reference to App object */
	private static App APP = null;

	/** Program name, used for Frame title and for Macs also on the system bar */
	private static final String PROGRAM_NAME = "GpsPrune";


	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		Locale locale = null;
		String localeCode = null;
		String langFilename = null;
		String configFilename = null;
		ArrayList<File> dataFiles = new ArrayList<File>();
		boolean showUsage = false;

		// Mac OSX - specific properties (Mac insists that this is done as soon as possible)
		if (System.getProperty("mrj.version") != null) {
			System.setProperty("apple.laf.useScreenMenuBar", "true"); // menu at top of screen
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", PROGRAM_NAME);
		}
		// Loop over given arguments, if any
		for (int i=0; i<args.length; i++)
		{
			String arg = args[i];
			if (arg.startsWith("--lang="))
			{
				localeCode = arg.substring(7);
				locale = getLanguage(localeCode);
			}
			else if (arg.startsWith("--langfile="))
			{
				langFilename = arg.substring(11);
			}
			else if (arg.startsWith("--configfile="))
			{
				configFilename = arg.substring(13);
			}
			else if (arg.startsWith("--help")) {
				showUsage = true;
			}
			else
			{
				// Check if a data file has been given
				File f = new File(arg);
				if (f.exists() && f.isFile() && f.canRead()) {
					dataFiles.add(f);
				}
				else
				{
					// Make a useful String from the unknown parameter - could it be a mistyped filename?
					System.out.println(makeUnknownParameterString(arg));
					showUsage = true;
				}
			}
		}
		if (showUsage)
		{
			System.out.println("GpsPrune - a tool for editing GPS data.\nPossible parameters:"
				+ "\n   --configfile=<file> used to specify a configuration file"
				+ "\n   --lang=<code>       used to specify language code such as DE"
				+ "\n   --langfile=<file>   used to specify an alternative language file\n");
		}
		// Initialise configuration if selected
		try
		{
			if (configFilename != null) {
				Config.loadFile(new File(configFilename));
			}
			else {
				Config.loadDefaultFile();
			}
		}
		catch (ConfigException ce) {
			System.err.println("Failed to load config file: " + configFilename);
		}
		boolean overrideLang = (locale != null);
		if (overrideLang) {
			// Make sure Config holds chosen language
			Config.setConfigString(Config.KEY_LANGUAGE_CODE, localeCode);
		}
		else {
			// Set locale according to Config's language property
			String configLang = Config.getConfigString(Config.KEY_LANGUAGE_CODE);
			if (configLang != null) {
				Locale configLocale = getLanguage(configLang);
				if (configLocale != null) {locale = configLocale;}
			}
		}
		I18nManager.init(locale);
		// Load the external language file, either from config file or from command line params
		if (langFilename == null && !overrideLang) {
			// If langfilename is blank on command line parameters then don't use setting from config
			langFilename = Config.getConfigString(Config.KEY_LANGUAGE_FILE);
		}
		if (langFilename != null)
		{
			try {
				I18nManager.addLanguageFile(langFilename);
				Config.setConfigString(Config.KEY_LANGUAGE_FILE, langFilename);
			}
			catch (FileNotFoundException fnfe) {
				System.err.println("Failed to load language file: " + langFilename);
				Config.setConfigString(Config.KEY_LANGUAGE_FILE, "");
			}
		}

		// Set look-and-feel
		try {
			String windowStyle = Config.getConfigString(Config.KEY_WINDOW_STYLE);
			UIManager.setLookAndFeel(windowStyle);
		}
		catch (Exception e) {}

		// Set up the window and go
		launch(dataFiles);
	}


	/**
	 * Choose a locale based on the given code
	 * @param inString code for locale
	 * @return Locale object if available, otherwise null
	 */
	private static Locale getLanguage(String inString)
	{
		if (inString.length() == 2)
		{
			return new Locale(inString);
		}
		else if (inString.length() == 5 && inString.charAt(2) == '_')
		{
			return new Locale(inString.substring(0, 2), inString.substring(3));
		}
		System.out.println("Unrecognised locale '" + inString
			+ "' - value should be eg 'DE' or 'DE_ch'");
		return null;
	}


	/**
	 * Launch the main application
	 * @param inDataFiles list of data files to load on startup
	 */
	private static void launch(ArrayList<File> inDataFiles)
	{
		// Initialise Frame
		JFrame frame = new JFrame(PROGRAM_NAME);
		APP = new App(frame);

		// make menu
		MenuManager menuManager = new MenuManager(APP, APP.getTrackInfo());
		frame.setJMenuBar(menuManager.createMenuBar());
		APP.setMenuManager(menuManager);
		UpdateMessageBroker.addSubscriber(menuManager);
		// Make toolbar for buttons
		JToolBar toolbar = menuManager.createToolBar();

		// Make main GUI components and add as listeners
		SelectorDisplay leftPanel = new SelectorDisplay(APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(leftPanel);
		DetailsDisplay rightPanel = new DetailsDisplay(APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(rightPanel);
		MapCanvas mapDisp = new MapCanvas(APP, APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(mapDisp);
		Viewport viewport = new Viewport(mapDisp);
		APP.setViewport(viewport);
		ProfileChart profileDisp = new ProfileChart(APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(profileDisp);
		StatusBar statusBar = new StatusBar();
		UpdateMessageBroker.addSubscriber(statusBar);
		UpdateMessageBroker.informSubscribers("GpsPrune v" + VERSION_NUMBER);

		// Arrange in the frame using split panes
		JSplitPane midSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapDisp, profileDisp);
		midSplit.setResizeWeight(1.0); // allocate as much space as poss to map
		JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, midSplit, rightPanel);
		rightSplit.setResizeWeight(1.0); // allocate as much space as poss to map

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(toolbar, BorderLayout.NORTH);
		JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplit);
		frame.getContentPane().add(leftSplit, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		// add closing listener
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				APP.exit();
			}
		});
		// Avoid automatically shutting down if window closed
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// set window icons of different resolutions (1.6+)
		try
		{
			ArrayList<Image> icons = new ArrayList<Image>();
			String[] resolutions = {"_16", "_20", "_22", "_24", "_32", "_36", "_48", "_64", "_72", "_96", "_128"};
			for (String r : resolutions) {
				icons.add(IconManager.getImageIcon(IconManager.WINDOW_ICON + r + ".png").getImage());
			}
			Class<?> d = java.awt.Window.class;
			// This is the same as frame.setIconImages(icons) but is compilable also for java1.5 where this isn't available
			d.getDeclaredMethod("setIconImages", new Class[]{java.util.List.class}).invoke(frame, icons);
		}
		catch (Exception e)
		{
			// setting a list of icon images didn't work, so try with just one image instead
			try {
				frame.setIconImage(IconManager.getImageIcon(IconManager.WINDOW_ICON + "_16.png").getImage());
			}
			catch (Exception e2) {}
		}

		// Set up drag-and-drop handler to accept dropped files
		frame.setTransferHandler(new FileDropHandler(APP));

		// finish off and display frame
		frame.pack();
		if (!setFrameBoundsFromConfig(frame))
		{
			frame.setSize(650, 450);
		}
		frame.setVisible(true);
		// Set position of map/profile splitter
		midSplit.setDividerLocation(0.75);
		// Update menu (only needed for recent file list)
		UpdateMessageBroker.informSubscribers();

		// Make a full screen toggler
		SidebarController fsc = new SidebarController(new Component[] {leftPanel, profileDisp, rightPanel},
			new JSplitPane[] {leftSplit, midSplit, rightSplit});
		APP.setSidebarController(fsc);
		// Finally, give the files to load to the App
		APP.loadDataFiles(inDataFiles);
	}


	/**
	 * Set the frame bounds using the saved config setting
	 * @param inFrame frame to set the bounds of
	 * @return true on success
	 */
	private static boolean setFrameBoundsFromConfig(JFrame inFrame)
	{
		// Try to get bounds from config
		String bounds = Config.getConfigString(Config.KEY_WINDOW_BOUNDS);
		try
		{
			String[] boundValues = bounds.split("x");
			if (boundValues.length == 4)
			{
				int[] elems = new int[4];
				for (int i=0; i<4; i++) {
					elems[i] = Integer.parseInt(boundValues[i]);
				}
				// Make sure width and height aren't stupid
				elems[2] = Math.max(elems[2], 400);
				elems[3] = Math.max(elems[3], 300);
				inFrame.setBounds(elems[0], elems[1], elems[2], elems[3]);
				return true;
			}
		}
		catch (NullPointerException npe) {}  // if no entry found in config
		catch (NumberFormatException nfe) {} // if string couldn't be parsed
		return false;
	}


	/**
	 * Try to guess whether it's a mistyped parameter or a mistyped filename
	 * @param inParam command line argument
	 * @return error message
	 */
	private static String makeUnknownParameterString(String inParam)
	{
		File file = new File(inParam);
		if (file.exists())
		{
			if (file.isDirectory()) return "'" + inParam + "' is a directory";
			if (!file.canRead())    return "Cannot read file '" + inParam + "'";
			return "Something wrong with file '" + inParam + "'";
		}
		do
		{
			String name = file.getName();
			file = file.getParentFile();
			if (file != null && file.exists() && file.canRead()) return "Tried to load file '" + inParam + "' but cannot find '" + name + "'";
		}
		while (file != null);

		return "Unknown parameter '" + inParam + "'";
	}
}
