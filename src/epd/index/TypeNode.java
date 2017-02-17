package epd.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;

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
			Collections.sort(c.categories, (c1, c2) -> c1.level - c2.level);
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
			if (Objects.equals(category, node.category))
				return node;
		}
		CategoryNode node = new CategoryNode();
		node.category = category;
		nodes.add(node);
		return node;
	}

}
