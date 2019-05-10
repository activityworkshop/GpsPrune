package tim.prune.load;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document class to restrict text input to single character
 * for selection of custom delimiter
 */
public class OneCharDocument extends PlainDocument
{
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException
	{
		//This rejects the insertion if it would make
		//the contents too long.
		if ((getLength() + str.length()) <= 1)
			super.insertString(offs, str, a);
	}
}
