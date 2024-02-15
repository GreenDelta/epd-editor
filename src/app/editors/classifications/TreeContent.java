package app.editors.classifications;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.CategorySystem;

class TreeContent implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object obj) {
		if (!(obj instanceof CategorySystem system))
			return null;
		return system.categories.toArray();
	}

	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof CategoryList list)
			return list.categories.toArray();
		if (obj instanceof Category category)
			return category.category.toArray();
		return null;
	}

	@Override
	public Object getParent(Object obj) {
		return null;
	}

	@Override
	public boolean hasChildren(Object obj) {
		if (obj instanceof CategoryList list)
			return !list.categories.isEmpty();
		if (obj instanceof Category category)
			return !category.category.isEmpty();
		return false;
	}
}
