package app.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.RefTree;

import app.App;
import app.rcp.Icon;
import app.util.Trees;
import app.util.UI;

public class DependencyPage extends FormPage {

	private DataSetEditor editor;
	private IDataSet dataSet;

	public DependencyPage(DataSetEditor editor, IDataSet dataSet) {
		super(editor, "DependencyPage", "#Dependencies");
		this.editor = editor;
		this.dataSet = dataSet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		FormToolkit tk = mform.getToolkit();
		ScrolledForm form = UI.formHeader(mform, "#Dependencies");
		Composite body = UI.formBody(form, tk);
		TreeViewer tree = Trees.createViewer(body, "#Field", "Reference");
		tree.getTree().setLinesVisible(false);
		tree.setContentProvider(new ContentProvider());
		tree.setLabelProvider(new Label());
		UI.gridData(tree.getTree(), true, true);
		Trees.bindColumnWidths(tree.getTree(), 0.4, 0.6);
		form.reflow(true);
		setInput(tree);
		editor.onSaved(() -> setInput(tree));
	}

	private void setInput(TreeViewer tree) {
		RefTree refTree = RefTree.create(dataSet);
		refTree.root.field = App.s(dataSet.getName());
		tree.setInput(refTree);
		tree.expandAll();
	}

	private class ContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object obj) {
			if (!(obj instanceof RefTree))
				return new Object[0];
			RefTree tree = (RefTree) obj;
			return new Object[] { tree.root };
		}

		@Override
		public Object[] getChildren(Object obj) {
			if (!(obj instanceof RefTree.Node))
				return new Object[0];
			RefTree.Node node = (RefTree.Node) obj;
			return node.childs.toArray();
		}

		@Override
		public Object getParent(Object elem) {
			return null;
		}

		@Override
		public boolean hasChildren(Object obj) {
			if (!(obj instanceof RefTree.Node))
				return false;
			RefTree.Node node = (RefTree.Node) obj;
			return !node.childs.isEmpty();
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col != 1)
				return null;
			if (!(obj instanceof RefTree.Node))
				return null;
			RefTree.Node node = (RefTree.Node) obj;
			if (node.ref == null || node.ref.type == null)
				return null;
			return Icon.img(node.ref.type);
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof RefTree.Node))
				return null;
			RefTree.Node node = (RefTree.Node) obj;
			if (col == 0)
				return node.field;
			if (node.ref == null)
				return null;
			String s = LangString.getFirst(node.ref.name, App.lang);
			return s;
		}

	}

}
