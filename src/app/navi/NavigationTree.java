package app.navi;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Factory methods for creating navigation trees in the application.
 */
public class NavigationTree {

	/**
	 * Creates a tree viewer with the same content provider, label provider,
	 * etc. as in the navigation tree. This viewer accepts an instance
	 * {@link NavigationElement} as input.
	 */
	@SuppressWarnings("deprecation")
	public static TreeViewer viewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE);
		viewer.setContentProvider(new NavigationContent());
		viewer.setLabelProvider(new NavigationLabel());
		viewer.setSorter(new NavigationSorter());
		ColumnViewerToolTipSupport.enableFor(viewer);
		return viewer;
	}

}
