package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.Photo;
import tim.prune.gui.PhotoThumbnail;

/**
 * Class to show a popup window for a photo
 */
public class PhotoPopupFunction extends GenericFunction
{
	/** popup window */
	private JFrame _frame = null; // would be a JDialog but that doesn't allow max button
	/** label for filename */
	private JLabel _label = null;
	/** Photo thumbnail */
	private PhotoThumbnail _photoThumb = null;

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public PhotoPopupFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.photopopup";
	}

	/**
	 * Show the screen
	 */
	public void begin()
	{
		if (_frame == null)
		{
			_frame = new JFrame(I18nManager.getText(getNameKey()));
			_frame.setIconImage(_parentFrame.getIconImage());
			_frame.getContentPane().add(makeContents());
			_frame.pack();
			_frame.setLocationRelativeTo(_parentFrame);
		}
		initFrame();
		_frame.setVisible(true);
	}

	/**
	 * Initialise the frame to show the current photo
	 */
	private void initFrame()
	{
		_frame.setVisible(false);
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		_frame.setTitle(photo.getName());
		_label.setText("'" + photo.getName() + "' ("
			+ photo.getWidth() + " x " + photo.getHeight() + ")");
		_photoThumb.setPhoto(photo);
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		_label = new JLabel("Photo popup");
		mainPanel.add(_label, BorderLayout.NORTH);
		_photoThumb = new PhotoThumbnail(false); // specify not in details panel
		_photoThumb.setPreferredSize(new Dimension(300, 300));
		mainPanel.add(_photoThumb, BorderLayout.CENTER);
		// Close button at bottom
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				_frame.dispose();
			}
		});
		okButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {_frame.dispose();}
			}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		okPanel.add(okButton);
		mainPanel.add(okPanel, BorderLayout.SOUTH);
		return mainPanel;
	}
}
