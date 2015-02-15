package tim.prune.load.babel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import tim.prune.I18nManager;
import tim.prune.gui.StatusIcon;

/**
 * Gui element to allow the specification of filters for GPSBabel.
 * Used for loading from GPS and loading from file
 */
public class BabelFilterPanel extends JPanel
{
	/** Text field for entering filters manually */
	private JTextField _filterField = null;
	/** Icon for showing whether the value is valid for GPSBabel or not */
	private StatusIcon _validIcon = null;
	/** Dialog for adding a new filter */
	private AddFilterDialog _addDialog = null;

	/** Regular expression for detecting valid filter strings */
	private static final Pattern FILTER_PATTERN
		= Pattern.compile("(-x [a-z,\\.0-9=]+ *)+");

	/**
	 * Constructor
	 * @param inParentFrame parent frame for launching popup dialog
	 */
	public BabelFilterPanel(JFrame inParentFrame)
	{
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(I18nManager.getText("dialog.gpsbabel.filters")),
			BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		initPanel();
		_addDialog = new AddFilterDialog(this, inParentFrame);
	}

	/**
	 * Set up the panel with all the components inside
	 */
	private void initPanel()
	{
		setLayout(new BorderLayout(4, 4));
		// text field for the filter text
		_filterField = new JTextField(20);
		_filterField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						checkFilter();
					}
				});
			}
		});
		JPanel filterFieldPanel = new JPanel();
		filterFieldPanel.setLayout(new BorderLayout(3, 3));
		JPanel filterIconPanel = new JPanel();
		filterIconPanel.setLayout(new BorderLayout(3, 3));
		filterIconPanel.add(_filterField, BorderLayout.CENTER);
		_validIcon = new StatusIcon();
		filterIconPanel.add(_validIcon, BorderLayout.EAST);
		filterFieldPanel.add(filterIconPanel, BorderLayout.NORTH);
		add(filterFieldPanel, BorderLayout.CENTER);
		// Add and clear buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JButton addButton = new JButton(I18nManager.getText("button.addnew"));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("Filter exists: " + hasFilter() + ", valid: " + isFilterValid());
				_addDialog.showDialog();
			}
		});
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createVerticalStrut(2));
		JButton clearButton = new JButton(I18nManager.getText("button.delete"));
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				_filterField.setText("");
				checkFilter();
			}
		});
		buttonPanel.add(clearButton);
		add(buttonPanel, BorderLayout.EAST);
	}

	/**
	 * @param inFilter filter string to set (normally from config)
	 */
	public void setFilterString(String inFilter)
	{
		if (inFilter != null && _filterField != null) {
			_filterField.setText(inFilter.trim());
		}
		checkFilter();
	}

	/**
	 * @return trimmed filter string, or null
	 */
	public String getFilterString()
	{
		String filter = _filterField.getText();
		if (filter != null) filter = filter.trim();
		return filter;
	}

	/**
	 * @return true if a filter has been given (which may or may not be valid)
	 */
	public boolean hasFilter()
	{
		String str = getFilterString();
		return str != null && str.length() > 0;
	}

	/**
	 * @return true if the given filter string is valid
	 */
	public boolean isFilterValid()
	{
		String str = getFilterString();
		if (str == null) return false;
		return FILTER_PATTERN.matcher(str).matches();
	}

	/**
	 * Called from the add filter dialog to indicate completion
	 * @param inFilter filter to add
	 */
	public void addFilter(String inFilter)
	{
		if (inFilter != null)
		{
			String newFilter = inFilter.trim();
			String currFilter = getFilterString();
			if (!newFilter.equals(""))
			{
				if (currFilter == null || currFilter.equals("")) {
					currFilter = newFilter;
				}
				else { // append
					currFilter = currFilter + " " + newFilter;
				}
			}
			_filterField.setText(currFilter);
		}
		checkFilter();
	}

	/**
	 * See if the current filter is valid or not, and update the icon accordingly
	 */
	private void checkFilter()
	{
		if (hasFilter())
		{
			if (isFilterValid()) {
				_validIcon.setStatusValid();
			}
			else {
				_validIcon.setStatusInvalid();
			}
		}
		else
		{
			_validIcon.setStatusBlank();
		}
	}
}
