package tim.prune;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import tim.prune.gui.DetailsDisplay;
import tim.prune.gui.MapChart;
import tim.prune.gui.MenuManager;
import tim.prune.gui.ProfileChart;

/**
 * Tool to visualize, edit and prune GPS data
 */
public class GpsPruner
{
	public static final String VERSION_NUMBER = "1";
	public static final String BUILD_NUMBER = "041";
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
				if (args[0].length() == 11)
					locale = new Locale(args[0].substring(9));
				else if (args[0].length() == 14)
					locale = new Locale(args[0].substring(9, 11), args[0].substring(12));
				else
					System.out.println("Unrecognised locale '" + args[0].substring(9)
						+ "' - locale should be eg 'DE' or 'DE_ch'");
			}
			else
				System.out.println("Unknown parameter '" + args[0] +
					"'. Possible parameters:\n   --locale=  used for overriding locale settings\n");
		}
		I18nManager.init(locale);
		launch();
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

		// Make three GUI components and add as listeners
		DetailsDisplay leftPanel = new DetailsDisplay(APP, APP.getTrackInfo());
		broker.addSubscriber(leftPanel);
		MapChart mapDisp = new MapChart(APP, APP.getTrackInfo());
		broker.addSubscriber(mapDisp);
		ProfileChart profileDisp = new ProfileChart(APP.getTrackInfo());
		broker.addSubscriber(profileDisp);

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapDisp, profileDisp);
		rightPane.setResizeWeight(1.0); // allocate as much space as poss to map
		frame.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				rightPane));
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
		frame.setSize(600, 450);
		frame.show();
	}
}
