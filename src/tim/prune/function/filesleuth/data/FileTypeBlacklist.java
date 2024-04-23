package tim.prune.function.filesleuth.data;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;

/**
 * Blacklists certain file types from the search results,
 * assuming that they can never represent coordinate data.
 */
public class FileTypeBlacklist
{
	private static HashSet<String> _badExtensions = null;

	public static boolean isAllowed(Path inPath)
	{
		if (inPath == null) {
			return false;
		}
		if (_badExtensions == null) {
			makeExtensionSet();
		}
		String extension = getExtension(inPath);
		return extension.isEmpty() || !_badExtensions.contains(extension);
	}

	static String getExtension(Path inPath)
	{
		Path filenamePath = (inPath == null ? null : inPath.getFileName());
		if (filenamePath == null) {return "";}
		String filename = filenamePath.toString().toLowerCase();
		int dotPos = filename.lastIndexOf('.');
		if (dotPos < 0) {
			return ""; // no dot found, so no extension
		}
		return filename.substring(dotPos + 1);
	}

	private static synchronized void makeExtensionSet()
	{
		HashSet<String> extensions = new HashSet<>();
		String endings = "gif;png;jpg;jpeg;xcf;mov;mkv;mp4;mp3;avi;wav;au;ogg;ogv;"
				+ "pptx;docx;xlsx;ppt;doc;xls;odt;ods;pdf;eps;svg;jar;java;class;py;pyc;php;"
				+ "html;css;md;js;img;tgz;iso;img;bin;sh;bat;pruneconfig";
		Collections.addAll(extensions, endings.split(";"));
		_badExtensions = extensions;
	}
}
