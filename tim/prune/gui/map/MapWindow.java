package tim.prune.gui.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.data.Track;

/**
 * Class to hold the gui functions of the map window
 */
public class MapWindow extends JFrame
{
	private MapCanvas _canvas = null;

	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public MapWindow(Track inTrack)
	{
		super(I18nManager.getText("dialog.map.title"));
		getContentPane().add(createComponents(inTrack));
		setResizable(false);
	}

	/**
	 * @param inTrack track object
	 * @return gui components
	 */
	private Component createComponents(Track inTrack)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		_canvas = new MapCanvas(inTrack);
		panel.add(_canvas, BorderLayout.CENTER);
		// Make panel for zoom buttons
		JPanel buttonPanel = new JPanel();
		JButton zoomInButton = new JButton(I18nManager.getText("menu.map.zoomin"));
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_canvas.zoomIn();
			}
		});
		buttonPanel.add(zoomInButton);
		JButton zoomOutButton = new JButton(I18nManager.getText("menu.map.zoomout"));
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_canvas.zoomOut();
			}
		});
		buttonPanel.add(zoomOutButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}
}
