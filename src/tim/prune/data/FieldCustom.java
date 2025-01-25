package tim.prune.data;

import java.util.Objects;

/**
 * Class to represent a custom field from a text file
 */
public class FieldCustom extends Field
{
	private String _customLabel;


	public FieldCustom(String inLabel)
	{
		super(FileType.TEXT);
		_customLabel = inLabel;
	}

	/**
	 * @return the name of the field
	 */
	public String getName() {
		return _customLabel;
	}

	/**
	 * Change the name of the (non built-in) field
	 * @param inName new name
	 */
	public void setName(String inName) {
		_customLabel = inName;
	}

	public boolean equals(Object inOther)
	{
		return inOther instanceof FieldCustom
			&& Objects.equals(_customLabel, ((FieldCustom) inOther)._customLabel);
	}
}
