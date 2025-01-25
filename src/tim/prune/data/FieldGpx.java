package tim.prune.data;

import java.util.List;
import java.util.Objects;


/** Represents a field loaded directly from Gpx (not from an extension) */
public class FieldGpx extends Field
{
	private final String _tag;

	private static final List<FieldGpx> _knownFields = List.of(
		new FieldGpx("course"), new FieldGpx("speed"), new FieldGpx("magvar"), new FieldGpx("geoidheight"),
		// followed by name, cmt, desc
		new FieldGpx("src"), new FieldGpx("fix"), new FieldGpx("sat"), new FieldGpx("hdop"), new FieldGpx("vdop"),
		new FieldGpx("pdop"), new FieldGpx("ageofdgpsdata"), new FieldGpx("dgpsid")
	);


	private FieldGpx(String inTag)
	{
		super(FileType.GPX);
		_tag = inTag;
	}

	/** @return the name of the field */
	public String getName() {
		return _tag;
	}

	/** @return the known field with the given name, if it exists, or null */
	public static FieldGpx getField(String inName)
	{
		for (FieldGpx field : _knownFields)
		{
			if (field.getName().equals(inName)) {
				return field;
			}
		}
		return null; // not known
	}

	/** @return the open tag for this field */
	public String getOpenTag() {
		return "<" + getName() + ">";
	}

	/** @return the closing tag for this field */
	public String getCloseTag() {
		return "</" + getName() + ">";
	}

	/** @return an array of the fields which appear in the first set, before name */
	public static FieldGpx[] getFirstFields()
	{
		return new FieldGpx[] {new FieldGpx("course"), new FieldGpx("speed"),
				new FieldGpx("magvar"), new FieldGpx("geoidheight")};
	}

	/** @return an array of the fields which appear in the second set, after name */
	public static FieldGpx[] getSecondFields()
	{
		return new FieldGpx[] {new FieldGpx("src"), new FieldGpx("fix"),
				new FieldGpx("sat"), new FieldGpx("hdop"), new FieldGpx("vdop"),
				new FieldGpx("pdop"), new FieldGpx("ageofdgpsdata"), new FieldGpx("dgpsid")};
	}

	public boolean equals(Object inOther)
	{
		return inOther instanceof FieldGpx
			&& Objects.equals(_tag, ((FieldGpx) inOther)._tag);
	}
}
