package tim.prune.gui;

import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JPanel;

/**
 * Layout class enhancing the regular card layout to add the ability to
 * see which is the current card, how many cards there are, previous / next etc
 */
public class WizardLayout extends CardLayout
{
	private JPanel _panel = null;
	private int    _currentCard = 0;
	private int    _numCards = 0;

	/**
	 * Constructor
	 * @param inPanel panel controlled by this layout
	 */
	public WizardLayout(JPanel inPanel)
	{
		super();
		_panel = inPanel;
		_panel.setLayout(this);
	}

	/**
	 * Add a card to this layout
	 * @param inCard
	 */
	public void addCard(Component inCard)
	{
		_panel.add(inCard, "card" + _numCards);
		_numCards++;
	}

	/**
	 * @return current card index (from 0)
	 */
	public int getCurrentCardIndex() {
		return _currentCard;
	}

	/**
	 * Go to the first card
	 */
	public void showFirstCard()
	{
		first(_panel);
		_currentCard = 0;
	}

	/**
	 * Go to the next card
	 */
	public void showNextCard()
	{
		if (_currentCard < (_numCards-1))
		{
			next(_panel);
			_currentCard++;
		}
	}

	/**
	 * Go to the previous card
	 */
	public void showPreviousCard()
	{
		if (_currentCard > 0)
		{
			previous(_panel);
			_currentCard--;
		}
	}

	/**
	 * @return true if this is the first card
	 */
	public boolean isFirstCard() {
		return _currentCard == 0;
	}

	/**
	 * @return true if this is the last card
	 */
	public boolean isLastCard() {
		return _currentCard == (_numCards-1);
	}

	/**
	 * @param inIndex index (from 0) of the card to show
	 */
	public void showCard(int inIndex)
	{
		if (inIndex >= 0 && inIndex < _numCards) {
			show(_panel, "card" + inIndex);
			_currentCard = inIndex;
		}
	}
}
