package epd.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

public class TypeNode {

	public final DataSetType type;
	public final Set<Ref> refs = new HashSet<>();
	public final List<CategoryNode> categories = new ArrayList<>();

	public TypeNode(DataSetType type) {
		this.type = type;
	}

	void remove(Ref ref) {
		if (ref == null)
			return;
		refs.remove(ref);
		for (CategoryNode cat : categories)
			cat.remove(ref);
	}

	Ref find(Ref ref) {
		if (ref == null)
			return null;
		for (Ref r : refs) {
			if (Objects.equals(r.uuid, ref.uuid))
				return r;
		}
		for (CategoryNode n : categories) {
			Ref r = n.find(ref);
			if (r != null)
				return r;
		}
		return null;
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
				childs = node.childs;
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
