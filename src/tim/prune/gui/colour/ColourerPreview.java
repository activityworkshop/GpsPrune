package tim.prune.gui.colour;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Shows a preview of the colours used by a Colourer object,
 * which appears either as discrete colour bands (if the Colourer is discrete)
 * or as a continuous spectrum (if it's continuous).
 */
public class ColourerPreview extends JPanel
{
	private static final Dimension _size = new Dimension(400, 30);
	private PointColourer _colourer = null;

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if (_colourer == null) {
			paintWithoutColourer(g);
		}
		else if (_colourer.isDiscrete()) {
			paintWithDiscreteColourer(g);
		}
		else {
			paintWithContinuousColourer(g);
		}
	}

	/** Just paint white background, should be invisible anyway */
	private void paintWithoutColourer(Graphics inG)
	{
		inG.setColor(Color.WHITE);
		inG.fillRect(0, 0, getWidth(), getHeight());
	}

	/** Paint stripes according to the set colourer */
	private void paintWithDiscreteColourer(Graphics inG)
	{
		if (_colourer instanceof DiscretePointColourer)
		{
			DiscretePointColourer discreteColourer = (DiscretePointColourer) _colourer;
			final int numStripes = Math.max(10, _colourer.getMaxColours());
			discreteColourer.generateDiscreteColours(numStripes);
			final int stripeWidth = getWidth() / numStripes;
			for (int i = 0; i < numStripes; i++) {
				inG.setColor(discreteColourer.getDiscreteColour(i));
				inG.fillRect(i * stripeWidth, 0, stripeWidth, getHeight());
			}
		}
	}

	/** Paint a spectrum according to the set colourer */
	private void paintWithContinuousColourer(Graphics inG)
	{
		final int width = getWidth();
		final int height = getHeight();
		for (int i=0; i<width; i += 2)
		{
			inG.setColor(_colourer.mixColour(i * 1.0f / width));
			inG.fillRect(i, 0, 2, height);
		}
	}

	void setColourer(PointColourer inColourer)
	{
		_colourer = inColourer;
		this.repaint(0, 0, getWidth(), getHeight());
	}

	public Dimension getPreferredSize() {
		return _size;
	}
}
