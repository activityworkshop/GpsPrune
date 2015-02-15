package tim.prune.load.babel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.gui.WizardLayout;


/**
 * Class to manage a dialog for adding a single GPSBabel filter
 */
public class AddFilterDialog
{
	/** Parent panel to pass the filter back to */
	private BabelFilterPanel _parentPanel = null;
	/** Reference to parent frame */
	private JFrame _parentFrame = null;
	/** Main dialog */
	private JDialog _dialog = null;
	/** layout for dealing with cards */
	private WizardLayout _wizard = null;
	/** Array of filter definitions */
	private FilterDefinition[] _filters = new FilterDefinition[4];
	/** Finish button */
	private JButton _finishButton = null;
	/** back button */
	private JButton _backButton = null;

	// Selector class for one of the filter types
	class FilterTypeListener implements ActionListener
	{
		private int _index = 0;
		public FilterTypeListener(int inIndex) {_index = inIndex;}
		public void actionPerformed(ActionEvent e) {
			_wizard.showCard(_index);
			_backButton.setEnabled(true);
			filterParamsChanged(); // to check parameters and enable/disable Finish button
		}
	}

	/**
	 * Constructor
	 * @param inParent parent panel to inform of selected filter
	 * @param inParentFrame parent frame to reference for dialogs
	 */
	public AddFilterDialog(BabelFilterPanel inParent, JFrame inParentFrame)
	{
		_parentPanel = inParent;
		_parentFrame = inParentFrame;
	}

	/**
	 * Show the dialog to add a new filter
	 */
	public void showDialog()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.addfilter.title"), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// TODO: Initialise cards, clear entries?
		_wizard.showFirstCard();
		_backButton.setEnabled(false);
		_finishButton.setEnabled(false);
		_dialog.setVisible(true);
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());

		// card panel in the middle
		JPanel cardPanel = new JPanel();
		_wizard = new WizardLayout(cardPanel);
		JPanel typesCard = new JPanel();
		JButton discardButton = new JButton(I18nManager.getText("dialog.gpsbabel.filter.discard"));
		discardButton.addActionListener(new FilterTypeListener(1));
		typesCard.add(discardButton);
		JButton simplifyButton = new JButton(I18nManager.getText("dialog.gpsbabel.filter.simplify"));
		simplifyButton.addActionListener(new FilterTypeListener(2));
		typesCard.add(simplifyButton);
		JButton distanceButton = new JButton(I18nManager.getText("dialog.gpsbabel.filter.distance"));
		distanceButton.addActionListener(new FilterTypeListener(3));
		typesCard.add(distanceButton);
		JButton interpButton = new JButton(I18nManager.getText("dialog.gpsbabel.filter.interpolate"));
		interpButton.addActionListener(new FilterTypeListener(4));
		typesCard.add(interpButton);

		// discard panel
		_filters[0] = new DiscardFilter(this);
		// simplify panel
		_filters[1] = new SimplifyFilter(this);
		// distance panel
		_filters[2] = new DistanceFilter(this);
		// interpolate panel
		_filters[3] = new InterpolateFilter(this);

		// Add cards to the wizard
		_wizard.addCard(typesCard);
		_wizard.addCard(_filters[0]);
		_wizard.addCard(_filters[1]);
		_wizard.addCard(_filters[2]);
		_wizard.addCard(_filters[3]);
		dialogPanel.add(cardPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_backButton = new JButton(I18nManager.getText("button.back"));
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_wizard.showCard(0);
				_backButton.setEnabled(!_wizard.isFirstCard());
				_finishButton.setEnabled(false);
			}
		});
		_backButton.setEnabled(false);
		buttonPanel.add(_backButton);
		_finishButton = new JButton(I18nManager.getText("button.finish"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		_finishButton.addActionListener(okListener);
		_finishButton.setEnabled(false);
		buttonPanel.add(_finishButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));

		return dialogPanel;
	}

	/**
	 * React to changes in the filter parameters (such as enabling/disabling the ok button)
	 */
	public void filterParamsChanged()
	{
		final int currCard = _wizard.getCurrentCardIndex();
		if (currCard > 0 && currCard < 5) {
			_finishButton.setEnabled(_filters[currCard-1].isFilterValid());
		}
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		// finish dialog and pass results back to the parent panel
		final int currCard = _wizard.getCurrentCardIndex();
		if (currCard > 0 && currCard < 5) {
			_parentPanel.addFilter(_filters[currCard-1].getString());
		}
		_dialog.dispose();
	}
}
