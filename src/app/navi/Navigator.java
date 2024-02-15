package app.navi;

import app.editors.Editors;
import app.editors.classifications.ClassificationEditor;
import app.editors.connection.ConnectionEditor;
import app.editors.locations.LocationEditor;
import app.editors.profiles.ProfileEditor;
import app.util.UI;
import app.util.Viewers;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.openlca.ilcd.commons.DataSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Navigator extends CommonNavigator {

	public static final String ID = "app.navi.Navigator";

	private NavigationRoot root;

	@Override
	protected Object getInitialInput() {
		root = new NavigationRoot();
		return root;
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite parent) {
		CommonViewer viewer = super.createCommonViewerObject(parent);
		viewer.setUseHashlookup(true);
		return viewer;
	}

	@Override
	protected void initListeners(TreeViewer viewer) {
		super.initListeners(viewer);
		viewer.setUseHashlookup(true);
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.addDoubleClickListener(e -> {
			Object obj = Viewers.getFirstSelected(viewer);
			if (obj instanceof RefElement refEl) {
				Editors.open(refEl.ref);
			} else if (obj instanceof ConnectionElement conEl) {
				ConnectionEditor.open(conEl.con);
			} else if (obj instanceof FileElement) {
				open((FileElement) obj);
			} else if (obj instanceof ProfileElement pe) {
				ProfileEditor.open(pe.profile);
			}
		});
	}

	private void open(FileElement e) {
		if (e.getType() == null)
			return;
		switch (e.getType()) {
			case DOC:
				UI.open(e.file);
				break;
			case CLASSIFICATION:
				ClassificationEditor.open(e.file);
				break;
			case LOCATION:
				LocationEditor.open(e.file);
				break;
			default:
				break;
		}
	}

	public NavigationRoot getRoot() {
		return root;
	}

	public static TypeElement getTypeRoot(DataSetType type) {
		Navigator navigator = getInstance();
		if (navigator == null || navigator.root == null)
			return new TypeElement(null, type);
		for (NavigationElement e : navigator.root.getChilds()) {
			if (!(e instanceof TypeElement te))
				continue;
			if (te.type == type)
				return te;
		}
		return new TypeElement(null, type);
	}

	public static void refreshConnections() {
		eachRoot(e -> {
			if (e instanceof ConnectionFolder) {
				refresh(e);
			}
		});
	}

	public static void refreshProfiles() {
		eachRoot(e -> {
			if (e instanceof ProfileFolder) {
				refresh(e);
			}
		});
	}

	public static void refreshFolders() {
		eachRoot(e -> {
			if (e instanceof FolderElement) {
				refresh(e);
			}
		});
	}

	private static void eachRoot(Consumer<NavigationElement> fn) {
		try {
			Navigator navi = Navigator.getInstance();
			if (navi == null)
				return;
			NavigationRoot root = navi.root;
			if (root == null || root.childs == null)
				return;
			for (NavigationElement e : root.childs) {
				fn.accept(e);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Navigator.class);
			log.error("eachRoot failed", e);
		}
	}

	/**
	 * Refreshes the navigation viewer. It does not update the content of the
	 * navigation elements (just the labels etc.). If you want to updated also
	 * the content of all elements, you should call {@link #refreshAll()} instead.
	 */
	public static void refreshViewer() {
		var viewer = getViewer();
		if (viewer != null) {
			viewer.refresh();
		}
	}

	/**
	 * Updates the content of the navigation tree and refreshes the viewer. For
	 * performance reasons you should only do this when you want to update the
	 * complete tree.
	 */
	public static void refreshAll() {
		var instance = getInstance();
		if (instance == null)
			return;
		refresh(instance.getRoot());
	}

	public static void refresh(NavigationElement e) {
		if (e == null)
			return;
		e.update();
		var viewer = getViewer();
		if (viewer != null) {
			viewer.refresh(e);
		}
	}

	public static CommonViewer getViewer() {
		Navigator navi = getInstance();
		return navi == null
			? null
			: navi.getCommonViewer();
	}

	public static Navigator getInstance() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;
		IViewPart part = page.findView(ID);
		if (part instanceof Navigator)
			return (Navigator) part;
		return null;
	}

}
