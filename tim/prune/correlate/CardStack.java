package tim.prune.correlate;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * Panel to act as a card stack
 */
public class CardStack extends JPanel
{
	private int _numCards = 0;
	private int _currCard = 0;
	private CardLayout _layout = null;
	private static final String cardName = "card";

	/**
	 * Constructor
	 */
	public CardStack()
	{
		_layout = new CardLayout();
		setLayout(_layout);
	}

	/**
	 * Add a card to the stack
	 * @param inComponent component to add
	 */
	public void addCard(Component inComponent)
	{
		super.add(inComponent, cardName + _numCards);
		_numCards++;
	}

	/**
	 * @return current card index, starting from 0
	 */
	public int getCurrentCardIndex()
	{
		return _currCard;
	}

	/**
	 * @return number of cards in the stack
	 */
	public int getNumCards()
	{
		return _numCards;
	}

	/**
	 * Show the specified card
	 * @param inIndex index of card, starting from 0
	 */
	public void showCard(int inIndex)
	{
		if (inIndex >= 0 && inIndex < _numCards) {
			_currCard = inIndex;
			_layout.show(this, cardName + inIndex);
		}
	}
}
