package app.navi;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

public class NavigationContent implements ICommonContentProvider {

	@Override
	public Object[] getElements(Object input) {
		return getChildren(input);
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (!(parent instanceof NavigationElement<?> e))
			return new Object[0];
		var childs = e.getChilds();
		return childs != null
			? childs.toArray()
			: new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return element instanceof NavigationElement<?> e
			? e.getParent()
			: null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof NavigationElement<?> e
			&& e.getChilds() != null
			&& !e.getChilds().isEmpty();
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
	}

}
