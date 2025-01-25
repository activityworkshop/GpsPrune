package tim.prune.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Class to represent a field from an xml extension
 */
public class FieldXml extends Field
{
	private final String _label;
	private final String _tagName;
	private final String[] _categories;


	public FieldXml(FileType inFileType, String inTagName, Collection<String> inCategories)
	{
		super(inFileType);
		_tagName = inTagName;
		_label = FieldRecogniser.getLabel(inTagName);
		_categories = inCategories == null ? new String[0] : inCategories.toArray(new String[0]);
	}

	public FieldXml(FileType inFileType, String inTagName, String inCategory) {
		this(inFileType, inTagName, List.of(inCategory));
	}

	/**
	 * @return the name of the field for display
	 */
	public String getName() {
		return _label;
	}

	/**
	 * @return the categories of the field for export
	 */
	public String[] getCategories() {
		return _categories;
	}

	/**
	 * @return the xml tag holding the given value (assuming categories are already present)
	 */
	public String getTag(String inValue) {
		return "<" + _tagName + ">" + inValue + "</" + _tagName + ">";
	}

	/**
	 * Checks if the two fields are equal
	 * @param inOther other Field object
	 * @return true if Fields are identical
	 */
	public boolean equals(Object inOther)
	{
		if (inOther == null || !(inOther instanceof FieldXml)) {
			return false;
		}
		FieldXml other = (FieldXml) inOther;
		return Objects.equals(_tagName, other._tagName)
				&& Arrays.equals(_categories, other._categories);
	}
}
