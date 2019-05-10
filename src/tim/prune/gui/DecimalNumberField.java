package tim.prune.gui;

import java.awt.Dimension;

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
		private boolean _allowNegative = false;

		/** constructor */
		DecimalNumberDocument(boolean inAllowNegative) {
			_allowNegative = inAllowNegative;
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
			char[] source = str.toCharArray();
			char[] result = new char[source.length];
			int j = 0;
			for (int i = 0; i < result.length; i++) {
				if (!Character.isLetter(source[i]) && (_allowNegative || source[i] != '-') && source[i] != ' ') // no letters, no minus sign or space
					result[j++] = source[i];
			}
			super.insertString(offs, new String(result, 0, j), a);
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
