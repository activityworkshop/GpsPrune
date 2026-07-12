package tim.prune.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtensionInfo
{
	/**
	 * Prefix and identifier of a single extension
	 */
	private record Extension(String prefix, String identifier) {
	}

	private String _namespace = null;
	private String _xsi = null;
	private final ArrayList<Extension> _extensions = new ArrayList<>();
	private final HashMap<String, String> _xsdLinks = new HashMap<>();
	private String _identifier = null;

	public void setNamespace(String inNamespace) {
		_namespace = inNamespace;
	}

	public String getNamespace() {
		return _namespace;
	}

	public void setXsi(String inXsi) {
		_xsi = inXsi;
	}

	public String getXsi() {
		return _xsi;
	}

	public void addNamespace(String inPrefix, String inUrl) {
		_extensions.add(new Extension(inPrefix, inUrl));
	}

	public void addXsiAttribute(String inAttribute)
	{
		if (inAttribute == null || inAttribute.isEmpty()) {
			return;
		}
		if (_identifier == null) {
			_identifier = inAttribute;
		}
		else {
			_xsdLinks.put(_identifier, inAttribute);
			_identifier = null;
		}
	}

	public List<String> getExtensions()
	{
		ArrayList<String> result = new ArrayList<>();
		for (Extension extn : _extensions) {
			result.add(extn.identifier());
		}
		return result;
	}

	public List<String> getExtraNamespaces()
	{
		ArrayList<String> result = new ArrayList<>();
		for (Extension extn : _extensions) {
			result.add("xmlns:" + extn.prefix() + "=\"" + extn.identifier() + "\"");
		}
		return result;
	}

	public String getSchemaLocations()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(_namespace).append(' ').append(_xsdLinks.getOrDefault(_namespace, ""));
		for (Extension extn : _extensions)
		{
			builder.append(' ').append(extn.identifier())
				.append(' ').append(_xsdLinks.getOrDefault(extn.identifier(), ""));
		}
		return builder.toString();
	}

	public String getNamespaceName(String inPrefix)
	{
		for (Extension extn : _extensions)
		{
			if (extn.prefix().equals(inPrefix))
			{
				final int lastSlashPos = extn.identifier().lastIndexOf('/');
				if (lastSlashPos > 0 && "Vv".indexOf(extn.identifier().charAt(lastSlashPos + 1)) >= 0) {
					final int prevSlashPos = extn.identifier().lastIndexOf('/', lastSlashPos - 1);
					if (prevSlashPos > 0) {
						return extn.identifier().substring(prevSlashPos + 1, lastSlashPos);
					}
				}
			}
		}
		return null;
	}
}
