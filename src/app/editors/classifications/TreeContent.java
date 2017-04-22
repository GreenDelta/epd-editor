package app.editors.classifications;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.openlca.ilcd.lists.Category;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.CategorySystem;

class TreeContent implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object obj) {
		if (!(obj instanceof CategorySystem))
			return null;
		CategorySystem system = (CategorySystem) obj;
		return system.categories.toArray();
	}

	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof CategoryList)
			return ((CategoryList) obj).categories.toArray();
		if (obj instanceof Category)
			return ((Category) obj).category.toArray();
		return null;
	}

	@Override
	public Object getParent(Object obj) {
		return null;
	}

	@Override
	public boolean hasChildren(Object obj) {
		if (obj instanceof CategoryList)
			return !((CategoryList) obj).categories.isEmpty();
		if (obj instanceof Category)
			return !((Category) obj).category.isEmpty();
		return false;
	}
}
