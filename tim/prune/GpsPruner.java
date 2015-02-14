package tim.prune;

import java.awt.event.WindowAdapter;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import tim.prune.gui.DetailsDisplay;
import tim.prune.gui.MapChart;
import tim.prune.gui.MenuManager;
import tim.prune.gui.ProfileChart;
import tim.prune.gui.SelectorDisplay;
import tim.prune.gui.StatusBar;

/**
 * Tool to visualize, edit and prune GPS data
 * Please see the included readme.txt or http://activityworkshop.net
 */
public class GpsPruner
{
	// Final build of version 5
	public static final String VERSION_NUMBER = "5";
	public static final String BUILD_NUMBER = "100";
	private static App APP = null;


	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		Locale locale = null;
		if (args.length == 1)
		{
			if (args[0].startsWith("--locale="))
			{
				locale = getLanguage(args[0].substring(9));
			}
			else if (args[0].startsWith("--lang="))
			{
				locale = getLanguage(args[0].substring(7));
			}
			else
			{
				System.out.println("Unknown parameter '" + args[0] +
					"'. Possible parameters:\n   --locale= or --lang=  used for overriding language settings\n");
			}
		}
		I18nManager.init(locale);
		launch();
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
		else if (inString.length() == 5)
		{
			return new Locale(inString.substring(0, 2), inString.substring(3));
		}
		System.out.println("Unrecognised locale '" + inString
			+ "' - value should be eg 'DE' or 'DE_ch'");
		return null;
	}


	/**
	 * Launch the main application
	 */
	private static void launch()
	{
		JFrame frame = new JFrame("Prune");
		APP = new App(frame);

		// make menu
		MenuManager menuManager = new MenuManager(frame, APP, APP.getTrackInfo());
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
		MapChart mapDisp = new MapChart(APP, APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(mapDisp);
		ProfileChart profileDisp = new ProfileChart(APP.getTrackInfo());
		UpdateMessageBroker.addSubscriber(profileDisp);
		StatusBar statusBar = new StatusBar();
		UpdateMessageBroker.addSubscriber(statusBar);
		UpdateMessageBroker.informSubscribers("Prune v" + VERSION_NUMBER);

		// Arrange in the frame using split panes
		JSplitPane midPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapDisp, profileDisp);
		midPane.setResizeWeight(1.0); // allocate as much space as poss to map
		JSplitPane triplePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, midPane, rightPanel);
		triplePane.setResizeWeight(1.0); // allocate as much space as poss to map

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(toolbar, BorderLayout.NORTH);
		frame.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
			triplePane), BorderLayout.CENTER);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		// add closing listener
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				APP.exit();
			}
		});
		// Avoid automatically shutting down if window closed
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// set icon
		try {
			frame.setIconImage(new ImageIcon(GpsPruner.class.getResource("gui/images/window_icon.png")).getImage());
		}
		catch (Exception e) {} // ignore

		// finish off and display frame
		frame.pack();
		frame.setSize(650, 450);
		frame.show();
		// Set position of map/profile splitter
		midPane.setDividerLocation(0.75);
	}
}
