package app.navi;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.openlca.ilcd.io.SodaConnection;

import app.M;
import app.editors.Editors;
import app.editors.classifications.ClassificationEditor;
import app.editors.connection.ConnectionEditor;
import app.editors.connection.EpdProfileDownload;
import app.editors.locations.LocationEditor;
import app.editors.profiles.ProfileEditor;
import app.navi.actions.ClassificationSync;
import app.navi.actions.ConnectionDeleteAction;
import app.navi.actions.DuplicateAction;
import app.navi.actions.FileDeletion;
import app.navi.actions.FileImport;
import app.navi.actions.NewConnectionAction;
import app.navi.actions.NewDataSetAction;
import app.navi.actions.ProfileDeleteAction;
import app.navi.actions.ProfileExportAction;
import app.navi.actions.ProfileImportAction;
import app.navi.actions.RefDeleteAction;
import app.rcp.Icon;
import app.store.Connections;
import app.store.validation.ValidationDialog;
import app.util.Actions;
import app.util.UI;
import app.util.Viewers;

public class NavigationMenu extends CommonActionProvider {

	@Override
	public void fillActionBars(IActionBars actionBars) {

		if (actionBars == null)
			return;
		IMenuManager menu = actionBars.getMenuManager();
		if (menu == null)
			return;

		Action collapseAll = Actions.create(
				"Collapse all", Icon.COLLAPSE.des(), () -> {
					CommonViewer viewer = Navigator.getViewer();
					if (viewer != null) {
						viewer.collapseAll();
					}
				});
		menu.add(collapseAll);

		Action expandAll = Actions.create(
				"Expand all", Icon.EXPAND.des(), () -> {
					CommonViewer viewer = Navigator.getViewer();
					if (viewer != null) {
						viewer.expandAll();
					}
				});
		menu.add(expandAll);

	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext con = getContext();
		IStructuredSelection s = (IStructuredSelection) con
				.getSelection();
		List<NavigationElement> elements = Viewers.getAll(s);
		if (elements.isEmpty())
			return;
		NavigationElement first = elements.get(0);
		if (first instanceof TypeElement) {
			TypeElement e = (TypeElement) first;
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof CategoryElement) {
			CategoryElement e = (CategoryElement) first;
			menu.add(new NewDataSetAction(e));
		}
		if (first instanceof FolderElement) {
			FolderElement e = (FolderElement) first;
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

		if (first instanceof ProfileFolder) {
			menu.add(new ProfileImportAction());
		}

		if (first instanceof ProfileElement) {
			ProfileElement pe = (ProfileElement) first;
			menu.add(Actions.create(M.Open, Icon.OPEN.des(),
					() -> ProfileEditor.open(pe.profile)));
			menu.add(new ProfileExportAction(pe.profile));
			menu.add(new ProfileDeleteAction(pe.profile));
		}

		if (first instanceof ConnectionFolder) {
			ConnectionFolder cf = (ConnectionFolder) first;
			menu.add(new NewConnectionAction(cf));
		}

		if (first instanceof ConnectionElement) {
			ConnectionElement e = (ConnectionElement) first;
			menu.add(Actions.create(M.Open, Icon.OPEN.des(),
					() -> ConnectionEditor.open(e.con)));
			menu.add(Actions.create(M.DownloadEPDProfiles,
					Icon.DOWNLOAD.des(), () -> {
						EpdProfileDownload.runInUI(e.con.url);
					}));
			menu.add(new ConnectionDeleteAction(e));
		}
	}

	private void forRef(RefElement e, IMenuManager menu) {
		menu.add(Actions.create(M.Open, Icon.OPEN.des(),
				() -> Editors.open(e.ref)));
		menu.add(Actions.create(M.Validate, Icon.OK.des(),
				() -> ValidationDialog.open(e.ref)));
		menu.add(new DuplicateAction(e));
		menu.add(new RefDeleteAction(e));
	}

	private void forFile(FileElement e, IMenuManager menu) {
		open(e, menu);
		menu.add(new FileDeletion(e));
		if (e.getType() == FolderType.CLASSIFICATION) {
			String name = e.file.getName();
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
		if (e == null || e.getType() == null || e.file == null)
			return;
		Runnable fn = null;
		switch (e.getType()) {
		case CLASSIFICATION:
			fn = () -> ClassificationEditor.open(e.file);
			break;
		case LOCATION:
			fn = () -> LocationEditor.open(e.file);
			break;
		case DOC:
			fn = () -> UI.open(e.file);
			break;
		default:
			break;
		}
		if (fn == null)
			return;
		menu.add(Actions.create(M.Open, Icon.OPEN.des(), fn));
	}

}
