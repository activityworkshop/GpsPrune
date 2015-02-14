package tim.prune;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import tim.prune.gui.DetailsDisplay;
import tim.prune.gui.MapChart;
import tim.prune.gui.MenuManager;
import tim.prune.gui.ProfileChart;
import tim.prune.gui.SelectorDisplay;

/**
 * Tool to visualize, edit and prune GPS data
 * Please see the included readme.txt or http://activityworkshop.net
 */
public class GpsPruner
{
	// Patch to version 4
	public static final String VERSION_NUMBER = "4.1";
	public static final String BUILD_NUMBER = "091";
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
		UpdateMessageBroker broker = new UpdateMessageBroker();
		APP = new App(frame, broker);

		// make menu
		MenuManager menuManager = new MenuManager(frame, APP, APP.getTrackInfo());
		frame.setJMenuBar(menuManager.createMenuBar());
		APP.setMenuManager(menuManager);
		broker.addSubscriber(menuManager);
		// Make toolbar for buttons
		JToolBar toolbar = menuManager.createToolBar();

		// Make three GUI components and add as listeners
		SelectorDisplay leftPanel = new SelectorDisplay(APP.getTrackInfo());
		broker.addSubscriber(leftPanel);
		DetailsDisplay rightPanel = new DetailsDisplay(APP.getTrackInfo());
		broker.addSubscriber(rightPanel);
		MapChart mapDisp = new MapChart(APP, APP.getTrackInfo());
		broker.addSubscriber(mapDisp);
		ProfileChart profileDisp = new ProfileChart(APP.getTrackInfo());
		broker.addSubscriber(profileDisp);

		JSplitPane midPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapDisp, profileDisp);
		midPane.setResizeWeight(1.0); // allocate as much space as poss to map
		JSplitPane triplePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, midPane, rightPanel);
		triplePane.setResizeWeight(1.0); // allocate as much space as poss to map

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(toolbar, BorderLayout.NORTH);
		frame.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
			triplePane), BorderLayout.CENTER);
		// add closing listener
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				APP.exit();
			}
		});
		// Avoid automatically shutting down if window closed
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// finish off and display frame
		frame.pack();
		frame.setSize(650, 450);
		frame.show();
	}
}
