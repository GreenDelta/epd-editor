package app.navi;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

@SuppressWarnings("deprecation")
public class NavigationSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof NavigationElement<?> n1)
			|| !(e2 instanceof NavigationElement<?> n2))
			return super.compare(viewer, e1, e2);
		return n1.compareTo(n2);
	}

}
