package tim.prune.gui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * text field for holding a single integer with validation
 */
public class WholeNumberField extends JTextField
{
	/**
	 * Inner class to act as document for validation
	 */
	protected static class WholeNumberDocument extends PlainDocument
	{
		/** Num digits to allow */
		private int _maxDigits = 0;

		/**
		 * Constructor
		 * @param inMaxDigits max digits to allow
		 */
		public WholeNumberDocument(int inMaxDigits)
		{
			_maxDigits = inMaxDigits;
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
			if (getLength() >= _maxDigits) return;
			char[] source = str.toCharArray();
			char[] result = new char[source.length];
			int j = 0;
			for (int i = 0; i < result.length && j < _maxDigits; i++) {
				if (Character.isDigit(source[i]))
					result[j++] = source[i];
			}
			super.insertString(offs, new String(result, 0, j), a);
		}
	}


	/**
	 * Constructor
	 * @param inMaxDigits max digits to allow
	 */
	public WholeNumberField(int inMaxDigits)
	{
		super(inMaxDigits);
		setDocument(new WholeNumberDocument(inMaxDigits));
		getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent arg0) {fireActionPerformed();}
			public void insertUpdate(DocumentEvent arg0) {fireActionPerformed();}
			public void changedUpdate(DocumentEvent arg0) {fireActionPerformed();}
		});
	}

	/**
	 * @return integer value
	 */
	public int getValue()
	{
		return parseValue(getText());
	}

	/**
	 * @param inValue value to set
	 */
	public void setValue(int inValue)
	{
		setText("" + inValue);
	}

	/**
	 * @param inText text to parse
	 * @return value as integer
	 */
	private static int parseValue(String inText)
	{
		int value = 0;
		try {
			value = Integer.parseInt(inText);
		}
		catch (NumberFormatException nfe) {}
		if (value < 0) {
			value = 0;
		}
		return value;
	}
}
