package app.navi;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.openlca.ilcd.commons.DataSetType;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import app.editors.Editors;
import app.editors.classifications.ClassificationEditor;
import app.editors.connection.ConnectionEditor;
import app.editors.locations.LocationEditor;
import app.editors.profiles.ProfileEditor;
import app.util.UI;
import app.util.Viewers;

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
		var viewer = super.createCommonViewerObject(parent);
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
				Editors.open(refEl.content);
			} else if (obj instanceof ConnectionElement conEl) {
				ConnectionEditor.open(conEl.content);
			} else if (obj instanceof FileElement) {
				open((FileElement) obj);
			} else if (obj instanceof ProfileElement pe) {
				ProfileEditor.open(pe.content);
			}
		});
	}

	private void open(FileElement e) {
		if (e.getType() == null)
			return;
		switch (e.getType()) {
			case DOC:
				UI.open(e.content);
				break;
			case CLASSIFICATION:
				ClassificationEditor.open(e.content);
				break;
			case LOCATION:
				LocationEditor.open(e.content);
				break;
			default:
				break;
		}
	}

	public NavigationRoot getRoot() {
		return root;
	}

	public static TypeElement getTypeRoot(DataSetType type) {
		var navigator = getInstance();
		if (navigator == null || navigator.root == null)
			return new TypeElement(null, type);
		var children = navigator.root.getChilds();
		for (var e : children) {
			if (!(e instanceof TypeElement te))
				continue;
			if (te.content == type)
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

	private static void eachRoot(Consumer<NavigationElement<?>> fn) {
		try {
			var navi = Navigator.getInstance();
			if (navi == null)
				return;
			var root = navi.root;
			if (root == null || root.childs == null)
				return;
			for (var e : root.getChilds()) {
				fn.accept(e);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Navigator.class);
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

	public static void refresh(NavigationElement<?> e) {
		var viewer = getViewer();
		if (e == null || viewer == null)
			return;
		var oldExpansion = viewer.getExpandedElements();
		e.update();
		viewer.refresh(e);
		setRefreshedExpansion(viewer, oldExpansion);
	}

	private static void setRefreshedExpansion(
		CommonViewer viewer, Object[] oldExpansion) {
		if (viewer == null || oldExpansion == null)
			return;
		var newExpanded = new ArrayList<NavigationElement<?>>();
		for (var e : oldExpansion) {
			if (!(e instanceof NavigationElement<?> oldElem))
				continue;
			var newElem = findElement(oldElem.getContent());
			if (newElem != null) {
				newExpanded.add(newElem);
			}
		}
		viewer.setExpandedElements(newExpanded.toArray());
	}

	public static NavigationElement<?> findElement(Object content) {
		var root = getNavigationRoot();
		if (content == null || root == null)
			return null;
		var queue = new ArrayDeque<NavigationElement<?>>();
		queue.add(root);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			if (Objects.equal(next.getContent(), content))
				return next;
			queue.addAll(next.getChilds());
		}
		return null;
	}

	public static NavigationRoot getNavigationRoot() {
		var navigator = getInstance();
		return navigator != null
			? navigator.getRoot()
			: null;
	}

	public static CommonViewer getViewer() {
		var navi = getInstance();
		return navi == null
			? null
			: navi.getCommonViewer();
	}

	public static Navigator getInstance() {
		var workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		var window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		var page = window.getActivePage();
		if (page == null)
			return null;
		var part = page.findView(ID);
		return part instanceof Navigator navi
			? navi
			: null;
	}

}
