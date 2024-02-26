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
		return system.getCategories().toArray();
	}

	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof CategoryList list)
			return list.getCategories().toArray();
		if (obj instanceof Category category)
			return category.getCategories().toArray();
		return null;
	}

	@Override
	public Object getParent(Object obj) {
		return null;
	}

	@Override
	public boolean hasChildren(Object obj) {
		if (obj instanceof CategoryList list)
			return !list.getCategories().isEmpty();
		if (obj instanceof Category category)
			return !category.getCategories().isEmpty();
		return false;
	}
}
