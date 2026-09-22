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
	public static TreeViewer viewer(Composite parent) {
		return viewer(parent, SWT.SINGLE);
	}

	/// See [viewer(Composite)]. The given style is used for the selection
	/// behaviour of the viewer, e.g. [SWT#MULTI] for selecting multiple
	/// elements at once.
	@SuppressWarnings("deprecation")
	public static TreeViewer viewer(Composite parent, int selectionStyle) {
		var viewer = new TreeViewer(parent, SWT.BORDER | selectionStyle);
		viewer.setContentProvider(new NavigationContent());
		viewer.setLabelProvider(new NavigationLabel());
		viewer.setSorter(new NavigationSorter());
		ColumnViewerToolTipSupport.enableFor(viewer);
		return viewer;
	}

}
