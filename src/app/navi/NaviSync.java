package app.navi;

import epd.index.CategoryNode;
import epd.index.Index;
import epd.index.Node;
import epd.index.TypeNode;
import epd.util.Strings;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import java.util.List;
import java.util.Objects;

/**
 * Synchronizes the navigation with an index.
 */
public class NaviSync implements Runnable {

	private final Index index;

	public NaviSync(Index index) {
		this.index = index;
	}

	@Override
	public void run() {
		if (index == null)
			return;
		index.removeEmptyCategories();
		for (DataSetType type : DataSetType.values()) {
			TypeElement elem = Navigator.getTypeRoot(type);
			TypeNode node = index.getNode(type);
			if (node == null)
				continue;
			sync(node, elem);
		}
		Navigator.refreshAll();
	}

	private void sync(Node node, NavigationElement parent) {
		if (parent.childs == null)
			return;
		remove(node, parent);
		syncRefs(node, parent);
		for (CategoryNode cat : node.categories) {
			CategoryElement elem = findChild(cat.category, parent);
			if (elem == null) {
				elem = new CategoryElement(parent, cat);
				parent.childs.add(elem);
			}
			sync(cat, elem);
		}
	}

	private void syncRefs(Node node, NavigationElement parent) {
		for (Ref ref : node.refs) {
			RefElement elem = findChild(ref, parent);
			if (elem != null) {
				elem.content = ref;
			} else {
				elem = new RefElement(parent, ref);
				parent.childs.add(elem);
			}
		}
	}

	private RefElement findChild(Ref ref, NavigationElement parent) {
		var children = (List<NavigationElement>)parent.getChilds();
		for (NavigationElement child : children) {
			if (!(child instanceof RefElement re))
				continue;
			if (Objects.equals(re.content, ref))
				return re;
		}
		return null;
	}

	private CategoryElement findChild(Category cat, NavigationElement parent) {
		if (cat == null)
			return null;
		var children = (List<NavigationElement>)parent.getChilds();
		for (NavigationElement child : children) {
			if (!(child instanceof CategoryElement ca))
				continue;
			if (ca.getCategory() == null)
				continue;
			if (Strings.nullOrEqual(ca.getCategory().getName(), cat.getName()))
				return ca;
		}
		return null;
	}

	private void remove(Node node, NavigationElement parent) {
		parent.childs.removeIf(elem -> {
			if (elem instanceof CategoryElement ce) {
				return !contains(node, ce.getCategory());
			}
			if (elem instanceof RefElement re) {
				return !node.refs.contains(re.content);
			}
			return false;
		});
	}

	private boolean contains(Node node, Category category) {
		if (category == null)
			return false;
		for (CategoryNode cn : node.categories) {
			if (cn.category == null)
				continue;
			if (Strings.nullOrEqual(cn.category.getName(), category.getName()))
				return true;
		}
		return false;
	}

}
