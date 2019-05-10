package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Photo;
import tim.prune.gui.PhotoThumbnail;

/**
 * Class to show a popup window for a photo
 */
public class PhotoPopupFunction extends GenericFunction implements DataSubscriber
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
			_frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_frame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					UpdateMessageBroker.removeSubscriber(PhotoPopupFunction.this);
					super.windowClosed(e);
				}
			});
		}
		initFrame();
		final Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo.getWidth() <= 0 || photo.getHeight() <= 0)
		{
			_frame.setVisible(false);
			_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getText("error.showphoto.failed")
			 + " : " + photo.getName());
		}
		else
		{
			_frame.setVisible(true);
			// Add listener to Broker
			UpdateMessageBroker.addSubscriber(this);
		}
	}

	/**
	 * Initialise the frame to show the current photo
	 */
	private void initFrame()
	{
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo == null)
		{
			_frame.setTitle("GpsPrune - " + I18nManager.getText("details.nophoto"));
			_label.setText(I18nManager.getText("details.nophoto"));
		}
		else
		{
			_frame.setTitle(photo.getName());
			_label.setText("'" + photo.getName() + "' ("
				+ photo.getWidth() + " x " + photo.getHeight() + ")");
		}
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

	public void dataUpdated(byte inUpdateType)
	{
		// Update photo if selection changes
		if ((inUpdateType & DataSubscriber.SELECTION_CHANGED) > 0)
		{
			initFrame();
		}
	}

	public void actionCompleted(String inMessage) {}
}
