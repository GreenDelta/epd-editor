package epd.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;

import epd.util.Strings;

public class TypeNode extends Node {

	public final DataSetType type;

	public TypeNode(DataSetType type) {
		this.type = type;
	}

	List<CategoryNode> syncCategories(List<Classification> classes) {
		if (classes == null || classes.isEmpty())
			return Collections.emptyList();
		List<CategoryNode> nodes = new ArrayList<>();
		for (Classification c : classes) {
			c.categories.sort(Comparator.comparingInt(cat -> cat.level));
			CategoryNode node = null;
			List<CategoryNode> childs = categories;
			for (Category cat : c.categories) {
				node = sync(cat, childs);
				childs = node.categories;
			}
			if (node != null)
				nodes.add(node);
		}
		return nodes;
	}

	private CategoryNode sync(Category category, List<CategoryNode> nodes) {
		for (CategoryNode node : nodes) {
			Category other = node.category;
			if (other == null)
				continue;
			if (Strings.nullOrEqual(category.value, other.value))
				return node;
		}
		CategoryNode node = new CategoryNode();
		node.category = category;
		nodes.add(node);
		return node;
	}

}
