package app.navi;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
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

import app.editors.contact.ContactEditor;
import app.editors.epd.EpdEditor;
import app.editors.flowproperty.FlowPropertyEditor;
import app.editors.source.SourceEditor;
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
		CommonViewer viewer = super.createCommonViewerObject(parent);
		viewer.setUseHashlookup(true);
		return viewer;
	}

	@Override
	protected void initListeners(TreeViewer viewer) {
		super.initListeners(viewer);
		viewer.setUseHashlookup(true);
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				onDoubleClick(event.getSelection());
			}
		});
	}

	private void onDoubleClick(ISelection selection) {
		Object obj = Viewers.getFirst(selection);
		if (obj instanceof RefElement) {
			RefElement e = (RefElement) obj;
			if (e.ref.type == DataSetType.PROCESS)
				EpdEditor.open(e.ref);
			else if (e.ref.type == DataSetType.CONTACT)
				ContactEditor.open(e.ref);
			else if (e.ref.type == DataSetType.SOURCE)
				SourceEditor.open(e.ref);
			else if (e.ref.type == DataSetType.FLOW_PROPERTY)
				FlowPropertyEditor.open(e.ref);
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
			if (!(e instanceof TypeElement))
				continue;
			TypeElement te = (TypeElement) e;
			if (te.type == type)
				return te;
		}
		return new TypeElement(null, type);
	}

	public static void refresh() {
		CommonViewer viewer = getNavigationViewer();
		NavigationRoot root = getNavigationRoot();
		if (viewer != null && root != null) {
			root.update();
			viewer.refresh();
		}
	}

	public static void refresh(NavigationElement e) {
		if (e == null)
			return;
		e.update();
		CommonViewer viewer = getNavigationViewer();
		viewer.refresh(e);
	}

	private static CommonViewer getNavigationViewer() {
		CommonViewer viewer = null;
		Navigator instance = getInstance();
		if (instance != null) {
			viewer = instance.getCommonViewer();
		}
		return viewer;
	}

	private static NavigationRoot getNavigationRoot() {
		NavigationRoot root = null;
		Navigator navigator = getInstance();
		if (navigator != null)
			root = navigator.getRoot();
		return root;
	}

	private static Navigator getInstance() {
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
