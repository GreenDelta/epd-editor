package app.navi;

import java.util.Objects;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;

import epd.index.CategoryNode;
import epd.index.Index;
import epd.index.Node;
import epd.index.TypeNode;
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
		for (DataSetType type : DataSetType.values()) {
			TypeElement elem = Navigator.getTypeRoot(type);
			TypeNode node = index.getNode(type);
			if (node == null)
				continue;
			sync(node, elem);
		}
		Navigator.refreshFolders();
		Navigator.refresh();
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
				elem.ref = ref;
			} else {
				elem = new RefElement(parent, ref);
				parent.childs.add(elem);
			}
		}
	}

	private RefElement findChild(Ref ref, NavigationElement parent) {
		for (NavigationElement child : parent.childs) {
			if (!(child instanceof RefElement))
				continue;
			RefElement re = (RefElement) child;
			if (Objects.equals(re.ref, ref))
				return re;
		}
		return null;
	}

	private CategoryElement findChild(Category cat, NavigationElement parent) {
		if (cat == null)
			return null;
		for (NavigationElement child : parent.childs) {
			if (!(child instanceof CategoryElement))
				continue;
			CategoryElement ca = (CategoryElement) child;
			if (ca.getCategory() == null)
				continue;
			if (Strings.nullOrEqual(ca.getCategory().value, cat.value))
				return ca;
		}
		return null;
	}

	private void remove(Node node, NavigationElement parent) {
		parent.childs.removeIf(elem -> {
			if (elem instanceof CategoryElement) {
				CategoryElement ce = (CategoryElement) elem;
				return !contains(node, ce.getCategory());
			}
			if (elem instanceof RefElement) {
				RefElement re = (RefElement) elem;
				return !node.refs.contains(re.ref);
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
			if (Strings.nullOrEqual(cn.category.value, category.value))
				return true;
		}
		return false;
	}

}
