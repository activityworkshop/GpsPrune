package tim.prune.function.olc;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * text field for holding an OLC code with validation
 */
public class OlcField extends JTextField
{
	private static final int OLC_MAX_CHARS = 12;

	/**
	 * Inner class to act as document for validation
	 */
	protected static class OlcDocument extends PlainDocument
	{
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
			if (str == null) {return;}
			if (getLength() >= OLC_MAX_CHARS) return;
			char[] source = str.toCharArray();
			char[] result = new char[source.length];
			int j = 0;
			for (int i = 0; i < result.length && j < OLC_MAX_CHARS; i++) {
				// ignore non-allowed characters
				char c = Character.toUpperCase(source[i]);
				if ("023456789CFGHJMPQRVWX+".indexOf(c) >= 0) {
					result[j++] = c;
				}
			}
			super.insertString(offs, new String(result, 0, j), a);
		}
	}


	/**
	 * Constructor
	 */
	public OlcField()
	{
		super(OLC_MAX_CHARS);
		setDocument(new OlcDocument());
	}
}
