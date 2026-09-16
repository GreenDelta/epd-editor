package app.editors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.openlca.commons.Strings;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.EpdProfiles;
import org.openlca.ilcd.util.RefTree;

import app.App;
import app.M;
import app.rcp.Icon;
import app.util.Colors;
import app.util.Trees;
import app.util.UI;
import app.util.Viewers;

public class DependencyPage extends FormPage {

	private final BaseEditor editor;
	private final IDataSet dataSet;
	private final HashMap<String, Ref> indexRefs = new HashMap<>();
	private final HashSet<String> profileRefs = new HashSet<>();

	public DependencyPage(BaseEditor editor, IDataSet dataSet) {
		super(editor, "DependencyPage", M.DataSetReferences);
		this.editor = editor;
		this.dataSet = dataSet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var form = UI.formHeader(mform, M.DataSetReferences);
		var body = UI.formBody(form, tk);

		var tree = Trees.createViewer(body, M.XMLField,
			M.DataSetReference, M.UUID, M.DataSetVersion);
		tree.getTree().setLinesVisible(false);
		tree.setContentProvider(new ContentProvider());
		tree.setLabelProvider(new Label());
		UI.stretchXY(tree.getTree());
		Trees.bindColumnWidths(tree.getTree(), 0.3, 0.5, 0.1, 0.1);
		form.reflow(true);
		setInput(tree);

		editor.onSaved(() -> setInput(tree));
		tree.addDoubleClickListener(_ -> {
			RefTree.Node node = Viewers.getFirstSelected(tree);
			if (node != null && node.ref != null)
				Editors.open(node.ref);
		});
	}

	private void setInput(TreeViewer tree) {
		var refTree = RefTree.create(dataSet);
		indexRefs.clear();
		profileRefs.clear();
		for (var ref : refTree.getRefs()) {
			if (!ref.isValid())
				continue;
			var r = App.index().find(ref);
			indexRefs.put(ref.getUUID(), r);
		}
		profileRefs.addAll(profileIds());
		tree.setInput(refTree);
		tree.expandAll();
	}

	/// Collects the UUIDs of the indicator and unit references that are
	/// available in the current EPD profiles.
	private Set<String> profileIds() {
		var ids = new HashSet<String>();
		for (var profile : EpdProfiles.getAll()) {
			for (var i : profile.getIndicators()) {
				addIdOf(ids, i.getRef());
				addIdOf(ids, i.getUnit());
			}
		}
		return ids;
	}

	private void addIdOf(Set<String> uuids, Ref ref) {
		if (ref != null && Strings.isNotBlank(ref.getUUID())) {
			uuids.add(ref.getUUID());
		}
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
			if (col > 1)
				return null;
			if (!(obj instanceof RefTree.Node node))
				return null;
			if (col == 1 && node.ref != null)
				return Icon.img(node.ref.getType());

			if (col != 0)
				return null;

			if (node.ref == null)
				return Icon.FOLDER.img();
			var uuid = node.ref.getUUID();
			if (Strings.isBlank(uuid))
				return Icon.ERROR.img();

			// indicators and units of the EPD profiles are not part
			// of the local data store
			boolean available = indexRefs.get(uuid) != null
				|| profileRefs.contains(uuid);
			return available
				? Icon.OK.img()
				: Icon.ERROR.img();
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
				case 1 -> App.s(node.ref.getName());
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
