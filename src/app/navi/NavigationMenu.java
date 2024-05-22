package app.navi;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.editors.Editors;
import app.editors.classifications.ClassificationEditor;
import app.editors.connection.ConnectionEditor;
import app.editors.locations.LocationEditor;
import app.navi.actions.ClassificationSync;
import app.navi.actions.ConnectionDeleteAction;
import app.navi.actions.DuplicateAction;
import app.navi.actions.FileDeletion;
import app.navi.actions.FileImport;
import app.navi.actions.NewConnectionAction;
import app.navi.actions.NewDataSetAction;
import app.navi.actions.RefDeleteAction;
import app.rcp.Icon;
import app.store.Connections;
import app.store.ExportDialog;
import app.store.validation.ValidationDialog;
import app.util.Actions;
import app.util.UI;
import app.util.Viewers;

public class NavigationMenu extends CommonActionProvider {

	private boolean menuAdded = false;

	@Override
	public void fillActionBars(IActionBars actionBars) {

		if (actionBars == null || menuAdded)
			return;
		var menu = actionBars.getMenuManager();
		if (menu == null)
			return;

		var collapseAll = Actions.create(
			"Collapse all", Icon.COLLAPSE.des(), () -> {
				CommonViewer viewer = Navigator.getViewer();
				if (viewer != null) {
					viewer.collapseAll();
				}
			});
		menu.add(collapseAll);

		var expandAll = Actions.create(
			"Expand all", Icon.EXPAND.des(), () -> {
				CommonViewer viewer = Navigator.getViewer();
				if (viewer != null) {
					viewer.expandAll();
				}
			});
		menu.add(expandAll);

		var refresh = Actions.create(
			"Refresh", Icon.RELOAD.des(), Navigator::refreshAll);
		menu.add(refresh);

		menuAdded = true;
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		var con = getContext();
		var s = (IStructuredSelection) con.getSelection();
		List<NavigationElement<?>> elements = Viewers.getAll(s);
		if (elements.isEmpty())
			return;
		var first = elements.get(0);
		if (first instanceof TypeElement e) {
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof CategoryElement e) {
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof FolderElement e) {
			if (e.type == FolderType.CLASSIFICATION)
				categorySync(menu, null);
			menu.add(new FileImport(e));
		}
		if (first instanceof RefElement) {
			forRef((RefElement) first, menu);
		}
		if (first instanceof FileElement) {
			forFile((FileElement) first, menu);
		}

		if (first instanceof ConnectionFolder cf) {
			menu.add(new NewConnectionAction(cf));
		}

		if (first instanceof ConnectionElement e) {
			menu.add(Actions.create(M.Open, Icon.OPEN.des(),
					() -> ConnectionEditor.open(e.content)));
			menu.add(new ConnectionDeleteAction(e));
		}
	}

	private void forRef(RefElement e, IMenuManager menu) {
		menu.add(Actions.create(M.Open, Icon.OPEN.des(),
			() -> Editors.open(e.content)));
		menu.add(Actions.create(M.Validate, Icon.OK.des(),
			() -> ValidationDialog.open(e.content)));
		menu.add(new DuplicateAction(e));
		menu.add(Actions.create(M.Export, Icon.EXPORT.des(),
			() -> ExportDialog.open(e.content)));
		menu.add(new RefDeleteAction(e));
	}

	private void forFile(FileElement e, IMenuManager menu) {
		open(e, menu);
		menu.add(new FileDeletion(e));
		if (e.getType() == FolderType.CLASSIFICATION) {
			String name = e.content.getName();
			if (name.toLowerCase().endsWith(".xml")) {
				name = name.substring(0, name.length() - 4);
			}
			categorySync(menu, name);
		}
	}

	private void categorySync(IMenuManager menu, String systemName) {
		MenuManager syncMenu = new MenuManager(M.Update,
			Icon.DOWNLOAD.des(), "UpdateClassifications");
		menu.add(syncMenu);
		for (SodaConnection con : Connections.get()) {
			syncMenu.add(new ClassificationSync(con, systemName));
		}
	}

	private void open(FileElement e, IMenuManager menu) {
		if (e == null || e.getType() == null || e.content == null)
			return;
		Runnable fn = null;
		switch (e.getType()) {
			case CLASSIFICATION:
				fn = () -> ClassificationEditor.open(e.content);
				break;
			case LOCATION:
				fn = () -> LocationEditor.open(e.content);
				break;
			case DOC:
				fn = () -> UI.open(e.content);
				break;
			default:
				break;
		}
		if (fn == null)
			return;
		menu.add(Actions.create(M.Open, Icon.OPEN.des(), fn));
	}

}
