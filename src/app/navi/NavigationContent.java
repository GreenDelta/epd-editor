package app.navi;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

import java.util.List;

public class NavigationContent implements ICommonContentProvider {

	@Override
	public Object[] getElements(Object input) {
		return getChildren(input);
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (!(parent instanceof NavigationElement e))
			return new Object[0];
		List<NavigationElement> childs = e.getChilds();
		if (childs == null)
			return new Object[0];
		else
			return childs.toArray();
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof NavigationElement)
			return ((NavigationElement) element).getParent();
		else
			return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (!(element instanceof NavigationElement e))
			return false;
		return !e.getChilds().isEmpty();
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
