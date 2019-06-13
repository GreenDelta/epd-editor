package app.editors.epd.contents;

import java.util.List;
import java.util.stream.IntStream;

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

import app.App;
import app.M;
import app.editors.epd.EpdEditor;
import app.util.Actions;
import app.util.Trees;
import app.util.UI;
import app.util.Viewers;
import epd.model.content.ContentDeclaration;
import epd.model.content.ContentElement;
import epd.model.content.Substance;
import epd.util.Fn;

class ContentTree {

	private final EpdEditor editor;
	private final ContentDeclaration decl;

	boolean forPackaging;
	private TreeViewer tree;

	public ContentTree(EpdEditor editor, ContentDeclaration decl) {
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

		// TODO: initially sort the content
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
				if (obj instanceof Substance) {
					Substance subst = (Substance) obj;
					boolean pack = subst.packaging != null && subst.packaging;
					return pack == forPackaging;
				}
				return !forPackaging;
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
		Fn.with(tree.getTree(),
				t -> t.setMenu(menu.createContextMenu(t)));

	}

	private void onAdd(ContentType type) {
		if (type == null)
			return;
		ContentElement elem = type.newInstance();
		if (elem instanceof Substance) {
			((Substance) elem).packaging = forPackaging;
		}
		if (ContentDialog.open(decl, elem) != Dialog.OK)
			return;
		tree.setInput(decl);
		editor.setDirty();
		tree.expandToLevel(elem, TreeViewer.ALL_LEVELS);
	}

	private void onEdit() {
		ContentElement elem = Viewers.getFirstSelected(tree);
		if (elem == null)
			return;
		if (ContentDialog.open(decl, elem) != Dialog.OK)
			return;
		tree.setInput(decl);
		editor.setDirty();
	}

	private void onDelete() {
		ContentElement elem = Viewers.getFirstSelected(tree);
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
			if (!(obj instanceof ContentDeclaration))
				return null;
			ContentDeclaration decl = (ContentDeclaration) obj;
			return decl.content.toArray();
		}

		@Override
		public Object[] getChildren(Object obj) {
			if (!(obj instanceof ContentElement))
				return null;
			ContentElement elem = (ContentElement) obj;
			List<?> childs = Content.childs(elem);
			return childs.isEmpty() ? null : childs.toArray();
		}

		@Override
		public Object getParent(Object obj) {
			if (!(obj instanceof ContentElement))
				return false;
			ContentElement elem = (ContentElement) obj;
			return Content.getParent(elem, decl);
		}

		@Override
		public boolean hasChildren(Object obj) {
			if (!(obj instanceof ContentElement))
				return false;
			ContentElement elem = (ContentElement) obj;
			return !Content.childs(elem).isEmpty();
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ContentElement))
				return null;

			ContentElement elem = (ContentElement) obj;
			Substance subst = null;
			if (elem instanceof Substance) {
				subst = (Substance) elem;
			}

			switch (col) {
			case 0:
				return App.s(elem.name);
			case 1:
				return elem.massPerc != null
						? elem.massPerc.toString() + " %"
						: null;
			case 2:
				return elem.mass != null
						? elem.mass.toString() + " kg"
						: null;
			case 3:
				return subst != null ? subst.casNumber : null;
			case 4:
				return subst != null ? subst.ecNumber : null;
			case 5:
				return subst != null ? subst.guid : null;
			case 6:
				return subst == null ? null
						: subst.renewable == null
								? null
								: subst.renewable.toString() + " %";
			case 7:
				return subst == null ? null
						: subst.recycled == null
								? null
								: subst.recycled.toString() + " %";
			case 8:
				return subst == null ? null
						: subst.recyclable == null
								? null
								: subst.recyclable.toString() + " %";
			default:
				return null;
			}
		}
	}
}
