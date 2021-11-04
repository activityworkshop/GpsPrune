package tim.prune.gui;

import java.awt.Dimension;
import java.text.DecimalFormatSymbols;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * text field for holding a decimal number with validation
 * - doesn't allow certain characters such as a-z to be entered
 */
public class DecimalNumberField extends JTextField
{
	/**
	 * Inner class to act as document for validation
	 */
	protected static class DecimalNumberDocument extends PlainDocument
	{
		private final boolean _allowNegative;
		private final char _decimalPoint;

		/** constructor */
		DecimalNumberDocument(boolean inAllowNegative)
		{
			_allowNegative = inAllowNegative;
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			_decimalPoint = dfs.getDecimalSeparator();
		}

		/**
		 * Override the insert string method
		 * @param offs offset
		 * @param str string
		 * @param a attributes
		 * @throws BadLocationException on insert failure
		 */
		public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException
		{
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < str.length(); i++)
			{
				final char c = str.charAt(i);
				if (!_allowNegative && c == '-') {
					continue; // negative not allowed
				}
				if (c == _decimalPoint || ("01234567890-".indexOf(c) >= 0)) {
					buffer.append(c);
				}
			}
			super.insertString(offs, buffer.toString(), a);
		}
	}


	/**
	 * Constructor
	 */
	public DecimalNumberField()
	{
		this(false);
	}

	/**
	 * Constructor
	 * @param inAllowNegative true to allow negative numbers
	 */
	public DecimalNumberField(boolean inAllowNegative)
	{
		super(6);
		setDocument(new DecimalNumberDocument(inAllowNegative));
	}

	/**
	 * @return double value
	 */
	public double getValue()
	{
		return parseValue(getText());
	}

	/**
	 * @param inValue value to set
	 */
	public void setValue(double inValue)
	{
		setText("" + inValue);
	}

	/**
	 * @param inText text to parse
	 * @return value as double
	 */
	private static double parseValue(String inText)
	{
		double value = 0.0;
		try {
			value = Double.parseDouble(inText);
		}
		catch (NumberFormatException nfe) {}
		if (value < 0) {
			value = 0;
		}
		return value;
	}

	/**
	 * Put a minimum on the minimum width
	 */
	public Dimension getMinimumSize()
	{
		Dimension dim = super.getMinimumSize();
		if (dim.width < 50) dim.width = 50;
		return dim;
	}
}
