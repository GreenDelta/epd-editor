package app.editors.epd.contents;

import app.App;
import app.M;
import app.editors.epd.EpdEditor;
import app.util.Actions;
import app.util.Trees;
import app.util.UI;
import app.util.Viewers;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.processes.epd.EpdContentAmount;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdContentElement;
import org.openlca.ilcd.processes.epd.EpdInnerContentElement;

import java.util.List;
import java.util.stream.IntStream;

class ContentTree {

	private final EpdEditor editor;
	private final EpdContentDeclaration decl;

	boolean forPackaging;
	private TreeViewer tree;

	public ContentTree(EpdEditor editor, EpdContentDeclaration decl) {
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
		createTree(comp);
		createMenu();
		tree.setInput(decl);
		tree.expandAll();
	}

	private void createTree(Composite comp) {
		tree = Trees.createViewer(comp,
			"Component/material",
			"Weight percentage",
			"Absolute mass",
			"CAS number",
			"EC number",
			"Data dictionary GUUID",
			"Renewable resources",
			"Recycled content",
			"Recyclable content",
			"Comment");
		ViewerModel vm = new ViewerModel();
		tree.setContentProvider(vm);
		tree.setLabelProvider(vm);

		// filter packaging materials
		tree.setFilters(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parent, Object obj) {
				return obj instanceof EpdContentElement<?> elem
					&& forPackaging == Content.isPackaging(elem);
			}
		});

		Tree t = tree.getTree();
		IntStream.of(1, 2, 6, 7, 8)
			.forEach(i -> t.getColumn(i).setAlignment(SWT.RIGHT));
		double w = 0.8 / 9;
		Trees.bindColumnWidths(t, 0.2, w, w, w, w, w, w, w, w, w);
		Trees.onDoubleClick(tree, _e -> onEdit());
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
		if (tree != null) {
			var t = tree.getTree();
			t.setMenu(menu.createContextMenu(t));
		}

	}

	private void onAdd(ContentType type) {
		if (type == null)
			return;
		var elem = type.newInstance();
		if (elem instanceof EpdInnerContentElement<?> i) {
			i.withPackaging(forPackaging);
		}
		if (ContentDialog.open(decl, elem) != Dialog.OK)
			return;
		tree.setInput(decl);
		editor.setDirty();
		tree.expandToLevel(elem, TreeViewer.ALL_LEVELS);
	}

	private void onEdit() {
		EpdContentElement<?> elem = Viewers.getFirstSelected(tree);
		if (elem == null)
			return;
		if (ContentDialog.open(decl, elem) != Dialog.OK)
			return;
		tree.setInput(decl);
		editor.setDirty();
	}

	private void onDelete() {
		EpdContentElement<?> elem = Viewers.getFirstSelected(tree);
		if (elem == null)
			return;
		Object[] expanded = tree.getExpandedElements();
		Content.remove(decl, elem);
		tree.setInput(decl);
		tree.setExpandedElements(expanded);
		editor.setDirty();
	}

	private class ViewerModel extends BaseLabelProvider
		implements ITreeContentProvider, ITableLabelProvider {

		@Override
		public Object[] getElements(Object obj) {
			return obj instanceof EpdContentDeclaration dec
				? dec.getElements().toArray()
				: null;
		}

		@Override
		public Object[] getChildren(Object obj) {
			if (!(obj instanceof EpdContentElement<?> elem))
				return null;
			List<?> childs = Content.childs(elem);
			return childs.isEmpty() ? null : childs.toArray();
		}

		@Override
		public Object getParent(Object obj) {
			return obj instanceof EpdContentElement<?> elem
				? Content.getParent(elem, decl)
				: null;
		}

		@Override
		public boolean hasChildren(Object obj) {
			return obj instanceof EpdContentElement<?> elem
				&& !Content.childs(elem).isEmpty();
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof EpdContentElement<?> elem))
				return null;

			var inner = elem instanceof EpdInnerContentElement<?> ii
				? ii : null;

			return switch (col) {
				case 0 -> App.s(elem.getName());
				case 1 -> amount(elem.getWeightPerc(), "%");
				case 2 -> amount(elem.getMass(), "kg");
				case 3 -> inner != null ? inner.getCasNumber() : null;
				case 4 -> inner != null ? inner.getEcNumber() : null;
				case 5 -> inner != null ? inner.getGuid() : null;
				case 6 -> inner != null ? perc(inner.getRenewable()) : null;
				case 7 -> inner != null ? perc(inner.getRecycled()) : null;
				case 8 -> inner != null ? perc(inner.getRecyclable()) : null;
				default -> null;
			};
		}

		private String amount(EpdContentAmount a, String unit) {
			if (a == null)
				return " - ";
			if (a.getValue() != null)
				return a.getValue() + " " + unit;
			if (a.getMin() != null && a.getMax() != null)
				return a.getMin() + " - " + a.getMax() + " " + unit;
			if (a.getMin() != null)
				return "> " + a.getMin() + " " + unit;
			if (a.getMax() != null)
				return "< " + a.getMax() + " " + unit;
			return " - ";
		}

		private String perc(Double num) {
			return num != null ? num + " %" : null;
		}
	}
}
