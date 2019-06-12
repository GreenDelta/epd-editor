package app.editors.epd.contents;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import app.M;
import app.editors.epd.EpdEditor;
import app.util.Actions;
import app.util.Tables;
import app.util.UI;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.util.Fn;

class ContentTable {

	private final EpdEditor editor;
	private final ContentDeclaration decl;

	boolean forPackaging;
	private TableViewer table;

	public ContentTable(EpdEditor editor, ContentDeclaration decl) {
		this.editor = editor;
		this.decl = decl;
	}

	void render(FormToolkit tk, Composite body) {
		String title = forPackaging
				? "Packaging materials"
				: "Components and materials";
		Section section = UI.section(body, tk, title);
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		createTable(comp);
		createMenu();
	}

	private void createTable(Composite comp) {
		table = Tables.createViewer(comp,
				"Component/material",
				"weight-%",
				"kg",
				"CAS No",
				"EC No",
				"GUUID",
				"Renewable resource",
				"Recycled content",
				"Recyclable content",
				"Comment");
		table.getTable().getColumn(5).setToolTipText(
				"Data dictionary GUUID");
		table.getTable().getColumn(7).setToolTipText(
				"Post cosumer material recycled content");
		table.getTable().getColumn(8).setToolTipText(
				"Material recyclable content");
		double w = 0.8 / 9;
		Tables.bindColumnWidths(table, 0.2, w, w, w, w, w, w, w, w, w);
	}

	private void createMenu() {
		MenuManager menu = new MenuManager();
		MenuManager addMenu = new MenuManager(M.Add);
		menu.add(addMenu);
		if (!forPackaging) {
			addMenu.add(Actions.create("Component",
					() -> onAdd(ContentType.COMPONENT)));
		}
		addMenu.add(Actions.create("Material",
				() -> onAdd(ContentType.MATERIAL)));
		addMenu.add(Actions.create("Substance",
				() -> onAdd(ContentType.SUBSTANCE)));
		menu.add(Actions.create("Edit", this::onEdit));
		menu.add(Actions.create(M.Delete, this::onDelete));
		Fn.with(table.getTable(),
				table -> table.setMenu(menu.createContextMenu(table)));

	}

	private void onAdd(ContentType type) {
		if (type == null)
			return;
		ContentElement elem = type.newInstance();
		if (ContentDialog.open(decl, elem) != Dialog.OK)
			return;
		editor.setDirty();
	}

	private void onEdit() {
		// TODO
		// ContentDialog.open(null);
	}

	private void onDelete() {
		// TODO
		// ContentDialog.open(null);
	}

}
