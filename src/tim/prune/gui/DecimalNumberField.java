package tim.prune.gui;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * Text field for holding a decimal number with validation
 * - doesn't allow certain characters such as a-z to be entered
 */
public class DecimalNumberField extends JTextField
{
	/** Are negative numbers allowed or not */
	private final boolean _allowNegative;
	/** formatter responsible for parsing and formatting */
	private final NumberFormat _localFormatter;

	/**
	 * Inner class to act as document for validation
	 */
	protected static class DecimalNumberDocument extends PlainDocument
	{
		private final boolean _allowNegative;
		private final char _decimalPoint;
		private static final int MAX_LENGTH = 10;

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
			if (getLength() > MAX_LENGTH) {
				return;
			}
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
	public DecimalNumberField() {
		this(false);
	}

	/**
	 * Constructor
	 * @param inAllowNegative true to allow negative numbers
	 */
	public DecimalNumberField(boolean inAllowNegative)
	{
		super(6);
		_allowNegative = inAllowNegative;
		setDocument(new DecimalNumberDocument(inAllowNegative));
		_localFormatter = NumberFormat.getNumberInstance();
		if (_localFormatter instanceof DecimalFormat) {
			((DecimalFormat) _localFormatter).applyPattern("0.00");
		}
	}

	@Override
	public String getText() {
		throw new IllegalArgumentException("Should not be called, use getValue instead");
	}

	/**
	 * @return double value
	 */
	public double getValue()
	{
		double value = 0.0;
		try {
			value = _localFormatter.parse(super.getText()).doubleValue();
		}
		catch (NumberFormatException | ParseException e) {
			// value stays zero
		}
		if (!_allowNegative) {
			value = Math.max(0.0, value);
		}
		return value;
	}

	/**
	 * @return true if there is no text in the field
	 */
	public boolean isEmpty() {
		return super.getText().isEmpty();
	}

	/**
	 * @param inValue value to set
	 */
	public void setValue(double inValue)
	{
		double valueToSet = _allowNegative ? inValue : Math.max(0.0, inValue);
		setText(_localFormatter.format(valueToSet));
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
