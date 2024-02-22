package app.editors;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Colors;
import app.util.Trees;
import app.util.UI;
import app.util.Viewers;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.RefTree;

import java.util.HashMap;

public class DependencyPage extends FormPage {

	private final BaseEditor editor;
	private final IDataSet dataSet;
	private final HashMap<String, Ref> indexRefs = new HashMap<>();

	public DependencyPage(BaseEditor editor, IDataSet dataSet) {
		super(editor, "DependencyPage", M.DataSetReferences);
		this.editor = editor;
		this.dataSet = dataSet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, M.DataSetReferences);
		Composite body = UI.formBody(form, tk);
		TreeViewer tree = Trees.createViewer(body, M.XMLField,
			M.DataSetReference, M.UUID, M.DataSetVersion);
		tree.getTree().setLinesVisible(false);
		tree.setContentProvider(new ContentProvider());
		tree.setLabelProvider(new Label());
		UI.gridData(tree.getTree(), true, true);
		Trees.bindColumnWidths(tree.getTree(), 0.3, 0.5, 0.1, 0.1);
		form.reflow(true);
		setInput(tree);
		editor.onSaved(() -> setInput(tree));
		tree.addDoubleClickListener(e -> {
			RefTree.Node node = Viewers.getFirstSelected(tree);
			if (node != null && node.ref != null)
				Editors.open(node.ref);
		});
	}

	private void setInput(TreeViewer tree) {
		RefTree refTree = RefTree.create(dataSet);
		indexRefs.clear();
		for (Ref ref : refTree.getRefs()) {
			if (!ref.isValid())
				continue;
			Ref r = App.index().find(ref);
			indexRefs.put(ref.getUUID(), r);
		}
		tree.setInput(refTree);
		tree.expandAll();
	}

	private static class ContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object obj) {
			if (!(obj instanceof RefTree tree))
				return new Object[0];
			return new Object[]{tree.root};
		}

		@Override
		public Object[] getChildren(Object obj) {
			if (!(obj instanceof RefTree.Node node))
				return new Object[0];
			return node.childs.toArray();
		}

		@Override
		public Object getParent(Object elem) {
			return null;
		}

		@Override
		public boolean hasChildren(Object obj) {
			if (!(obj instanceof RefTree.Node node))
				return false;
			return !node.childs.isEmpty();
		}
	}

	private class Label extends LabelProvider
		implements ITableLabelProvider, ITableColorProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col == 2 || col == 3)
				return null;
			if (!(obj instanceof RefTree.Node node))
				return null;
			if (col == 0) {
				if (node.ref == null)
					return Icon.FOLDER.img();
				Ref ir = indexRefs.get(node.ref.getUUID());
				if (ir != null)
					return Icon.OK.img();
				else
					return Icon.ERROR.img();
			}
			if (col == 1 && node.ref != null)
				return Icon.img(node.ref.getType());
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof RefTree.Node node))
				return null;
			if (col == 0)
				return node.field;
			if (node.ref == null)
				return null;
			return switch (col) {
				case 1 -> LangString.getFirst(node.ref.withName(), App.lang());
				case 2 -> node.ref.getUUID();
				case 3 -> node.ref.getVersion();
				default -> null;
			};
		}

		@Override
		public Color getForeground(Object obj, int col) {
			if (col == 0)
				return Colors.gray();
			return null;
		}

		@Override
		public Color getBackground(Object obj, int col) {
			return null;
		}
	}
}
