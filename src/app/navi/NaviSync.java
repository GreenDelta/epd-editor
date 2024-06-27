package app.navi;

import java.util.Objects;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import epd.index.CategoryNode;
import epd.index.Index;
import epd.index.Node;
import epd.util.Strings;

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
		for (var type : DataSetType.values()) {
			var elem = Navigator.getTypeRoot(type);
			var node = index.getNode(type);
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
		for (var cat : node.categories) {
			var elem = findChild(cat.category, parent);
			if (elem == null) {
				elem = new CategoryElement(parent, cat);
				parent.childs.add(elem);
			}
			sync(cat, elem);
		}
	}

	private void syncRefs(Node node, NavigationElement parent) {
		for (Ref ref : node.refs) {
			var elem = findChild(ref, parent);
			if (elem != null) {
				elem.setRef(ref);
			} else {
				elem = new RefElement(parent, ref);
				parent.childs.add(elem);
			}
		}
	}

	private RefElement findChild(Ref ref, NavigationElement parent) {
		var children = parent.getChilds();
		for (var child : children) {
			if (!(child instanceof RefElement re))
				continue;
			if (Objects.equals(re.ref(), ref))
				return re;
		}
		return null;
	}

	private CategoryElement findChild(Category cat, NavigationElement parent) {
		if (cat == null)
			return null;
		var children = parent.getChilds();
		for (var child : children) {
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
				return !node.refs.contains(re.ref());
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
