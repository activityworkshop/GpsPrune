package tim.prune.save.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * For GPX 1.1, the extensions appear in a tree of categories, so this tree
 * needs to be rebuilt for export depending on which categories are present
 */
public class ExtensionTree
{
	/** Single category containing tags and subcategories */
	private static class Category
	{
		private final String _name;
		private final ArrayList<Category> _subcategories = new ArrayList<>();
		private final ArrayList<String> _tags = new ArrayList<>();

		Category(String inName) {
			_name = inName;
		}

		Category getSubcategory(String inName)
		{
			for (Category subcat : _subcategories)
			{
				if (subcat._name.equals(inName)) {
					return subcat;
				}
			}
			Category subcat = new Category(inName);
			_subcategories.add(subcat);
			return subcat;
		}

		void addTag(String inTag) {
			_tags.add(inTag);
		}
	}

	/** Root category of tree */
	private final Category _root = new Category(null);

	/**
	 * Add a tag to the tree
	 * @param inCategories array of category strings
	 * @param inTag xml tag inside category
	 */
	public void addTag(String[] inCategories, String inTag)
	{
		if (inTag == null || inTag.equals("")) {
			return;
		}
		Category category = _root;
		if (inCategories != null)
		{
			for (String catName : inCategories) {
				category = category.getSubcategory(catName);
			}
		}
		category.addTag(inTag);
	}

	/**
	 * @return a list of all the tags, including categories and subcategories
	 */
	public List<String> getAllTags(int inStartIndent)
	{
		ArrayList<String> tags = new ArrayList<>();
		addTags(_root, tags, inStartIndent);
		return tags;
	}

	/** Recursively add the tags and subcategories to the given list */
	private static void addTags(Category inCategory, ArrayList<String> inTags, int inIndent)
	{
		for (String tag : inCategory._tags) {
			inTags.add("\t".repeat(inIndent) + tag);
		}
		for (Category subcat : inCategory._subcategories)
		{
			inTags.add("\t".repeat(inIndent) + "<" + subcat._name + ">");
			addTags(subcat, inTags, inIndent + 1);
			inTags.add("\t".repeat(inIndent) + "</" + subcat._name + ">");
		}
	}
}
