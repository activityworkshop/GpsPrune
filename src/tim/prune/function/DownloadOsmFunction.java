package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DoubleRange;

/**
 * Class to allow the download of OSM data (using the XAPI)
 * for the area covered by the data
 */
public class DownloadOsmFunction extends GenericFunction implements Runnable
{
	private JDialog _dialog = null;
	private JLabel[] _latLonLabels = null;
	private JProgressBar _progressBar = null;
	private JButton _okButton = null;
	private JFileChooser _fileChooser = null;
	private File _selectedFile = null;
	private boolean _cancelled = false;
	/** Number formatter */
	private final NumberFormat FORMAT_TWO_DP = NumberFormat.getNumberInstance();


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DownloadOsmFunction(App inApp)
	{
		super(inApp);
		FORMAT_TWO_DP.setMaximumFractionDigits(2);
		FORMAT_TWO_DP.setMinimumFractionDigits(2);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.downloadosm";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_fileChooser = new JFileChooser();
			_fileChooser.setSelectedFile(new File("data.osm"));
		}
		initDialog();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.downloadosm.desc")), BorderLayout.NORTH);
		// grid of labels to show lat/long extent
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(3, 3));
		_latLonLabels = new JLabel[4];
		for (int i=0; i<4; i++) {
			_latLonLabels[i] = new JLabel("0");
		}
		int lNum = 0;
		for (int i=0; i<4; i++) {
			gridPanel.add(new JLabel(" "));
			gridPanel.add(_latLonLabels[lNum++]);
		}
		// layout main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(gridPanel);
		_progressBar = new JProgressBar();
		_progressBar.setIndeterminate(true);
		mainPanel.add(_progressBar);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		};
		_okButton.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_cancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Initialise the values of the labels in the dialog
	 */
	private void initDialog()
	{
		// Get range of data
		String[] lats = expandRange(_app.getTrackInfo().getTrack().getLatRange());
		String[] lons = expandRange(_app.getTrackInfo().getTrack().getLonRange());
		_latLonLabels[0].setText(lats[1]); // max lat
		_latLonLabels[1].setText(lons[0]); // min lon
		_latLonLabels[2].setText(lons[1]); // max lon
		_latLonLabels[3].setText(lats[0]); // min lat
		_okButton.setEnabled(true);
		_progressBar.setVisible(false);
		_cancelled = false;
	}

	/**
	 * Expand the given range to reasonable limits
	 * @param inRange range of lat/long values
	 * @return expanded range as pair of Strings
	 */
	private String[] expandRange(DoubleRange inRange)
	{
		double mid = (inRange.getMaximum() + inRange.getMinimum()) / 2.0;
		double range = inRange.getRange();
		double max = 0.0, min = 0.0;
		// Expand range to at least 0.02 degree
		if (range < 0.02)
		{
			min = mid - 0.01;
			max = mid + 0.01;
		}
		else {
			// expand by 10% in both directions
			min = mid - range * 0.55;
			max = mid + range * 0.55;
		}
		// Round min down to 0.01 degree
		int minCents = (int) (100 * min);
		// Round max upwards likewise
		int maxCents = (int) (100 * max + 1);
		final String[] answer = new String[] {FORMAT_TWO_DP.format(minCents/100.0),
			FORMAT_TWO_DP.format(maxCents/100.0)};
		return answer;
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		if (!_okButton.isEnabled()) return;
		_selectedFile = selectOsmFile();
		if (_selectedFile != null)
		{
			// Show progress bar
			_okButton.setEnabled(false);
			_progressBar.setVisible(true);
			new Thread(this).start();
		}
		else
			_dialog.dispose();
	}

	/**
	 * Select a file to save the OSM data to
	 * @return selected file or null if cancelled
	 */
	private File selectOsmFile()
	{
		File saveFile = null;
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_dialog) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = _fileChooser.getSelectedFile();
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(_dialog,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// new file or overwrite confirmed
					saveFile = file;
				}
				else
				{
					// file exists and overwrite cancelled - select again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
		return saveFile;
	}


	/**
	 * Do the actual download - launched to run in another thread
	 */
	public void run()
	{
		String url = "http://overpass-api.de/api/interpreter?data=(node(" +
			_latLonLabels[3].getText() + "," + _latLonLabels[1].getText() + "," +
			_latLonLabels[0].getText() + "," + _latLonLabels[2].getText() + ");<;);out%20qt;";
		// System.out.println(url);

		byte[] buffer = new byte[1024];
		InputStream inStream = null;
		FileOutputStream outStream = null;
		int numBytesRead = 0;
		try
		{
			inStream = new URL(url).openStream();
			outStream = new FileOutputStream(_selectedFile);
			// Loop and copy bytes to file
			while ((numBytesRead = inStream.read(buffer)) > -1 && !_cancelled)
			{
				outStream.write(buffer, 0, numBytesRead);
			}
		}
		catch (MalformedURLException mue) {}
		catch (IOException ioe) {
			_app.showErrorMessageNoLookup(getNameKey(), ioe.getClass().getName() + " - " + ioe.getMessage());
		}
		// clean up streams
		finally {
			try {inStream.close();} catch (Exception e) {}
			try {outStream.close();} catch (Exception e) {}
		}
		// close dialog
		_dialog.dispose();
	}
}
